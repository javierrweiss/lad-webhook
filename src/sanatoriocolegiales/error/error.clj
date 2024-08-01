(ns sanatoriocolegiales.error.error
  (:require [ring.util.response :refer [status]]))

(defn lanza-error
  [^Throwable err]
  (assoc (ex-data err)
         :error (ex-message err)))

(defn respuesta-http-de-error
  [^Throwable err]
  (let [mensaje (ex-message err)]
    (condp = mensaje
      "Solicitud no autorizada" (status "Solicitud no autorizada" 401)
      "Paciente no encontrado" (status "El paciente referido no fue encontrado" 404)
      (status (str "Hubo un error inesperado en el servidor:\n" mensaje) 500))))

