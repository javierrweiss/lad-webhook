(ns sanatoriocolegiales.lad-webhook.api.atencion-guardia
  "Atenciones de teleconsulta en guardia con servicio LAD"
  (:require
   [ring.util.response :refer [response status]]
   [sanatoriocolegiales.lad-webhook.seguridad.validacion :refer [validar]]))

(defn procesar-atencion
  "Handler para las atenciones. 
   Recibe el request `req` y el estado del sistema `sys`"
  [req sys]
  (if (validar req)
    (response "do")
    (status "Solicitud no autorizada" 401)))

(defn routes
  "Reitit route configuration for scoreboard endpoints
  Responses validated with sanatoriocolegiales.lad-webhook.spec clojure.spec"
  [system-config]
  ["/lad/historia_clinica_guardia"
   {:swagger {:tags ["Atenciones de Guardia con servicio LAD"]}
    :post {:summary "Atenciones de teleconsulta en guardia con servicio LAD"
           :description "Procesa las atenciones y genera/actualiza la historia clínica del paciente"
           :handler #(procesar-atencion % system-config)}}])
