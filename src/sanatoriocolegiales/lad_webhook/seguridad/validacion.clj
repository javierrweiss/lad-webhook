(ns sanatoriocolegiales.lad-webhook.seguridad.validacion
  (:require [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta!]]
            [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [selecciona-guardia inserta-en-tbl-ladguardia-fallidos]]
            [sanatoriocolegiales.lad-webhook.historiasclinicas.utils :refer [extraer-fecha-y-hora obtener-hora-finalizacion]] 
            [clojure.java.io :as io]))

(defn valida-request
  [req] 
  (if req
    (:body-params req)
    (throw (ex-info "Solicitud no autorizada" {:type :sanatoriocolegiales.lad-webhook.error.error/no-autorizada})))) 

(defn valida-paciente
  "Si tiene éxito devuelve un mapa con el registro del paciente, la fecha, la hora y la información necesaria del request.
   Arroja excepción si la validación no es exitosa y escribe los datos en una tabla auxiliar"
  [{:keys [asistencial bases_auxiliares]} {:keys [event_object]}]
  (let [{:keys [call_diagnosis
                call_cie10
                call_motive
                call_doctor_comment
                call_duration
                call_start_datetime
                order_id   ;; Acá recibiremos fecha y hora
                doctor_name
                doctor_enrollment_type
                patient_name
                patient_external_id ;; Acá recibimos el número de HC
                ]} event_object 
        [fecha hora] (extraer-fecha-y-hora order_id)
        [fecha-ini hora-ini] (extraer-fecha-y-hora call_start_datetime)
        hora-finalizacion (obtener-hora-finalizacion hora-ini call_duration)]
    (if-let [paciente (seq (ejecuta! asistencial (selecciona-guardia patient_external_id fecha hora)))]
      (assoc
       (first paciente) 
       :fecha-inicio-atencion fecha-ini
       :hora-inicio-atencion hora-ini
       :hora-final-atencion hora-finalizacion
       :diagnostico call_diagnosis
       :matricula (Integer/parseInt doctor_enrollment_type)
       :medico doctor_name
       :motivo call_motive
       :historia call_doctor_comment
       :nombre patient_name
       :hc (Integer/parseInt patient_external_id)
       :patologia call_cie10)
      (do
        (ejecuta! bases_auxiliares (inserta-en-tbl-ladguardia-fallidos [patient_external_id
                                                                        fecha
                                                                        hora
                                                                        patient_name
                                                                        call_doctor_comment
                                                                        call_cie10
                                                                        call_diagnosis
                                                                        call_motive]))
        (throw (ex-info "Paciente no encontrado" {:type :sanatoriocolegiales.lad-webhook.error.error/recurso-no-encontrado
                                                  :fecha fecha
                                                  :hora hora
                                                  :hc patient_external_id}))))))

(comment
  
  (def request (-> (io/resource "payload_model.edn") slurp clojure.edn/read-string)) 
 
  (let [conexiones {:asistencial (-> (system-repl/system) :donut.system/instances :conexiones :asistencial)
                    :bases_auxiliares (-> (system-repl/system) :donut.system/instances :conexiones :bases_auxiliares)}]
    (valida-paciente conexiones request))
  
  ;; No hay ventaja en desempeño ni en legibilidad para usar una sola desestructuración
  (dotimes [_ 10]
    (println "Una sola desestructuración....")
    (time
     (let [{:keys [datetime]
            {:keys [call_diagnosis
                    call_cie10
                    call_motive
                    call_doctor_comment
                    patient_name]
             {:keys [uid]} :patient_external_id} :event_object} request]
       (list datetime call_diagnosis call_cie10 call_motive call_doctor_comment patient_name uid)))
    (println "Desestructuraciones en sucesivas....")
    (time
     (let [{:keys [datetime event_object]} request
           {:keys [call_diagnosis
                   call_cie10
                   call_motive
                   call_doctor_comment
                   patient_name
                   patient_external_id]} event_object
           {:keys [uid]}  patient_external_id]
       (list datetime call_diagnosis call_cie10 call_motive call_doctor_comment patient_name uid))))
  ) 