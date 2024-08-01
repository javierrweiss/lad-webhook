(ns sanatoriocolegiales.lad-webhook.api.atencion-guardia
  "Atenciones de teleconsulta en guardia con servicio LAD"
  (:require
   [sanatoriocolegiales.error.error :refer [lanza-error]]
   [ring.util.response :refer [response]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [persiste-historia-clinica]]
   [fmnoise.flow :as flow :refer [then else]]
   [sanatoriocolegiales.lad-webhook.seguridad.validacion :refer [valida-paciente valida-request]] 
   [com.brunobonacci.mulog :as mulog]))

(defn procesar-atencion
  "Handler para las atenciones. 
   Recibe el request `req` y el estado del sistema `sys`"
  [req sys] 
  (->> (valida-request req)
       (then #(valida-paciente sys %))
       (then #(persiste-historia-clinica sys %))
       (then (fn [_] (response "Ok")))
       (else lanza-error)))

 (defn dummy-handler
  [r s]
  (tap> r)
  (tap> s)
   (response "Ok"))
 
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
           :handler #(dummy-handler % system-config) #_#(procesar-atencion % system-config)}}])
 

(comment




  )