;; ---------------------------------------------------------
;; sanatoriocolegiales.lad-webhook
;; Start the service using donut system configuration
;; defined in `system.clj`
;;
;; The service consist of
;; - httpkit web application server
;; - metosin/reitit for routing and ring for request / response management
;; - mulog event logging service
;;
;; Related namespaces
;; `sanatoriocolegiales.lad-webhook/system` donut system configuration
;; ---------------------------------------------------------
(ns sanatoriocolegiales.lad-webhook.service
  "Gameboard service component lifecycle management"
  (:gen-class)
  (:require 
   [donut.system :as donut]))
;; --------------------------------------------------
;; Service entry point

(defn -main
  "sanatoriocolegiales lad-webhook service managed by donut system,
  Aero is used to configure the donut system configuration based on profile (dev, test, prod),
  allowing environment specific configuration, e.g. mulog publisher
  The shutdown hook gracefully stops the service on receipt of a SIGTERM from the infrastructure,
  giving the application 30 seconds before forced termination."
  []
  (let [profile (keyword (System/getenv "SERVICE_PROFILE"))
        ;; Reference to running system for shutdown hook
        running-system (donut/start (or profile :prod))]
    ;; Shutdown system components on SIGTERM
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. ^Runnable #(donut/signal running-system ::donut/stop)))))


(comment
  

  )


