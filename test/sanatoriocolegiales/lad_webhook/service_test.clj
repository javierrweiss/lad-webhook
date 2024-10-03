(ns sanatoriocolegiales.lad-webhook.service-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-test run-all-tests]]
            [donut.system :as ds]
            [sanatoriocolegiales.lad-webhook.api.atencion-guardia :as guardia]
            [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [ingresar-historia-a-sistema]]
            [sanatoriocolegiales.lad-webhook.router :refer [app]]
            [clj-test-containers.core :as tc]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [ring.mock.request :as mock]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [sanatoriocolegiales.auxiliares :as aux])
  (:import [org.testcontainers.containers PostgreSQLContainer]))

(use-fixtures :once (ds/system-fixture
                     {::ds/defs {:testcontainer {:contenedor #::ds{:start (fn configurar-contenedor
                                                                            [_]
                                                                            (-> (tc/init {:container (-> (PostgreSQLContainer. "postgres:12.2") (.withInitScript "init.sql"))
                                                                                          :exposed-ports [5432]})
                                                                                (tc/start!))) 
                                                                   :stop (fn detener-contenedor
                                                                           [{::ds/keys [instance]}]
                                                                           (tc/stop! instance))}
                                                 :conexion #::ds{:start (fn conectar
                                                                          [{{{:keys [container]} :test-cont} ::ds/config}] 
                                                                          (let [opts {:user (.getUsername container)
                                                                                      :password (.getPassword container)}]
                                                                            (jdbc/get-connection (.getJdbcUrl container) opts)))
                                                                 :stop (fn cerrar
                                                                         [{::ds/keys [instance]}]
                                                                         (.close instance))
                                                                 :config {:test-cont (ds/local-ref [:contenedor])}}}}}))
 
