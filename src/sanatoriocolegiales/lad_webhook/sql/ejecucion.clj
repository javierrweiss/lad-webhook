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
  (try
    (jdbc/execute! conn sentencia {:builder-fn rs/as-unqualified-kebab-maps})
    (catch SQLException e (mulog/log ::excepcion-sql :fecha (LocalDateTime/now) :mensaje (ex-message e)))))
  
(comment  
  (macroexpand '(ejecutar-todo desal ["SELECT * FROM tbl_eventlog_cirugia"] ["SELECT * FROM tbl_eventlog_cirugia"] ["SELECT * FROM tbl_eventlog_cirugia"]))

  (def desal (-> (system-repl/system) :donut.system/instances :conexiones :desal)) 
  (def asistencial (-> (system-repl/system) :donut.system/instances :conexiones :asistencial)) 
   
  (ejecuta! desal ["PRAGMA table_info(tbl_hist_txt)"])
  (ejecuta! asistencial ["PRAGMA table_info(tbc_histpac)"])
  (ejecuta! asistencial ["PRAGMA table_info(tbc_guardia)"])
  
   

  
  

  )