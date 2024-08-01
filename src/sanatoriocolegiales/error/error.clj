(ns sanatoriocolegiales.error.error
  (:require [ring.util.response :refer [status]]
            [reitit.ring.middleware.exception :as exception])
  (:import java.sql.SQLException))

(defn lanza-error
  [^Throwable err]
  (assoc (ex-data err)
         :error (ex-message err)))

(defn handler
  [^Throwable err _]
  (let [mensaje (ex-message err)]
    (condp = mensaje
      "Solicitud no autorizada" (status "Solicitud no autorizada" 401)
      "Paciente no encontrado" (status "El paciente referido no fue encontrado" 404)
      (status (str "Hubo un error inesperado en el servidor:\n" mensaje) 500))))

(def exception-middleware
  (exception/create-exception-middleware 
   (merge 
    exception/default-handlers
    {SQLException (partial handler)})))