(deftest dummy-connection-test
  (testing "Test de control que verifica instancia de testcontainer"
(with-open [conn (get-in ds/*system* [::ds/instances :testcontainer :conexion])] 
  (is (true? (instance? java.sql.Connection conn))))))

(deftest requests
  (with-open [conn (get-in ds/*system* [::ds/instances :testcontainer :conexion])]
    (let [sistema {:asistencial conn
                   :desal conn
                   :bases_auxiliares conn
                   :maestros conn
                   :env :test}
          request_evento_x {:event_type "CUALQUIERA"
                            :datetime ""
                            :event_object {}}
          request_evento_incompleto {:event_type "CALL_ENDED"
                                     :datetime "cualquiera"
                                     :event_object {}}
          request_autenticado_evento_vacio {:event_type "CALL_ENDED"
                                                :datetime "cualquiera"
                                                :event_object {}
                                                :query-params {"client_id" "lad" 
                                                               "client_secret" "123456"}}
          request_autenticado_evento_incompleto (assoc request_autenticado_evento_vacio :a 1 :b 2 :c 'ds :d true)
          payload (-> (io/resource "payload_model.edn") slurp edn/read-string)]
      (testing "Cuando recibe evento inesperado del servidor, responde 'Recibido'"
        (is (= "Recibido" (:body (guardia/procesa-eventos request_evento_x sistema)))))
      (testing "Cuando recibe evento inesperado del servidor, devuelve código 200"
        (is (== 200 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                (merge {:body-params {:event_type "CUALQUIERA"
                                                                      :datetime "cualquiera"
                                                                      :event_object {}}
                                                        :query-params {"client_id" "lad"
                                                                       "client_secret" "123456"}})))))))
      (testing "Cuando no recibe query string con autorización, lanza excepción <unauthorized>"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/handler sistema request_evento_incompleto)))
        (is (= "Solicitud no autorizada"  (try (guardia/handler sistema request_evento_incompleto)
                                               (catch clojure.lang.ExceptionInfo e (ex-message e))))))
      (testing "Cuando no recibe query string con autorización, devuelve código 401"
        (is (== 401 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                (merge {:body-params {:event_type "CUALQUIERA"
                                                                      :datetime "cualquiera"
                                                                      :event_object {}}})))))))
      (testing "Cuando recibe event object vacío, lanza excepción"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/handler sistema request_autenticado_evento_vacio))))
      (testing "Cuando recibe event object vacío, devuelve código 400"
        (is (== 400 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                (merge {:body-params {:event_type "CALL_ENDED"
                                                                      :datetime "cualquiera"
                                                                      :event_object {}}
                                                        :query-params {"client_id" "lad"
                                                                       "client_secret" "123456"}})))))))
      (testing "Cuando recibe event object con forma inesperada, lanza excepción"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/handler sistema request_autenticado_evento_incompleto))))
      (testing "Cuando recibe event objecto con forma inesperada, devuelve código 400"
        (is (== 400 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                (merge {:body-params {:e "C"
                                                                      :d "cualquiera"
                                                                      :a 12345}
                                                        :query-params {"client_id" "lad"
                                                                       "client_secret" "123456"}})))))))
      (testing "Cuando recibe una solicitud con un paciente no registrado, lanza excepción"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/procesa-atencion payload sistema)))
        (is (= "Paciente no encontrado"  (try (guardia/procesa-atencion payload sistema)
                                               (catch clojure.lang.ExceptionInfo e (ex-message e))))))
      (testing "Cuando recibe una solicitud con un paciente no registrado, devuelve código 404"
        (is (== 404 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                (merge {:body-params payload
                                                        :query-params {"client_id" "lad"
                                                                       "client_secret" "123456"}})))))))
      (testing "Cuando recibe solicitud correcta, devuelve estatus 201"
        (jdbc/execute! (:asistencial sistema) 
                       (aux/sql-insertar-registro-en-guardia 182222 20240808 1300 1 4 "John Doe" 1820 "GIHI" "11··$MMM" "A" "B" "Bla") 
                       {:builder-fn rs/as-unqualified-kebab-maps})
        (is (== 201 (:status (guardia/procesa-atencion payload sistema))))))))


(deftest ingreso-registros-db
  (with-open [conn (get-in ds/*system* [::ds/instances :testcontainer :conexion])]
    (let [sistema {:asistencial conn
                   :desal conn
                   :bases_auxiliares conn
                   :maestros conn
                   :env :test}
          paciente {:hc 145200
                    :fecha 20241002
                    :hora 1256
                    :nombre "Pepino El Breve"
                    :historia "Presenta un mal muy severo: estupidez"
                    :patologia "Estupidez cronica"
                    :diagnostico "Falta de cultura"
                    :motivo "Ignorancia"
                    :patient_external_id "145200"
                    :guar-hist-clinica 145200
                    :guar-fecha-ingreso 20241002
                    :guar-hora-ingreso 1256
                    :hora-inicio-atencion 1256
                    :hora-final-atencion 1314
                    :fecha-inicio-atencion 20241002
                    :guar-obra 1820
                    :guar-plan "4000-A"
                    :guar-nroben "1123-AC"
                    :descripcion-patologia "Cree saberlo todo y de todo opina"
                    :histpactratam 123456
                    :histpacmotivo 123457
                    :medico "Galeno"
                    :matricula 125546}
          _ (println (str "Insertando registro en guardia... "
                        (jdbc/execute! (:asistencial sistema)
                                       (aux/sql-insertar-registro-en-guardia 145200 20241002 1256 1 1 "Pepino El Breve" 1820 "4000-A" "1123-AC" "A" "B" "Bla")
                                       {:builder-fn rs/as-unqualified-kebab-maps})))
          ejecucion (ingresar-historia-a-sistema sistema paciente)] 
      (testing "Cuando ingresa exitosamente los registros, devuelve id (hc) del paciente"
        (is (== (:id ejecucion) (:hc paciente)))) 
      (let [registro-histpac (jdbc/execute! conn ["SELECT * FROM tbc_histpac"] {:builder-fn rs/as-unqualified-kebab-maps})
            registro-histpac-txt (jdbc/execute! conn ["SELECT * FROM tbc_histpac_txt"] {:builder-fn rs/as-unqualified-kebab-maps})
            registro-guardia (jdbc/execute! conn ["SELECT * FROM tbc_guardia WHERE guar_histclinica = 145200"] {:builder-fn rs/as-unqualified-kebab-maps})]
        (testing "Cuando ingresa exitosamente los registros, se obtiene la cantidad adecuada de registros por tabla"
          (is (== 5 (count registro-histpac-txt)))
          (is (== 1 (count registro-histpac))))
        (testing "Cuando ingresa exitosamente los registros, actualiza el registro correspondiente en tbc_guardia"
          (println (str "Consulta a guardia: " registro-guardia))
          (is (== 4 (-> registro-guardia first :guar-estado)))
          (is (== 9 (-> registro-guardia first :guar-diagnostico)))
          (is (== 20241002 (-> registro-guardia first :guar-fechaalta)))
          (is (== 1314 (-> registro-guardia first :guar-horaalta))))))))
 
(comment 
   
  (run-all-tests)

  (run-test ingreso-registros-db)
  
  (run-test dummy-connection-test)
  
  (run-test requests)

  
  (ds/instance (ds/named-system :test))

  (def cont
    (-> (tc/init {:container (-> (PostgreSQLContainer. "postgres:12.2") (.withInitScript "init.sql"))
                  :exposed-ports [5432]})
        (tc/start!)))

  (def cont2
    (-> (tc/init {:container (PostgreSQLContainer. "postgres:12.2")
                  :exposed-ports [5432]})
        (tc/map-classpath-resource!  {:resource-path "init.sql"
                                      :container-path "/docker-entrypoint-initdb.d/init.sql"
                                      :mode :read-only})
        (tc/start!)))

  (def cont-obj (:container #_cont cont2))

  (.getJdbcUrl cont-obj)

  (def conn (jdbc/get-connection (.getJdbcUrl cont-obj) {:user (.getUsername cont-obj)
                                                         :password (.getPassword cont-obj)}))

  (jdbc/execute! conn ["SELECT * FROM tbc_guardia"])
  (jdbc/execute! conn ["SELECT * FROM tbl_hist_txt"])
  (jdbc/execute! conn ["SELECT NOW()"])

  (tc/stop! cont2) 

  (with-open [conn (get-in ds/*system* [::ds/instances :testcontainer :conexion])]
    (let [sistema {:asistencial conn
                   :desal conn
                   :bases_auxiliares conn
                   :maestros conn
                   :env :test}
          mock-req (-> (mock/request :post "/lad/historia_clinica_guardia")
                       (merge {:body-params {:event_type "CALL_ENDED"
                                             :datetime "cualquiera"
                                             :event_object {}
                                             :query-params {"client_id" "lad"
                                                            "client_secret" "123456"}}}))] 
      (:status ((app sistema) mock-req)) 
      #_(:asistencial sistema)
      #_(jdbc/execute! (:asistencial sistema) ["SELECT * FROM tbc_guardia"])))
  

  :rcf)