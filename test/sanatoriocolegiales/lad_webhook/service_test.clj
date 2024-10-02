(ns sanatoriocolegiales.lad-webhook.service-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-test run-all-tests]]
            [donut.system :as ds]
            [sanatoriocolegiales.lad-webhook.api.atencion-guardia :as guardia]
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
      (testing "Cuando no recibe query string con autorización, lanza excepción <unauthorized>"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/handler sistema request_evento_incompleto)))
        (is (= "Solicitud no autorizada"  (try (guardia/handler sistema request_evento_incompleto)
                                               (catch clojure.lang.ExceptionInfo e (ex-message e))))))
      (testing "Cuando recibe event object vacío, lanza excepción"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/handler sistema request_autenticado_evento_vacio))))
      (testing "Cuando recibe event object con forma inesperada, lanza excepción"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/handler sistema request_autenticado_evento_incompleto))))
      (testing "Cuando recibe una solicitud con un paciente no registrado, lanza excepción"
        (is (thrown? clojure.lang.ExceptionInfo (guardia/procesa-atencion payload sistema)))
        (is (= "Paciente no encontrado"  (try (guardia/procesa-atencion payload sistema)
                                               (catch clojure.lang.ExceptionInfo e (ex-message e))))))
      (testing "Cuando recibe solicitud correcta, devuelve estatus 201"
        (jdbc/execute! (:asistencial sistema) 
                       (aux/sql-insertar-registro-en-guardia 182222 20240808 1300 1 4 "John Doe" 1820 "GIHI" "11··$MMM" "A" "B" "Bla") 
                       {:builder-fn rs/as-unqualified-kebab-maps})
        (is (== 201 (:status (guardia/procesa-atencion payload sistema))))))))


(deftest ingreso
  (with-open [conn (get-in ds/*system* [::ds/instances :testcontainer :conexion])]
    (jdbc/execute! conn
                   (aux/sql-insertar-registro-en-guardia 182222 20240808 1300 1 4 "John Doe" 1820 "GIHI" "11··$MMM" "A" "B" "Bla")
                   {:builder-fn rs/as-unqualified-kebab-maps})
    (tap> (jdbc/execute! conn ["SELECT * from tbc_guardia"] {:builder-fn rs/as-unqualified-kebab-maps}))))

(comment 
   
  (run-test ingreso)
  
  (run-test dummy-connection-test)
  
  (run-test requests)

  (def mock-req (-> (mock/request :post "/lad/historia_clinica_guardia")
                    (merge {:body-params {:event_type "CALL_ENDED"
                                          :datetime "cualquiera"
                                          :event_object {}
                                          :query-params {"client_id" "lad"
                                                         "client_secret" "123456"}}})))

  (:status (guardia/handler {:env :test} mock-req))
  
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
                   :env :test}]
      sistema
      #_(:asistencial sistema)
      #_(jdbc/execute! (:asistencial sistema) ["SELECT * FROM tbc_guardia"])))
 

  :rcf)