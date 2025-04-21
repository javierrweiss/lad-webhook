(ns sanatoriocolegiales.lad-webhook.service-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-test run-all-tests]]
            [donut.system :as ds]
            [sanatoriocolegiales.lad-webhook.api.atencion-guardia :as atencion-guardia]
            [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [ingresar-historia-a-sistema]]
            [sanatoriocolegiales.lad-webhook.router :refer [app]]
            [clj-test-containers.core :as tc]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [ring.mock.request :as mock]
            [next.jdbc.connection :as connection]
            [sanatoriocolegiales.auxiliares :as aux]
            [sanatoriocolegiales.lad-webhook.especificaciones.especificaciones :as especificaciones]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.alpha :as spec]
            [cheshire.core :as json])
  (:import [org.testcontainers.containers PostgreSQLContainer]
           java.time.Instant
           com.zaxxer.hikari.HikariDataSource))

(use-fixtures :each (ds/system-fixture
                     {::ds/defs {:asistencial {:contenedor #::ds{:start (fn configurar-contenedor
                                                                          [_]
                                                                          (-> (tc/init {:container (-> (PostgreSQLContainer. "postgres:12.2") (.withInitScript "init_asistencial.sql"))
                                                                                        :exposed-ports [5432]})
                                                                              (tc/start!)))
                                                                 :stop (fn detener-contenedor
                                                                         [{::ds/keys [instance]}]
                                                                         (tc/stop! instance))}
                                               :conexion #::ds{:start (fn conectar
                                                                        [{{{:keys [container]} :test-cont} ::ds/config}]
                                                                        (let [opts {:username (.getUsername container)
                                                                                    :password (.getPassword container)}]
                                                                          (connection/->pool HikariDataSource (merge {:jdbcUrl (.getJdbcUrl container)} opts))))
                                                               :stop (fn cerrar
                                                                       [{::ds/keys [instance]}]
                                                                       (.close instance))
                                                               :config {:test-cont (ds/local-ref [:contenedor])}}}
                                 :desal {:contenedor #::ds{:start (fn configurar-contenedor
                                                                    [_]
                                                                    (-> (tc/init {:container (-> (PostgreSQLContainer. "postgres:12.2") (.withInitScript "init_desal.sql"))
                                                                                  :exposed-ports [5432]})
                                                                        (tc/start!)))
                                                           :stop (fn detener-contenedor
                                                                   [{::ds/keys [instance]}]
                                                                   (tc/stop! instance))}
                                         :conexion #::ds{:start (fn conectar
                                                                  [{{{:keys [container]} :test-cont} ::ds/config}]
                                                                  (let [opts {:username (.getUsername container)
                                                                              :password (.getPassword container)}]
                                                                    (connection/->pool HikariDataSource (merge {:jdbcUrl (.getJdbcUrl container)} opts))))
                                                         :stop (fn cerrar
                                                                 [{::ds/keys [instance]}]
                                                                 (.close instance))
                                                         :config {:test-cont (ds/local-ref [:contenedor])}}}
                                 :bases_auxiliares {:contenedor #::ds{:start (fn configurar-contenedor
                                                                               [_]
                                                                               (-> (tc/init {:container (-> (PostgreSQLContainer. "postgres:12.2") (.withInitScript "init_bases_auxiliares.sql"))
                                                                                             :exposed-ports [5432]})
                                                                                   (tc/start!)))
                                                                      :stop (fn detener-contenedor
                                                                              [{::ds/keys [instance]}]
                                                                              (tc/stop! instance))}
                                                    :conexion #::ds{:start (fn conectar
                                                                             [{{{:keys [container]} :test-cont} ::ds/config}]
                                                                             (let [opts {:username (.getUsername container)
                                                                                         :password (.getPassword container)}]
                                                                               (connection/->pool HikariDataSource (merge {:jdbcUrl (.getJdbcUrl container)} opts))))
                                                                    :stop (fn cerrar
                                                                            [{::ds/keys [instance]}]
                                                                            (.close instance))
                                                                    :config {:test-cont (ds/local-ref [:contenedor])}}}
                                 :maestros {:contenedor #::ds{:start (fn configurar-contenedor
                                                                       [_]
                                                                       (-> (tc/init {:container (-> (PostgreSQLContainer. "postgres:12.2") (.withInitScript "init_maestros.sql"))
                                                                                     :exposed-ports [5432]})
                                                                           (tc/start!)))
                                                              :stop (fn detener-contenedor
                                                                      [{::ds/keys [instance]}]
                                                                      (tc/stop! instance))}
                                            :conexion #::ds{:start (fn conectar
                                                                     [{{{:keys [container]} :test-cont} ::ds/config}]
                                                                     (let [opts {:username (.getUsername container)
                                                                                 :password (.getPassword container)}]
                                                                       (connection/->pool HikariDataSource (merge {:jdbcUrl (.getJdbcUrl container)} opts))))
                                                            :stop (fn cerrar
                                                                    [{::ds/keys [instance]}]
                                                                    (.close instance))
                                                            :config {:test-cont (ds/local-ref [:contenedor])}}}}}))

