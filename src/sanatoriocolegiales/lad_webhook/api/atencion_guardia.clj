(ns sanatoriocolegiales.lad-webhook.api.atencion-guardia
  "Atenciones de teleconsulta en guardia con servicio LAD"
  (:require 
   [ring.util.response :refer [created response]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [persiste-historia-clinica]]
   [fmnoise.flow :as flow :refer [then]]
   [sanatoriocolegiales.lad-webhook.seguridad.validacion :refer [valida-paciente valida-request]]))

(defn procesa-atencion 
  [request sys] 
  (->> (valida-paciente sys request)
       (then #(persiste-historia-clinica sys %)) 
       (created "/"))) 
 
(defn procesa-eventos
  [{:keys [event_type] :as req} sys] 
  (case event_type
    "CALL_ENDED" (procesa-atencion req sys)
      (-> (response "Recibido") 
          (assoc :headers {"Content-Type" "text/plain"}))))
     
(defn routes
  "Reitit route configuration"
  [system-config]
  ["/lad/historia_clinica_guardia"
   {:swagger {:tags ["Atenciones de Guardia con servicio LAD"]}
    :post {:summary "Atenciones de teleconsulta en guardia con servicio LAD"
           :description "Procesa las atenciones y genera/actualiza la historia clínica del paciente"
           :parameters {:body {:datetime string?
                               :event_type string?
                               :event_object map?}}
           :handler #(-> (valida-request %) 
                         (procesa-eventos system-config))}}])
 

(comment
  
(created "/" {:id 233})
  )