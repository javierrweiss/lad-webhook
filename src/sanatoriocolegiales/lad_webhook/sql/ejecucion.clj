(ns sanatoriocolegiales.lad-webhook.sql.ejecucion
  (:require [next.jdbc :as jdbc] 
            [next.jdbc.result-set :as rs]
            [com.brunobonacci.mulog :as mulog])
  (:import java.time.LocalDateTime
           java.sql.SQLException))

(defmacro ejecutar-todo!
  [conn & sentencias]
  (when-not (every? vector? sentencias)
    (throw (ex-info "Las sentencias deben ser vectores" {:args sentencias})))
  `(try
     (mulog/log ::transaccion-sql :fecha (LocalDateTime/now) :enunciados-a-ejecutar ~sentencias)
     (jdbc/with-transaction [conn# ~conn] 
       ~@(for [sentencia sentencias]
           `(jdbc/execute! conn# ~sentencia)))
     (catch SQLException e (mulog/log ::excepcion-transaccion-sql :fecha (LocalDateTime/now) :mensaje (ex-message e)))))
 
(defn ejecuta!
  [conn sentencia]
  (jdbc/execute! conn sentencia {:builder-fn rs/as-unqualified-kebab-maps}))
  
(comment
  (require '[system-repl :refer [system]])
  (def desal (-> (system) :donut.system/instances :env :persistence :desal))
  (macroexpand '(ejecutar-todo desal ["SELECT * FROM tbl_eventlog_cirugia"] ["SELECT * FROM tbl_eventlog_cirugia"] ["SELECT * FROM tbl_eventlog_cirugia"])) 
  
  

  
  

  )