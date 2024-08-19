(ns sanatoriocolegiales.lad-webhook.api.atencion-guardia
  "Atenciones de teleconsulta en guardia con servicio LAD"
  (:require 
   [ring.util.response :refer [created response]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [ingresar-historia-a-sistema]]
   [fmnoise.flow :as flow :refer [then]]
   [sanatoriocolegiales.lad-webhook.seguridad.validacion :refer [valida-paciente valida-request]]))

(defn procesa-atencion 
  [request sys] 
  (->> (valida-paciente sys request)
       (then #(ingresar-historia-a-sistema sys %))  
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
           :parameters {:query-string {:client_id string?
                                       :client_secret string?}
                        :body {:datetime string?
                               :event_type string?
                               :event_object map?}}
           :handler #(-> (valida-request % (:env system-config))
                      (procesa-eventos system-config))}}])
 

(comment
  
(created "/" {:id 233}) 
  (-> (ring.util.response/response "Bien")
      (ring.util.response/content-type "text/plain"))
  )