(defspec cuando-recibe-evento-inesperados-responde-200
  100
  (prop/for-all [prescripcion (spec/gen :prescription/event_object)
                 practices (spec/gen :practices/event_object)
                 caseclosed (spec/gen :case_closed/event_object)
                 couldnotcontact (spec/gen :could_not_contact/event_object)
                 appointmentcreated (spec/gen :appointment_created/event_object)
                 appointmentcancelled (spec/gen :appointment_cancelled/event_object)] 
                (let [sistema {:asistencial nil
                               :desal nil
                               :bases_auxiliares nil
                               :maestros nil
                               :env :test}
                      fecha (.toString (Instant/now))]
                  (is (== 200 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime fecha
                                                                                :event_type "PRESCRIPTION"
                                                                                :event_object prescripcion}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}}))))))
                  (is (== 200 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime fecha
                                                                                :event_type "PRACTICES"
                                                                                :event_object practices}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}}))))))
                  (is (== 200 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime fecha
                                                                                :event_type "CASE_CLOSED"
                                                                                :event_object caseclosed}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}}))))))
                  (is (== 200 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime fecha
                                                                                :event_type "COULD_NOT_CONTACT"
                                                                                :event_object couldnotcontact}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}}))))))
                  (is (== 200 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime fecha
                                                                                :event_type "APPOINTMENT_CREATED"
                                                                                :event_object appointmentcreated}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}}))))))
                  (is (== 200 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime fecha
                                                                                :event_type "APPOINTMENT_CANCELLED"
                                                                                :event_object appointmentcancelled}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}})))))))))

(defspec cuando-recibe-evento-inesperados-responde-recibido
  100
  (prop/for-all [prescripcion (spec/gen :prescription/event_object)
                 practices (spec/gen :practices/event_object)
                 caseclosed (spec/gen :case_closed/event_object)
                 couldnotcontact (spec/gen :could_not_contact/event_object)
                 appointmentcreated (spec/gen :appointment_created/event_object)
                 appointmentcancelled (spec/gen :appointment_cancelled/event_object)] 
                (let [sistema {:asistencial nil
                               :desal nil
                               :bases_auxiliares nil
                               :maestros nil
                               :env :test}
                      fecha (.toString (Instant/now))]
                  (is (= "Recibido" (-> ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                           (merge {:body-params {:datetime fecha
                                                                                 :event_type "PRESCRIPTION"
                                                                                 :event_object prescripcion}
                                                                   :query-params {"client_id" "lad"
                                                                                  "client_secret" "123456"}})))
                                        :body
                                        (json/decode keyword)
                                        :mensaje)))
                  (is (= "Recibido" (-> ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                           (merge {:body-params {:datetime fecha
                                                                                 :event_type "PRACTICES"
                                                                                 :event_object practices}
                                                                   :query-params {"client_id" "lad"
                                                                                  "client_secret" "123456"}})))
                                        :body
                                        (json/decode keyword)
                                        :mensaje)))
                  (is (= "Recibido" (-> ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                           (merge {:body-params {:datetime fecha
                                                                                 :event_type "CASE_CLOSED"
                                                                                 :event_object caseclosed}
                                                                   :query-params {"client_id" "lad"
                                                                                  "client_secret" "123456"}})))
                                        :body
                                        (json/decode keyword)
                                        :mensaje)))
                  (is (= "Recibido" (-> ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                           (merge {:body-params {:datetime fecha
                                                                                 :event_type "COULD_NOT_CONTACT"
                                                                                 :event_object couldnotcontact}
                                                                   :query-params {"client_id" "lad"
                                                                                  "client_secret" "123456"}})))
                                        :body
                                        (json/decode keyword)
                                        :mensaje)))
                  (is (= "Recibido" (-> ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                           (merge {:body-params {:datetime fecha
                                                                                 :event_type "APPOINTMENT_CREATED"
                                                                                 :event_object appointmentcreated}
                                                                   :query-params {"client_id" "lad"
                                                                                  "client_secret" "123456"}})))
                                        :body
                                        (json/decode keyword)
                                        :mensaje)))
                  (is (= "Recibido" (-> ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                           (merge {:body-params {:datetime fecha
                                                                                 :event_type "APPOINTMENT_CANCELLED"
                                                                                 :event_object appointmentcancelled}
                                                                   :query-params {"client_id" "lad"
                                                                                  "client_secret" "123456"}})))
                                        :body
                                        (json/decode keyword)
                                        :mensaje))))))

