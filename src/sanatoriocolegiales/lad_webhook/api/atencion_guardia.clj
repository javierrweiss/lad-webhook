(ns sanatoriocolegiales.lad-webhook.api.atencion-guardia
  "Atenciones de teleconsulta en guardia con servicio LAD"
  (:require
   [ring.util.response :refer [created response]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [ingresar-historia-a-sistema extraer-event-object]]
   [fmnoise.flow :as flow :refer [then]]
   [sanatoriocolegiales.lad-webhook.seguridad.validacion :refer [valida-paciente valida-request valida-event-object]]
   [com.brunobonacci.mulog :as mulog]
   [cheshire.core :as json]
   [sanatoriocolegiales.lad-webhook.especificaciones.especificaciones :as especificaciones])
  (:import java.time.LocalDateTime))

(defn procesa-atencion
  [request sys]
  #_(tap> sys)
  (assoc
   (->> request
        valida-event-object
        (then #(extraer-event-object %))
        (then #(valida-paciente sys %))
        (then #(ingresar-historia-a-sistema sys %))
         json/encode
        (created "/"))
   :headers {"Content-Type" "application/json"}))

(defn procesa-eventos
  [{:keys [event_type] :as req} sys]
  (case event_type
    nil (throw (ex-info "El objeto no tiene la forma esperada" {:type :sanatoriocolegiales.lad-webhook.error.error/bad-request}))
    "CALL_ENDED" (procesa-atencion req sys)
    (do (mulog/log ::recepcion-evento-no-esperado :request req :fecha (LocalDateTime/now))
        (-> (response (json/encode {:mensaje "Recibido"}))
            (assoc :headers {"Content-Type" "application/json"})))))

(defn handler
  [conf req]
  (-> (valida-request req (:env conf))
      (procesa-eventos conf)))

(defn routes
  "Reitit route configuration"
  [system-config]
  ["/lad/historia_clinica_guardia"
   {:swagger {:tags ["Atenciones de Guardia con servicio LAD"]}
    :post {:summary "Atenciones de teleconsulta en guardia con servicio LAD"
           :description "Procesa las atenciones y genera/actualiza la historia clínica del paciente"
           :parameters {:query {:client_id string?
                                :client_secret string?}
                        :body  :message/message}
           :handler (partial handler system-config)}}])


(comment

  (created "/" {:id 233})
  (created "/" "Bien")
  (-> (ring.util.response/response "Bien")
      (ring.util.response/content-type "text/plain")))