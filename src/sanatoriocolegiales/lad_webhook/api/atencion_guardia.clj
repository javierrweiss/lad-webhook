(ns sanatoriocolegiales.lad-webhook.api.atencion-guardia
  "Atenciones de teleconsulta en guardia con servicio LAD"
  (:require
   [sanatoriocolegiales.error.error :refer [lanza-error]]
   [ring.util.response :refer [created]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia :refer [persiste-historia-clinica]]
   [fmnoise.flow :as flow :refer [then else]]
   [sanatoriocolegiales.lad-webhook.seguridad.validacion :refer [valida-paciente valida-request]]))

(defn procesar-atencion
  "Handler para las atenciones. 
   Recibe el request `req` y el estado del sistema `sys`"
  [req sys]
  (->> (valida-request req)
       (then #(valida-paciente sys %))
       (then #(persiste-historia-clinica sys %))
       (then created)
       (else lanza-error)))
 
(defn routes
  "Reitit route configuration for scoreboard endpoints
  Responses validated with sanatoriocolegiales.lad-webhook.spec clojure.spec"
  [system-config]
  ["/lad/historia_clinica_guardia"
   {:swagger {:tags ["Atenciones de Guardia con servicio LAD"]}
    :post {:summary "Atenciones de teleconsulta en guardia con servicio LAD"
           :description "Procesa las atenciones y genera/actualiza la historia clínica del paciente"
           :handler #(procesar-atencion % system-config)}}])