(defspec cuando-no-recibe-query-string-con-autorizacion-responde-401
  100
  (prop/for-all [msj (spec/gen :message/message)] 
                (let [sistema {:asistencial nil
                               :desal nil
                               :bases_auxiliares nil
                               :maestros nil
                               :env :test}]
                  (is (= 401 (:status 
                              (try 
                                ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                   (merge {:body-params msj}
                                                          {:query-params {"client_id" ""
                                                                          "client_secret" ""}})))
                                (catch Exception e (ex-message e)))))))))

(defspec cuando-recibe-objecto-invalido-devuelve-400
  100
  (prop/for-all [body (gen/bind (gen/tuple (gen/any) (gen/any))
                       (fn [[k v]] 
                         (gen/fmap #(assoc-in % [:event_object k] v) (spec/gen :message/message))))]
                (is (== 400 (:status ((app {}) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                   (merge
                                                    body
                                                    {:query-params {"client_id" "lad"
                                                                    "client_secret" "123456"}}))))))))

(defspec cuando-recibe-solicitud-correcta-y-se-inserta-paciente-devuelve-201 
  5
  (prop/for-all [call-ended (spec/gen :call_ended/event_object)] 
                (let [asistencial (get-in ds/*system* [::ds/instances :asistencial :conexion])
                      maestros (get-in ds/*system* [::ds/instances :maestros :conexion])
                      bases_auxiliares (get-in ds/*system* [::ds/instances :bases_auxiliares :conexion])
                      desal (get-in ds/*system* [::ds/instances :desal :conexion])
                      sistema {:asistencial (fn [] asistencial)
                               :desal desal
                               :bases_auxiliares bases_auxiliares
                               :maestros (fn [] maestros)
                               :env :test}]
                  (->> (:patient_external_id call-ended)
                       (Integer/parseInt)
                       aux/sql-insertar-registro-en-reservas
                       (jdbc/execute! ((:asistencial sistema))))
                  (Thread/sleep 100)
                       (is (== 201 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                               (merge {:body-params {:datetime (.toString (Instant/now))
                                                                                     :event_type "CALL_ENDED"
                                                                                     :event_object call-ended}
                                                                       :query-params {"client_id" "lad"
                                                                                      "client_secret" "123456"}})))))))))

(defspec cuando-recibe-solicitud-correcta-y-no-encuentra-paciente-devuelve-404
  10
  (prop/for-all [call-ended (spec/gen :call_ended/event_object)]
                (let [asistencial (get-in ds/*system* [::ds/instances :asistencial :conexion])
                      maestros (get-in ds/*system* [::ds/instances :maestros :conexion])
                      bases_auxiliares (get-in ds/*system* [::ds/instances :bases_auxiliares :conexion])
                      desal (get-in ds/*system* [::ds/instances :desal :conexion])
                      sistema {:asistencial (fn [] asistencial)
                               :desal desal
                               :bases_auxiliares bases_auxiliares
                               :maestros (fn [] maestros)
                               :env :test}]
                  (is (== 404 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime (.toString (Instant/now))
                                                                                :event_type "CALL_ENDED"
                                                                                :event_object call-ended}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}})))))))))


(defspec cuando-recibe-solicitud-correcta-y-no-puede-guardar-devuelve-500
  10
  (prop/for-all [call-ended (spec/gen :call_ended/event_object)]
                (let [asistencial (get-in ds/*system* [::ds/instances :asistencial :conexion])
                      maestros (get-in ds/*system* [::ds/instances :maestros :conexion])
                      bases_auxiliares (get-in ds/*system* [::ds/instances :bases_auxiliares :conexion])
                      desal (get-in ds/*system* [::ds/instances :desal :conexion])
                      sistema {:asistencial (fn [] asistencial)
                               :desal desal
                               :bases_auxiliares bases_auxiliares
                               :maestros (fn [] maestros)
                               :env :test}]
                  (is (== 500 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                                          (merge {:body-params {:datetime (.toString (Instant/now))
                                                                                :event_type "CALL_ENDED"
                                                                                :event_object call-ended}
                                                                  :query-params {"client_id" "lad"
                                                                                 "client_secret" "123456"}})))))))))


(deftest dummy-connection-test
  (testing "Test de control que verifica operatividad de testcontainer"
(let [asistencial (get-in ds/*system* [::ds/instances :asistencial :conexion])
      maestros (get-in ds/*system* [::ds/instances :maestros :conexion])
      bases_auxiliares (get-in ds/*system* [::ds/instances :bases_auxiliares :conexion])
      desal (get-in ds/*system* [::ds/instances :desal :conexion])] 
  (is (seq (jdbc/execute! asistencial ["SELECT NOW()"])))
  (is (seq (jdbc/execute! maestros ["SELECT NOW()"])))
  (is (seq (jdbc/execute! bases_auxiliares ["SELECT NOW()"])))
  (is (seq (jdbc/execute! desal ["SELECT NOW()"]))))))

#_(deftest requests
    
  
  (testing "Cuando recibe una solicitud con un paciente no registrado, devuelve código 404"
    (is (== 404 (:status ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                                            (merge {:body-params payload
                                                    :query-params {"client_id" "lad"
                                                                   "client_secret" "123456"}}))))))))


(deftest ingreso-registros-db 
  (let [asistencial (get-in ds/*system* [::ds/instances :asistencial :conexion])
        maestros (get-in ds/*system* [::ds/instances :maestros :conexion])
        bases_auxiliares (get-in ds/*system* [::ds/instances :bases_auxiliares :conexion])
        desal (get-in ds/*system* [::ds/instances :desal :conexion])
        sistema {:asistencial (fn [] asistencial)
                 :desal desal
                 :bases_auxiliares bases_auxiliares
                 :maestros (fn [] maestros)
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
    (let [registro-histpac (jdbc/execute! asistencial ["SELECT * FROM tbc_histpac"] {:builder-fn rs/as-unqualified-kebab-maps})
          registro-histpac-txt (jdbc/execute! asistencial ["SELECT * FROM tbc_histpac_txt"] {:builder-fn rs/as-unqualified-kebab-maps})
          registro-guardia (jdbc/execute! asistencial ["SELECT * FROM tbc_guardia WHERE guar_histclinica = 145200"] {:builder-fn rs/as-unqualified-kebab-maps})]
      (testing "Cuando ingresa exitosamente los registros, se obtiene la cantidad adecuada de registros por tabla"
        (is (== 5 (count registro-histpac-txt)))
        (is (== 1 (count registro-histpac))))
      (testing "Cuando ingresa exitosamente los registros, actualiza el registro correspondiente en tbc_guardia"
        (println (str "Consulta a guardia: " registro-guardia))
        (is (== 4 (-> registro-guardia first :guar-estado)))
        (is (== 9 (-> registro-guardia first :guar-diagnostico)))
        (is (== 20241002 (-> registro-guardia first :guar-fechaalta)))
        (is (== 1314 (-> registro-guardia first :guar-horaalta)))))))
 
(comment

  (run-all-tests)

  (run-test ingreso-registros-db)

  (run-test dummy-connection-test)

  (run-test cuando-recibe-evento-inesperados-responde-200)

  (run-test cuando-recibe-evento-inesperados-responde-recibido)

  (run-test cuando-no-recibe-query-string-con-autorizacion-responde-401)

  (run-test cuando-recibe-objecto-invalido-devuelve-400) 

  (run-test cuando-recibe-solicitud-correcta-y-se-inserta-paciente-devuelve-201)

  (run-test cuando-recibe-solicitud-correcta-y-no-puede-guardar-devuelve-500)

  (run-test cuando-recibe-solicitud-correcta-y-no-encuentra-paciente-devuelve-404)
  
  
  (let [m (gen/generate (gen/bind  (gen/fmap #(assoc-in % [:event_object :m] "sdds") (spec/gen :message/message))
                                   (fn [mp]
                                     (gen/elements [{:body-params {:datetime (.toString (Instant/now))
                                                                   :event_type "APPOINTMENT_CREATED"
                                                                   :event_object mp}
                                                     :query-params {"client_id" "lad"
                                                                    "client_secret" "123456"}}
                                                    {:body-params mp
                                                     :query-params {"client_id" "lad"
                                                                    "client_secret" "123456"}}]))))]
    ((app {}) (-> (mock/request :post "/lad/historia_clinica_guardia")
                  (merge m))))


  ((app {}) (-> (mock/request :post "/lad/historia_clinica_guardia")
                (merge {:body-params (gen/generate (spec/gen :message/message))}
                       {:query-params {"client_id" ""
                                       "client_secret" ""}})))

  (atencion-guardia/handler {} (-> (mock/request :post "/lad/historia_clinica_guardia")
                                   (merge {:body-params (gen/generate (spec/gen :message/message))}
                                          {:query-params {"client_id" ""
                                                          "client_secret" ""}})))

  (with-open [conn (get-in ds/*system* [::ds/instances :testcontainer :conexion])]
    (let [sistema {:asistencial conn
                   :desal conn
                   :bases_auxiliares conn
                   :maestros conn
                   :env :test}]
      ((app sistema) (-> (mock/request :post "/lad/historia_clinica_guardia")
                         (merge {:body-params {:datetime (.toString (Instant/now))
                                               :event_type "CALL_ENDED"
                                               :event_object (gen/generate (spec/gen :call_ended/event_object))}
                                 :query-params {"client_id" "lad"
                                                "client_secret" "123456"}})))))

  (ds/instance (ds/named-system :test))

  (def cont
    (-> (tc/init {:container (-> (PostgreSQLContainer. "postgres:12.2") (.withInitScript "init.sql"))
                  :exposed-ports [5432]})
        (tc/start!)))

  (tc/stop! cont)

  (def cont2
    (-> (tc/init {:container (PostgreSQLContainer. "postgres:12.2")
                  :exposed-ports [5432]})
        (tc/map-classpath-resource!  {:resource-path "init.sql"
                                      :container-path "/docker-entrypoint-initdb.d/init.sql"
                                      :mode :read-only})
        (tc/start!)))

  (def cont-obj (:container cont #_cont2))

  (def conn (jdbc/get-connection (.getJdbcUrl cont-obj) {:user (.getUsername cont-obj)
                                                         :password (.getPassword cont-obj)}))
  
  (def conn2 (connection/->pool HikariDataSource (merge {:jdbcUrl (.getJdbcUrl cont-obj)} {:username (.getUsername cont-obj)
                                                                                           :password (.getPassword cont-obj)})))
  
  (def c (jdbc/get-connection conn2))
  
  (.close c)
  
  (jdbc/execute! conn2 ["SELECT NOW()"])

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