;; ---------------------------------------------------------
;; sanatoriocolegiales.lad-webhook
;;
;; TODO: Provide a meaningful description of the project
;;
;; Start the service using donut configuration and an environment profile.
;; ---------------------------------------------------------

(ns sanatoriocolegiales.lad-webhook.system
  "Service component lifecycle management"
  (:gen-class)
  (:require
   ;; Application dependencies
   [sanatoriocolegiales.lad-webhook.router :as router]
   [sanatoriocolegiales.lad-webhook.sql.conexiones :refer [cerrar crear-conexion-simple crear-connection-pool]]

   ;; Component system
   [donut.system :as donut]
   ;; [sanatoriocolegiales.lad-webhook.parse-system :as parse-system]

   ;; System dependencies
   [org.httpkit.server     :as http-server]
   [com.brunobonacci.mulog :as mulog]
   
   ;; Config
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [donut.system :as ds]))

;; ---------------------------------------------------------
;; Donut Party System configuration

(def conf (read-config (io/resource "config.edn") {:profile :prod}))

(def main
  "System Component management with Donut"
  {::donut/defs
   {:env
    {:app-version "0.1.0"
     :app-env "prod"
     :http-port (:service-port conf)
     :persistence
     {:desal (-> conf :dbtype :postgres :desal)
      :asistencial (-> conf :dbtype :relativity :asistencial)
      :maestros (-> conf :dbtype :relativity :maestros)}}
    :event-log
    {:publisher
     #::donut{:start (fn mulog-publisher-start
                       [{{:keys [global-context publisher]} ::donut/config}]
                       (mulog/set-global-context! global-context)
                       (mulog/log ::log-publish-component
                                  :publisher-config publisher
                                  :local-time (java.time.LocalDateTime/now))
                       (mulog/start-publisher! publisher))

              :stop (fn mulog-publisher-stop
                      [{::donut/keys [instance]}]
                      (mulog/log ::log-publish-component-shutdown :publisher instance :local-time (java.time.LocalDateTime/now))
                      ;; Pause so final messages have chance to be published
                      (Thread/sleep 250)
                      (instance))

              :config {:global-context {:app-name "sanatoriocolegiales lad-webhook service"
                                        :version (donut/ref [:env :app-version])
                                        :environment (donut/ref [:env :app-env])}
                       ;; Publish events to console in json format
                       ;; optionally add `:transform` function to filter events before publishing
                       :publisher {:type :console-json
                                   :pretty? false
                                   #_#_:transform identity}}}}

    :conexiones 
    {:maestros #::donut{:start (fn iniciar-conexion
                                 [{{:keys [specs]} ::donut/config}]
                                 (crear-conexion-simple specs))
               
                        :stop (fn detener-conexion
                                [{::donut/keys [instance]}]
                                (cerrar instance))
               
                        :config {:specs (donut/ref [:env :persistence :maestros])}}
     :desal #::donut{:start (fn iniciar-conexion
                              [{{:keys [specs]} ::donut/config}]
                              (crear-connection-pool specs))
            
                     :stop (fn detener-conexion
                             [{::donut/keys [instance]}]
                             (cerrar instance))
            
                     :config {:specs (donut/ref [:env :persistence :desal])}}
     :asistencial #::donut{:start (fn iniciar-conexion
                                    [{{:keys [specs]} ::donut/config}]
                                    (crear-conexion-simple specs))
                  
                           :stop (fn detener-conexion
                                   [{::donut/keys [instance]}]
                                   (cerrar instance))
                  
                           :config {:specs (donut/ref [:env :persistence :asistencial])}}
    }
    ;; HTTP server start - returns function to stop the server
    :http
    {:server
     #::donut{:start (fn http-kit-run-server
                       [{{:keys [handler options]} ::donut/config}]
                       (mulog/log ::http-server-component
                                  :handler handler
                                  :port (options :port)
                                  :local-time (java.time.LocalDateTime/now))
                       (http-server/run-server handler options))

              :stop  (fn http-kit-stop-server
                       [{::donut/keys [instance]}]
                       (mulog/log ::http-server-component-shutdown
                                  :http-server-instance instance
                                  :local-time (java.time.LocalDateTime/now))
                       (instance))

              :config {:handler (donut/local-ref [:handler])
                       :options {:port  (donut/ref [:env :http-port])
                                 :join? true}}}

     ;; Function handling all requests, passing system environment
     ;; Configure environment for router application, e.g. database connection details, etc.
     :handler (router/app (donut/ref [:env :persistence]))}}})

(defmethod ds/named-system :test
  [_]
  (ds/system main {}))

 