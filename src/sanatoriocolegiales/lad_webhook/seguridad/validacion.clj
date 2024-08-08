(ns sanatoriocolegiales.lad-webhook.seguridad.validacion
  (:require [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta!]]
            [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [selecciona-guardia]]
            [sanatoriocolegiales.lad-webhook.historiasclinicas.utils :refer [extraer-fecha-y-hora]]
            [hyperfiddle.rcf :refer [tests]]))

(defn valida-request
  [req-body] 
  (if req-body
    req-body
    (throw (ex-info "Solicitud no autorizada" {:type :sanatoriocolegiales.lad-webhook.error.error/no-autorizada})))) 

;; Voy a tener que parsear el request antes de validar

(defn valida-paciente
  "Si tiene éxito añade al cuerpo el registro del paciente, la fecha, la hora y lo devuelve.
   Arroja excepción si la validación no es exitosa"
  [{:keys [asistencial]} req-body]
  (let [{:keys [datetime event_object]} req-body
        {{:keys [uid]} :patient_external_id} event_object
        [fecha hora] (extraer-fecha-y-hora datetime)
        paciente (ejecuta! asistencial (selecciona-guardia uid fecha hora))]
    (if (> (count paciente) 0)
      (assoc req-body :paciente paciente :fecha fecha :hora hora)
      (throw (ex-info "Paciente no encontrado" {:type :sanatoriocolegiales.lad-webhook.error.error/recurso-no-encontrado
                                                :fecha fecha
                                                :hora hora
                                                :hc uid})))))

(tests
 
 )