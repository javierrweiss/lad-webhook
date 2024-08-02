(ns sanatoriocolegiales.lad-webhook.api.atencion-guardia
  "Atenciones de teleconsulta en guardia con servicio LAD"
  (:require 
   [ring.util.response :refer [created]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [persiste-historia-clinica]]
   [fmnoise.flow :as flow :refer [then]]
   [sanatoriocolegiales.lad-webhook.seguridad.validacion :refer [valida-paciente valida-request]]))

(defn procesar-atencion
  "Handler para las atenciones. 
   Recibe el request `req` y el estado del sistema `sys`"
  [{:keys [body-params]} sys] 
  (->> (valida-request body-params)
       (then #(valida-paciente sys %))
       (then #(persiste-historia-clinica sys %)) 
       (created "/"))) ;;Debería pasar en la respuesta el id de paciente
 
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
           :handler #(procesar-atencion % system-config)}}])
 

(comment

  (procesar-atencion {:body-params {:a 12 
                                    :b 334}} 
                     {})

  )