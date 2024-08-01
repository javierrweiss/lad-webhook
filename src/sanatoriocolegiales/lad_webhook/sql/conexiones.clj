(ns sanatoriocolegiales.lad-webhook.sql.conexiones
  (:require [next.jdbc.connection :as connection]
            [next.jdbc :as jdbc]
            [com.brunobonacci.mulog :as mulog])
  (:import com.zaxxer.hikari.HikariDataSource
           java.sql.Connection 
           java.time.LocalDateTime))

(defn crear-connection-pool ^HikariDataSource
  [specs]
  (try
    (mulog/log ::connection-pool-creada :fecha (LocalDateTime/now) :especificaciones specs)
    (connection/->pool HikariDataSource specs)
    (catch Exception e (mulog/log ::error-creacion-connection-pool :fecha (LocalDateTime/now) :mensaje (ex-message e)))))

(defn crear-conexion-simple
  [specs]
  (try
    (mulog/log ::connexion-simple-creada :fecha (LocalDateTime/now) :especificaciones specs)
    (jdbc/get-connection specs)
    (catch Exception e (mulog/log ::error-creacion-conexion-simple :fecha (LocalDateTime/now) :mensaje (ex-message e)))))

(defprotocol Cerrar-conexion
  (cerrar [this]))

(extend-protocol Cerrar-conexion
  nil
  (cerrar [this] this)
  HikariDataSource
  (cerrar [this] (.close this))
  Connection
  (cerrar [this] (.close this)))


(comment
  (jdbc/get-connection nil)
  
  )