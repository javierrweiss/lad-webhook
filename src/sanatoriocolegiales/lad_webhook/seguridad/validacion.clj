(ns sanatoriocolegiales.lad-webhook.seguridad.validacion
  (:require [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta!]]
            [sanatoriocolegiales.lad-webhook.seguridad.autorizacion :refer [autenticar-y-autorizar-solicitud]]
            [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [selecciona-guardia inserta-en-tbl-ladguardia-fallidos]]
            [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [extraer-event-object]] 
            [clojure.java.io :as io]
            [com.brunobonacci.mulog :as mulog]
            [hyperfiddle.rcf :refer [tests]]
            [clojure.edn :as edn]
            [ring.mock.request :as mock])
  (:import java.time.LocalDateTime))

(defn valida-request
  [req env]
  (mulog/log ::validar-request :fecha (LocalDateTime/now))
  (if (autenticar-y-autorizar-solicitud req env)
    (:body-params req)
    (throw (ex-info "Solicitud no autorizada" {:type :sanatoriocolegiales.lad-webhook.error.error/no-autorizada})))) 

(defn valida-paciente
  "Si tiene éxito devuelve un mapa con el registro del paciente, la fecha, la hora y la información necesaria del request.
   Arroja excepción si la validación no es exitosa y escribe los datos en una tabla auxiliar"
  [{:keys [asistencial bases_auxiliares]} {:keys [event_object]}]
  (mulog/log ::validar-paciente :fecha (LocalDateTime/now) :paciente (:patient_external_id event_object)) 
  (let [{:keys [hc
                fecha
                hora
                nombre
                historia
                patologia
                diagnostico
                motivo]
         :as request-info} (extraer-event-object event_object)]
    (if-let [paciente (->> (selecciona-guardia hc fecha hora) (ejecuta! asistencial) seq)] 
      (merge (first paciente) request-info)
      (do
        (ejecuta! bases_auxiliares (inserta-en-tbl-ladguardia-fallidos [hc
                                                                        fecha
                                                                        hora
                                                                        nombre
                                                                        historia
                                                                        patologia
                                                                        diagnostico
                                                                        motivo]))
        (throw (ex-info "Paciente no encontrado" {:type :sanatoriocolegiales.lad-webhook.error.error/recurso-no-encontrado
                                                  :fecha fecha
                                                  :hora hora
                                                  :hc hc}))))))


(tests
 
 (def payload (-> (io/resource "payload_model.edn") slurp edn/read-string))
 (def request-correcto (-> (mock/request :post "/lad/historia_clinica_guardia")
                           (assoc :body-params payload)))
 (def resultado-req-correcto (:body-params request-correcto))
 
 "Validación de request devuelve body-params cuando credenciales son correctas"
 (with-redefs-fn {#'autenticar-y-autorizar-solicitud (constantly true)} 
   #(valida-request request-correcto :dev)) := resultado-req-correcto 

 "Validación de request lanza excepción cuando credenciales son incorrectas"
 (with-redefs-fn {#'autenticar-y-autorizar-solicitud (constantly nil)}
   #(valida-request request-correcto :dev)) :throws clojure.lang.ExceptionInfo
 )    


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