(ns sanatoriocolegiales.lad-webhook.service-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [donut.system :as ds]
            [sanatoriocolegiales.lad-webhook.service :as lad-webhook]
            [clj-test-containers.core :as tc]
            [next.jdbc :as jdbc])
  (:import [org.testcontainers.containers PostgreSQLContainer]))

(use-fixtures :once (ds/system-fixture ::test))

(defmethod ds/named-system :test
  [_]
  (ds/system ::ds/defs {:testcontainer {:container #::ds{:start (fn configurar-contenedor
                                                                  []
                                                                  (tc/init {:container (PostgreSQLContainer. "postgres:12.2")
                                                                            :exposed-ports [5432]}))
                                                         :post-start (fn arrancar-contenedor
                                                                       [{::ds/keys [instance]}]
                                                                       (tc/map-classpath-resource! instance {:resource-path "init.sql"
                                                                                                            :container-path "/docker-entrypoint-initdb.d/init.sql"
                                                                                                            :mode :read-only})
                                                                       (tc/start! instance))
                                                         :stop (fn detener-contenedor
                                                                 [{::ds/keys [instance]}]
                                                                 (tc/stop! instance))}
                                        :conexion #::ds{:start (fn conectar
                                                                 [{{:keys [datasource]} ::ds/config}]
                                                                 (let [spec {:jdbc-url (.getJdbcUrl (:container datasource))
                                                                             :user (.getUsername (:container datasource))
                                                                             :password (.getPassword (:container datasource))}]
                                                                   (jdbc/get-connection spec)))
                                                        :stop (fn cerrar
                                                                [{::ds/keys [instance]}]
                                                                (.close instance))
                                                        :config {:datasource (ds/ref [:container])}}}}))

(deftest dummy-connection-test
  (testing ""
(with-open [conn @(ds/instance ds/*system* [:testcontainer :conexion])] 
  (is (true? (instance? java.sql.Connection conn))))))

(comment
  
  )