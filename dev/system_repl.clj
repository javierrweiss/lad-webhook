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
   [clojure.java.io :as io]
   [sql-auxiliar :refer [crear-tabla-tbc-hist-pac 
                         crear-tabla-tbc-guardia 
                         crear-tabla-tbc-histpac-txt
                         crear-tabla-tbl-ladguardia-fallidos
                         crear-tabla-tbl-parametros]]))

(def conf (read-config (io/resource "config.edn") {:profile :dev}))

(def conexiones {:desal (-> conf :db-type :postgres :desal)
                 :asistencial (-> conf :db-type :relativity :asistencial)
                 :maestros (-> conf :db-type :relativity :maestros)
                 :bases_auxiliares (-> conf :db-type :postgres :bases_auxiliares)})

(def conexiones2 {:desal {:jdbcUrl "jdbc:sqlite:dev-dbs/desal.db"}
                  :asistencial "jdbc:sqlite:dev-dbs/asistencial.db"
                  :maestros "jdbc:sqlite:dev-dbs/maestros.db"
                  :bases_auxiliares {:jdbcUrl "jdbc:sqlite:dev-dbs/bases_auxiliares.db"}})


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
                     [:env :persistence] conexiones2
                     [:conexiones :maestros ::donut/post-start] (fn [{{:keys [specs]} ::donut/config}]
                                                                  (println "Ejecutando post-start maestros..."))
                     [:conexiones :desal ::donut/post-start] (fn [{{:keys [specs]} ::donut/config}]
                                                               (println "Ejecutando post-start desal...")
                                                               (crear-tabla-tbl-parametros specs))
                     [:conexiones :asistencial ::donut/post-start] (fn [{{:keys [specs]} ::donut/config}]
                                                                     (println "Ejecutando post-start asistencial...")
                                                                     (crear-tabla-tbc-guardia specs)
                                                                     (crear-tabla-tbc-hist-pac specs)
                                                                     (crear-tabla-tbc-histpac-txt specs))
                     [:conexiones :bases_auxiliares ::donut/post-start] (fn [{{:keys [specs]} ::donut/config}]
                                                                          (println "Ejecutando post-start bases auxiliares")
                                                                          (crear-tabla-tbl-ladguardia-fallidos specs))
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
  (donut/system :dev {[:env :persistence] conexiones2})
   (donut/signal (donut/system :dev) ::donut/post-start)
  ) 