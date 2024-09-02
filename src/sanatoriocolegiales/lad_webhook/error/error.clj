(ns sanatoriocolegiales.lad-webhook.error.error
  (:require [ring.util.response :refer [status]]
            [reitit.ring.middleware.exception :as exception]
            [com.brunobonacci.mulog :as mulog])
  (:import java.sql.SQLException
           java.time.LocalDateTime))

(defn handler
  [mensaje _ _]
  mensaje)

(def exception-middleware
  (exception/create-exception-middleware 
   (merge 
    exception/default-handlers
    {SQLException (partial handler (status {:body "Hubo un error con la base de datos"} 500))
     ::excepcion-sql (partial handler (status {:body "Hubo un error con la base de datos"} 500))
     ::no-autorizada (partial handler (status {:body "Solicitud no autorizada"} 401))
     ::recurso-no-encontrado (partial handler (status {:body "No se encontró el paciente"} 404))
     ::exception/wrap (fn [handler e request]
                        (mulog/log ::excepcion-en-solicitud :mensaje (ex-message e) :fecha (LocalDateTime/now) :solicitud request)
                        (handler e request))}))) 
