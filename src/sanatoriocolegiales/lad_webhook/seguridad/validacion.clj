(ns sanatoriocolegiales.lad-webhook.seguridad.validacion
  (:require [buddy.sign.jwt :as jwt]
            [ring.util.response :refer [status]]
            [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta!]]
            [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [selecciona-guardia]]
            [sanatoriocolegiales.lad-webhook.historiasclinicas.utils :refer [extraer-fecha-y-hora]]))

(defn valida-request
  [req-body]
  (throw (ex-info "Solicitud no autorizada" req-body))
  #_(if req-body
    req-body
    (throw (ex-info "Solicitud no autorizada" req-body)))) 

(defn valida-paciente
  "Si tiene éxito añade al cuerpo el registro del paciente, la fecha, la hora y lo devuelve.
   Arroja excepción si la validación no es exitosa"
  [conn req-body]
  (let [{:keys [datetime event_object]} req-body
        {{:keys [uid]} :patient_external_id} event_object
        [fecha hora] (extraer-fecha-y-hora datetime)
        paciente (ejecuta! conn (selecciona-guardia uid fecha hora))]
    (if (> (count paciente) 0)
      (assoc req-body :paciente paciente :fecha fecha :hora hora)
      (throw (ex-info "Paciente no encontrado" {:fecha fecha
                                                :hora hora
                                                :hc uid})))))