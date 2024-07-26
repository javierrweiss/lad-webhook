(ns sanatoriocolegiales.lad-webhook.sql.ejecucion
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [com.brunobonacci.mulog :as mulog]
            [sanatoriocolegiales.lad-webhook.utils.utils :refer [al-abrir]])
  (:import com.zaxxer.hikari.HikariDataSource
           java.sql.SQLException
           java.time.LocalDateTime))

(defn ejecuta-transaccion
  [conn stmt]
  (with-open [^HikariDataSource d  (connection/->pool com.zaxxer.hikari.HikariDataSource conn)]
    (jdbc/execute! d stmt)))


(comment
  
  (defn l
    [x]
    (mulog/log ::evento-de-prueba ::error x))

  (al-abrir [r (clojure.java.io/reader "cosa2.txt")] l
            (throw (java.io.IOException.)))
  
  (with-open [r (clojure.java.io/reader "cosa.txt")]
    (throw (java.io.IOException.)))
  
  (fn? (fn [x] (println x)))

  (l "Excepcion muy mala")

  )