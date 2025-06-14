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
   [clojure.java.io :as io]))

;; ---------------------------------------------------------
;; Donut Party System configuration

(def main
  "System Component management with Donut"
  {::donut/defs
   {:env
    {:app-env :prod
     :env-conf #::donut{:start (fn cargar-configuracion
                                 [{{:keys [perfil]} ::donut/config}] 
                                 (read-config (io/resource "config.edn") {:profile perfil}))
                        :config {:perfil (donut/local-ref [:app-env])}}}
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
                                        :version (donut/ref [:env :env-conf :version])
                                        :environment (donut/ref [:env :app-env])} 
                       :publisher {:type :simple-file
                                   :filename "lad_webhook/events.log"
                                   :transform (fn [sq] 
                                                (map #(dissoc % :especificaciones :system-config) sq))}}}}

    :conexiones 
    {:maestros #::donut{:start (fn iniciar-conexion
                                 [{{:keys [specs]} ::donut/config}] 
                                 (partial crear-conexion-simple specs))
               
                        :stop (fn detener-conexion
                                [{::donut/keys [instance]}]
                                (when-not (fn? instance)
                                  (cerrar instance)))
               
                        :config {:specs (donut/ref [:env :env-conf :db-type :relativity :maestros])}}
     :desal #::donut{:start (fn iniciar-conexion
                              [{{:keys [specs]} ::donut/config}]
                              (crear-connection-pool specs))
            
                     :stop (fn detener-conexion
                             [{::donut/keys [instance]}]
                             (cerrar instance))
            
                     :config {:specs (donut/ref [:env :env-conf :db-type :postgres :desal])}}
     :asistencial #::donut{:start (fn iniciar-conexion
                                    [{{:keys [specs]} ::donut/config}]
                                    (partial crear-conexion-simple specs))
                  
                           :stop (fn detener-conexion
                                   [{::donut/keys [instance]}]
                                   (when-not (fn? instance) 
                                     (cerrar instance)))
                  
                           :config {:specs (donut/ref [:env :env-conf :db-type :relativity :asistencial])}}
     :bases_auxiliares #::donut{:start (fn iniciar-conexion
                                         [{{:keys [specs]} ::donut/config}]
                                         (crear-connection-pool specs))
                                :stop (fn detener-conexion
                                        [{::donut/keys [instance]}]
                                        (cerrar instance))
                                :config {:specs (donut/ref [:env :env-conf :db-type :postgres :bases_auxiliares])}}
    }
    ;; HTTP server start - returns function to stop the server
    :http
    {:server
     #::donut{:start (fn http-kit-run-server
                       [{{:keys [handler options]} ::donut/config}]
                       (mulog/log ::http-server-component
                                  :handler handler
                                  :port (-> options :port)
                                  :local-time (java.time.LocalDateTime/now))
                       (http-server/run-server handler options))

              :stop  (fn http-kit-stop-server
                       [{::donut/keys [instance]}]
                       (mulog/log ::http-server-component-shutdown
                                  :http-server-instance instance
                                  :local-time (java.time.LocalDateTime/now))
                       (instance))

              :config {:handler (donut/local-ref [:handler])
                       :options {:port  (donut/ref [:env :env-conf :service-port])
                                 :join? true}}}
     
     :handler #::donut{:start (fn call-handler 
                                [{{:keys [db env]} ::donut/config}] 
                                (router/app (assoc db :env env)))
                       :config {:db (donut/ref [:conexiones])
                                :env (donut/ref [:env :app-env])}}}}})
 