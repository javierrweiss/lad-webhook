;; ---------------------------------------------------------
;; REPL workflow development tools
;;
;; Include development tool libraries vai aliases from practicalli/clojure-cli-config
;; Start Rich Terminal UI REPL prompt:
;; `clojure -M:repl/reloaded`
;;
;; Or call clojure jack-in from an editor to start a repl
;; including the `:dev/reloaded` alias
;; - alias included in the Emacs `.dir-locals.el` file
;; ---------------------------------------------------------

#_{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
(ns user
  "Tools for REPL Driven Development"
  (:require
   ;; REPL Workflow
   [system-repl :refer [start stop system restart]]
   [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [portal]  ; launch portal
   [portal.api :as inspect]
   [hyperfiddle.rcf]
   ;; Logging
   [com.brunobonacci.mulog :as mulog]  ; Event Logging
   [mulog-events]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [cheshire.core :as json]))                    ; Global context & Tap publisher

;; ---------------------------------------------------------
;; Help


(println "---------------------------------------------------------")
(println "Loading custom user namespace tools...")
(println "---------------------------------------------------------")

(defn help
  []
  (println "---------------------------------------------------------")
  (println "System components:")
  (println "(start)                        ; starts all components in system config")
  (println "(restart)                      ; read system config, reloads changed namespaces & restarts system")
  (println "(stop)                         ; shutdown all components in the system")
  (println "(system)                       ; show configuration of the running system")
  (println)
  (println "Hotload libraries:             ; Clojure 1.12.x")
  (println "(add-lib 'library-name)")
  (println "(add-libs '{domain/library-name {:mvn/version \"v1.2.3\"}})")
  (println "(sync-deps)                    ; load dependencies from deps.edn")
  (println "- deps-* lsp snippets for adding library")
  (println)
  (println "Portal Inspector:")
  (println "- portal started by default, listening to all evaluations")
  (println "(inspect/clear)                ; clear all values in portal")
  (println "(remove-tap #'inspect/submit)  ; stop sending to portal")
  (println "(inspect/close)                ; close portal")
  (println)
  (println "Mulog Publisher:")
  (println "- mulog publisher started by default")
  (println "(mulog-events/stop)            ; stop publishing log events")
  (println)
  (println "(help)                         ; print help text")
  (println "---------------------------------------------------------"))

(help)

;; End of Help
;; ---------------------------------------------------------

;; ---------------------------------------------------------
;; Avoid reloading `dev` code
;; - code in `dev` directory should be evaluated if changed to reload into repl
(println
 "Set REPL refresh directories to "
 (set-refresh-dirs "src" "resources"))
;; ---------------------------------------------------------

;; ---------------------------------------------------------
;; Mulog event logging
;; `mulog-publisher` namespace used to launch tap> events to tap-source (portal)
;; and set global context for all events

;; Example mulog event message
#_(mulog/log ::dev-user-ns
             :message "Example event from user namespace"
             :ns (ns-publics *ns*))
;; ---------------------------------------------------------

;; Iniciando hyperfiddle rcf

(println "Iniciando hyperfiddle rcf para tests...")
(hyperfiddle.rcf/enable!)

 
;; ---------------------------------------------------------
;; Hotload libraries into running REPL
;; `deps-*` LSP snippets to add dependency forms
(comment
  ;; Require for Clojure 1.11.x and earlier
  (require '[clojure.tools.deps.alpha.repl :refer [add-libs]])
  (add-libs '{domain/library-name {:mvn/version "1.0.0"}})
           
  (restart)
  (stop)     
  (start) 
    
   
  (tap> (:donut.system/instances (system))) 
  #'system
  ((-> (system) :donut.system/instances :http :handler))
 



  ;; Clojure 1.12.x onward
  #_(add-lib 'library-name)   ; find and add library
  #_(sync-deps)               ; load dependencies in deps.edn (if not yet loaded)
  #_()) ; End of rich comment
;; ---------------------------------------------------------

;; ---------------------------------------------------------
;; Portal Data Inspector
(comment
  ;; Open a portal inspector in browser window - light theme
  ;; (inspect/open {:portal.colors/theme :portal.colors/solarized-light})

  (inspect/clear) ; Clear all values in portal window (allows garbage collection)

  (remove-tap #'inspect/submit) ; Remove portal from `tap>` sources

  (mulog-events/stop) ; stop tap publisher

  (inspect/close); Close the portal window

  (inspect/open)

  (inspect/docs) ; View docs locally via Portal

  #_()) ; End of rich comment

;; ---------------------------------------------------------

(comment
  (require '[org.httpkit.client :as http]
           '[clojure.edn :as edn]
           '[clojure.java.io :as io]
           '[cheshire.core :as json])
  (def post-resource (-> (io/resource "payload_model.edn")
                         slurp
                         edn/read-string))

  @(http/post "http://127.0.0.1:2000/api/v1/lad/historia_clinica_guardia"
              {:body {:datetime "2024-07-12T01:17:19.813Z",
                      :event_type "CALL_ENDED",
                      :event_object (json/generate-string
                                     {:patient_id "12345678",
                                      :doctor_enrollment_prov "C",
                                      :patient_phone "+1234567890",
                                      :call_diagnosis "Otalgia y secreción del oído",
                                      :patient_gender "F",
                                      :call_cie10 "H92",
                                      :call_doctor_rating 0,
                                      :call_motive "Fiebre / Sin otros síntomas mencionados",
                                      :call_patient_comment "",
                                      :call_patient_rating 0,
                                      :patient_email "johndoe@example.com",
                                      :call_start_datetime "2024-07-12T01:12:19.225Z",
                                      :doctor_specialty "Medicina General",
                                      :patient_location_city "",
                                      :rest_indication false,
                                      :call_resolution "referred",
                                      :doctor_enrollment "100016",
                                      :provider_id "64ef63311b7b9a0091cc8934",
                                      :patient_location_longitude -80,
                                      :order_id "1-31764197940",
                                      :call_doctor_comment
                                      "FIEBRE DESDE HOY, REFIERE OTALGIA ESTUVO EN PISCINA Y MAR? DICE NO MOCO NI TOS CONTROL PRESENCIAL PARA OTOSCOPIA OTITIS EXTERNA O MEDIA?",
                                      :patient_name "John Doe",
                                      :patient_age 9,
                                      :custom_questions ""
                                      :patient_external_id
                                      {:idType "CI", :uid "abcd1234-ef56-7890-gh12-ijklmnopqrst"},
                                      :call_duration 151,
                                      :doctor_enrollment_type "MN",
                                      :doctor_name "Amezqueta Marcela",
                                      :patient_location_latitude 9,
                                      :patient_location_country_code "PA",
                                      :doctor_id "27217651420",
                                      :patient_location_region_code "",
                                      :call_id "669082f3492f32a38fe8fc37"})}})

  @(http/post "http://127.0.0.1:2000/api/v1/lad/historia_clinica_guardia"  {:body (json/generate-string {"datetime" "20240712",
                                                                                                         "event_type" "CALL_ENDED",
                                                                                                         "event_object"
                                                                                                         {"patient_id" "12345678",
                                                                                                          "doctor_enrollment_prov" "C"}})})
 
  )