(ns sanatoriocolegiales.lad-webhook.sql.conexiones
  (:require [next.jdbc.connection :as connection]
            [next.jdbc :as jdbc]
            [com.brunobonacci.mulog :as mulog])
  (:import com.zaxxer.hikari.HikariDataSource
           java.sql.Connection
           java.time.LocalDateTime))

(defn crear-connection-pool ^HikariDataSource
  [{:keys [specs]}]
  (mulog/log ::connection-pool-creada :fecha (LocalDateTime/now))
  (connection/->pool HikariDataSource specs))

(defn crear-conexion-simple
  [{:keys [specs]}]
  (mulog/log ::connexion-simple-creada :fecha (LocalDateTime/now))
  (jdbc/get-connection specs))

(defprotocol Cerrar-conexion
  (cerrar [this]))

(extend-protocol Cerrar-conexion
  HikariDataSource
  (cerrar [this] (.close this))
  Connection
  (cerrar [this] (.close this)))

