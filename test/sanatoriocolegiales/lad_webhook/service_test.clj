(ns sanatoriocolegiales.lad-webhook.service-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-test run-all-tests]]
            [donut.system :as ds]
            [sanatoriocolegiales.lad-webhook.service :as lad-webhook]
            [clj-test-containers.core :as tc]
            [next.jdbc :as jdbc])
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
  (testing ""
(with-open [conn (get-in ds/*system* [::ds/instances :testcontainer :conexion])] 
  (is (true? (instance? java.sql.Connection conn))))))



(comment
  (run-test dummy-connection-test)
   
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
  :rcf)