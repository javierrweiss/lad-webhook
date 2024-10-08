(ns sanatoriocolegiales.lad-webhook.seguridad.validacion
  (:require [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta!]]
            [sanatoriocolegiales.lad-webhook.seguridad.autorizacion :refer [autenticar-y-autorizar-solicitud]]
            [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [selecciona-guardia inserta-en-tbl-ladguardia-fallidos]]
            [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer []]
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

(defn valida-event-object
  "Recibe cuerpo del request, valida event-object y lo devuelve"
  [body]
  (letfn [(objeto-valido? [obj]
            ((every-pred :call_diagnosis
                         :call_cie10
                         :call_motive
                         :call_doctor_comment
                         :call_duration
                         :call_start_datetime
                         :order_id
                         :doctor_name
                         :doctor_enrollment_type
                         :patient_name
                         :patient_external_id)
             obj))]
    (let [event-object (:event_object body)]
      (cond 
        (objeto-valido? event-object) event-object
        :else (throw (ex-info "El objeto event-object no tiene la forma esperada" {:type :sanatoriocolegiales.lad-webhook.error.error/bad-request}))))))

(defn valida-paciente
  "Si tiene éxito devuelve un mapa con el registro del paciente, la fecha, la hora y la información necesaria del request.
   Arroja excepción si la validación no es exitosa y escribe los datos en una tabla auxiliar"
  [{:keys [asistencial bases_auxiliares]}
   {:keys [hc
           fecha
           hora
           nombre
           historia
           patologia
           diagnostico
           motivo
           patient_external_id]
    :as request-info}]
  (mulog/log ::validar-paciente :fecha (LocalDateTime/now) :paciente patient_external_id)
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
                                                :hc hc})))))


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

 (let [req {:event_type "CALL_ENDED"
            :datetime "x"
            :event_object {:patient_id "12345678",
                           :doctor_enrollment_prov "C",
                           :patient_phone "+1234567890",
                           :call_diagnosis "Otalgia y secreción del oído",
                           :patient_gender "F",
                           :call_cie10 "H92",
                           :call_doctor_rating 0,
                           :call_motive "Fiebre / Sin otros síntomas mencionados",
                           :call_patient_comment "",
                           :call_patient_rating 0,
                           :patient_email "johndoe@example.com",
                           :call_start_datetime "2024-07-12T01:12:19.225Z",
                           :doctor_specialty "Medicina General",
                           :patient_location_city "",
                           :rest_indication false,
                           :call_resolution "referred",
                           :doctor_enrollment "100016",
                           :provider_id "64ef63311b7b9a0091cc8934",
                           :patient_location_longitude -80,
                           :order_id "2024/08/08 13:00",
                           :call_doctor_comment
                           "FIEBRE DESDE HOY, REFIERE OTALGIA ESTUVO EN PISCINA Y MAR? DICE NO MOCO NI TOS CONTROL PRESENCIAL PARA OTOSCOPIA OTITIS EXTERNA O MEDIA?",
                           :patient_name "John Doe",
                           :patient_age 9,
                           :custom_questions [],
                           :patient_external_id "182222",
                           :call_duration 151,
                           :doctor_enrollment_type "123456",
                           :doctor_name "Amezqueta Marcela",
                           :patient_location_latitude 9,
                           :patient_location_country_code "PA",
                           :doctor_id "27217651420",
                           :patient_location_region_code "",
                           :call_id "669082f3492f32a38fe8fc37"}}
       req2 {:event_type "CALL_ENDED"
             :datetime "x"
             :event_object {:patient_id "12345678",
                            :doctor_enrollment_prov "C",
                            :patient_phone "+1234567890"
                            :patient_gender "F",
                            :call_cie10 "H92",
                            :call_doctor_rating 0,
                            :call_motive "Fiebre / Sin otros síntomas mencionados",
                            :call_patient_comment "",
                            :call_patient_rating 0,
                            :patient_email "johndoe@example.com",
                            :call_start_datetime "2024-07-12T01:12:19.225Z",
                            :doctor_specialty "Medicina General",
                            :patient_location_city "",
                            :rest_indication false,
                            :call_resolution "referred",
                            :doctor_enrollment "100016",
                            :provider_id "64ef63311b7b9a0091cc8934",
                            :patient_location_longitude -80,
                            :order_id "2024/08/08 13:00",
                            :call_doctor_comment
                            "FIEBRE DESDE HOY, REFIERE OTALGIA ESTUVO EN PISCINA Y MAR? DICE NO MOCO NI TOS CONTROL PRESENCIAL PARA OTOSCOPIA OTITIS EXTERNA O MEDIA?",
                            :patient_name "John Doe",
                            :patient_age 9,
                            :custom_questions [],
                            :patient_external_id "182222",
                            :call_duration 151,
                            :doctor_enrollment_type "123456",
                            :doctor_name "Amezqueta Marcela",
                            :patient_location_latitude 9,
                            :patient_location_country_code "PA",
                            :doctor_id "27217651420",
                            :patient_location_region_code "",
                            :call_id "669082f3492f32a38fe8fc37"}}
       req3 {:event_type "CALL_STARTED"
             :datetime "x"
             :event_object nil}]
   (valida-event-object req) := (:event_object req)
   (valida-event-object req2) :throws clojure.lang.ExceptionInfo
   (valida-event-object req3) :throws clojure.lang.ExceptionInfo)

 :rcf)


(comment

  (valida-event-object nil)

  (letfn [(objeto-valido? [obj]
            ((every-pred :call_diagnosis
                         :call_cie10
                         :call_motive
                         :call_doctor_comment
                         :call_duration
                         :call_start_datetime
                         :order_id
                         :doctor_name
                         :doctor_enrollment_type
                         :patient_name
                         :patient_external_id)
             obj))]
    (let [body nil]
      (cond
        #_#_(not= (:event_type body) "CALL_ENDED") body
        (objeto-valido? (:event_object body)) body
        :else (throw (ex-info "El objeto event-object no tiene la forma esperada" {:type :sanatoriocolegiales.lad-webhook.error.error/bad-request})))))

  (not= "CALL_STARTED" "CALL_ENDED")

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

  ((every-pred :a :b :c :d) {:a 2 :b "3d" :c 'sd :d true :e 23 :m 9980}))  