(ns sanatoriocolegiales.lad-webhook.sql.conexiones
  (:require [next.jdbc.connection :as connection]
            [next.jdbc :as jdbc]
            [com.brunobonacci.mulog :as mulog]
            [com.potetm.fusebox.timeout :as to])
  (:import com.zaxxer.hikari.HikariDataSource
           java.sql.Connection 
           java.time.LocalDateTime))

(def timeout
  (to/init {::to/timeout-ms 1000}))

(def conexion-simple-id (atom 0))

(def connection-pool-id (atom 0))

(defn crear-connection-pool ^HikariDataSource
  [specs]
  (try
    (mulog/log ::connection-pool-creada :fecha (LocalDateTime/now) :especificaciones specs)
    (connection/->pool HikariDataSource specs)
    (catch Exception e (do 
                         (mulog/log ::error-creacion-connection-pool 
                                    :fecha (LocalDateTime/now) 
                                    :mensaje (ex-message e)
                                    :id-conexion (swap! connection-pool-id inc))
                         (throw (ex-info "Excepción en en Hikari" {:mensaje (ex-message e)}))))))
 
(defn crear-conexion-simple
  [specs]
  (try
    (to/with-timeout timeout
      (mulog/log ::conexion-simple-creada
                 :fecha (LocalDateTime/now)
                 :especificaciones specs
                 :id-conexion (swap! conexion-simple-id inc))
      (jdbc/get-connection specs))
    (catch Exception e (do
                         (mulog/log ::error-creacion-conexion-simple :fecha (LocalDateTime/now) :mensaje (ex-message e))
                         (throw (ex-info "Excepción en Relativity" {:mensaje (ex-message e)}))))))

(defprotocol Cerrar-conexion
  (cerrar [this]))

(extend-protocol Cerrar-conexion
  nil
  (cerrar [this] this)
  HikariDataSource
  (cerrar [this] (.close this))
  Connection
  (cerrar [this] (.close this)))

(defprotocol Devolver-conexion
  (devolver [this]))

(extend-protocol Devolver-conexion
  nil
  (devolver [_] nil) 

  HikariDataSource
  (devolver [this] this) 

  Connection
  (devolver [this] (.close this))) 



(comment 
  (jdbc/get-connection nil)
  (def conn (connection/->pool HikariDataSource {:jdbcUrl "jdbc:sqlite:bochinche.db"}))
  (jdbc/execute! conn ["CREATE TABLE anunaki (id int not null, valor varchar)"])
  (cerrar conn)  

  (reset! conexion-simple-id 0)
  @conexion-simple-id
  ) 