(ns sanatoriocolegiales.lad-webhook.seguridad.validacion
  (:require [buddy.sign.jwt :as jwt]
            [ring.util.response :refer [status]]
            [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta!]]
            [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [selecciona-guardia]]))

(defn extraer-fecha-y-hora
  [date-str]
  )

(defn valida-request
  [req]
  (if req
    req
    (status "Solicitud no autorizada" 401)))

(defn valida-paciente
  [conn req]
  (let [{{:keys [datetime event_object]} :body} req
        {{:keys [uid]} :patient_external_id} event_object
        [fecha hora] (extraer-fecha-y-hora datetime)
        paciente (ejecuta! conn (selecciona-guardia uid fecha hora))]
    (if (> (count paciente) 0)
      (assoc req :paciente paciente)
      (throw (ex-info "Paciente no encontrado" {:fecha fecha
                                                :hora hora
                                                :hc uid})))))