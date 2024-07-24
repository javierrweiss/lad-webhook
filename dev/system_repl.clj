;; ---------------------------------------------------------
;; Donut System REPL
;;
;; Tools for REPl workflow with Donut system components
;; ---------------------------------------------------------

(ns system-repl
  "Tools for REPl workflow with Donut system components"
  (:require
   [donut.system :as donut]
   [donut.system.repl :as donut-repl]
   [donut.system.repl.state :as donut-repl-state]
   [sanatoriocolegiales.lad-webhook.system :as system]
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]))

(def conf (read-config (io/resource "config.edn") {:profile :dev}))

(def conexiones {:desal (-> conf :db-type :postgres :desal)
                 :asistencial (-> conf :db-type :relativity :asistencial)
                 :maestros (-> conf :db-type :relativity :maestros)})
;; ---------------------------------------------------------
;; Donut named systems
;; `:donut.system/repl` is default named system,
;; bound to `sanatoriocolegiales.lad-webhook.system` configuration
(defmethod donut/named-system :donut.system/repl
  [_] system/main)

;; `dev` system, partially overriding main system configuration
;; to support the development workflow
(defmethod donut/named-system :dev
  [_] (donut/system :donut.system/repl
                    {[:env :app-env] "dev"
                     [:env :app-version] "0.0.0-SNAPSHOT"
                     [:services :http-server ::donut/config :options :join?] false
                     [:env :persistence] conexiones
                     [:env :http-port] (:service-port conf)
                     [:services :event-log-publisher ::donut/config]
                     {:publisher {:type :console :pretty? true}}}))
;; ---------------------------------------------------------

;; ---------------------------------------------------------
;; Donut REPL workflow helper functions

(defn start
  "Start services using a named-system configuration,
  use `:dev` named-system by default"
  ([] (start :dev))
  ([named-system] (donut-repl/start named-system)))

(defn stop
  "Stop the currently running system"
  []  (donut-repl/stop))

(defn restart
  "Restart the system with donut repl,
  Uses clojure.tools.namespace.repl to reload namespaces
  `(clojure.tools.namespace.repl/refresh :after 'donut.system.repl/start)`"
  [] (donut-repl/restart))

(defn system
  "Return: fully qualified hash-map of system state"
  [] donut-repl-state/system)
;; ---------------------------------------------------------


(comment
  (-> conf :dbtype #_:postgres #_:desal)
  
  )