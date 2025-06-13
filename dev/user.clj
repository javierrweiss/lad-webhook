{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
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
  (require '[clojure.repl.deps :refer :all])
  (add-lib '{org.clojure/test.check {:mvn/version "1.1.1"}}) 
   
  (restart)
  (stop)      
  (start) 
  
  (tap> (:env (:donut.system/instances (system)))) 
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

  ;; Flowstorm

  (require '[flow-storm.api :as fs-api])

  (fs-api/local-connect)

  (.availableProcessors (java.lang.Runtime/getRuntime))

  (dotimes [_ 10]
    (println "Usando java nio")
    (time
     (let [path (java.nio.file.Path/of (.toURI (io/resource "payload_model.edn")))]
       (java.nio.file.Files/readString path)))
    (println "Usando el reader implementado por Clojure")
    (time
     (slurp (io/resource "payload_model.edn"))))
  
  (mapv count particion)

  (count v1)
  (count v2)
(count v3)

  #_()) ; End of rich comment

;; ---------------------------------------------------------

(comment
  (require '[org.httpkit.client :as http])
  (def post-resource (-> (io/resource "payload_model.edn")
                         slurp
                         edn/read-string))

  (re-matches #"\d+" "-515")

  @(http/post "http://127.0.0.1:2000/lad/historia_clinica_guardia?client_id=lad&client_secret=123456"
              {:headers {"Content-Type" "application/json"}
               :body (json/generate-string {:datetime "2024-07-12T01:17:19.813Z",
                                            :event_type "CALL_ENDED",
                                            :event_object {:patient_id "12345678",
                                                           :doctor_enrollment_prov "C",
                                                           :patient_phone "+1234567890",
                                                           :call_diagnosis "Otalgia y secreción del oído",
                                                           :patient_gender "F",
                                                           :call_cie10 "H92",
                                                           :call_doctor_rating 0,
                                                           :call_motive "Fiebre / Sin otros síntomas mencionados",
                                                           :call_patient_comment "sdds",
                                                           :call_patient_rating 0,
                                                           :patient_email "johndoe@example.com",
                                                           :call_start_datetime "2024-07-12T01:12:19.225Z",
                                                           :doctor_specialty "Medicina General",
                                                           :patient_location_city "A",
                                                           :rest_indication false,
                                                           :call_resolution "referred",
                                                           :doctor_enrollment "100016",
                                                           :provider_id (str (random-uuid)),
                                                           :patient_location_longitude -80.0,
                                                           :order_id "2024/08/08 13:00",
                                                           :call_doctor_comment
                                                           "FIEBRE DESDE HOY, REFIERE OTALGIA ESTUVO EN PISCINA Y MAR? DICE NO MOCO NI TOS CONTROL PRESENCIAL PARA OTOSCOPIA OTITIS EXTERNA O MEDIA?",
                                                           :patient_name "John Doe",
                                                           :patient_age 9,
                                                           :custom_questions [] ,
                                                           :patient_external_id "45652",
                                                           :call_duration 151,
                                                           :doctor_enrollment_type "MP",
                                                           :doctor_name "Amezqueta Marcela",
                                                           :patient_location_latitude 9.01,
                                                           :patient_location_country_code "PA",
                                                           :doctor_id "27217651420",
                                                           :patient_location_region_code "D",
                                                           :call_id (str (random-uuid))}})})

  @(http/post "http://127.0.0.1:2000/lad/historia_clinica_guardia?client_id=lad&client_secret=123456"
              {:headers {"Content-Type" "application/json"}
               :body (json/generate-string {:datetime "2025-01-17T10:17:19.813Z",
                                            :event_type "CALL_ENDED",
                                            :event_object {:patient_id "12345678",
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
                                                           :call_start_datetime "2025-01-17T09:12:19.225Z",
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
                                                           :custom_questions "",
                                                           :patient_external_id {:idType "CI", :uid "abcd1234-ef56-7890-gh12-ijklmnopqrst"},
                                                           :call_duration 151,
                                                           :doctor_enrollment_type "334455",
                                                           :doctor_name "Amezqueta Marcela",
                                                           :patient_location_latitude 9,
                                                           :patient_location_country_code "PA",
                                                           :doctor_id "27217651420",
                                                           :patient_location_region_code "",
                                                           :call_id "669082f3492f32a38fe8fc37"}})})


  @(http/post "https://lad-webhook-dev.sanatoriocolegiales.com.ar/lad/historia_clinica_guardia?client_id=lad&client_secret=123456"
              {:headers {"Content-Type" "application/json"}
               :body (json/generate-string {:datetime "2025-01-17T10:17:19.813Z",
                                            :event_type "CALL_ENDED",
                                            :event_object {:patient_id "12345678",
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
                                                           :call_start_datetime "2025-01-17T09:12:19.225Z",
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
                                                           :custom_questions "",
                                                           :patient_external_id {:idType "CI", :uid "abcd1234-ef56-7890-gh12-ijklmnopqrst"},
                                                           :call_duration 151,
                                                           :doctor_enrollment_type "334455",
                                                           :doctor_name "Amezqueta Marcela",
                                                           :patient_location_latitude 9,
                                                           :patient_location_country_code "PA",
                                                           :doctor_id "27217651420",
                                                           :patient_location_region_code "",
                                                           :call_id "669082f3492f32a38fe8fc37"}})})

  @(http/post "http://127.0.0.1:2000/api/v1/lad/historia_clinica_guardia"  {:body (json/generate-string {"datetime" "20240712",
                                                                                                         "event_type" "CALL_ENDED",
                                                                                                         "event_object"
                                                                                                         {"patient_id" "12345678",
                                                                                                          "doctor_enrollment_prov" "C"}})})

  (def logs [{:publisher-config {:type :simple-file, :filename "lad_webhook/events.log"}, :local-time "2024-10-04T10:35:01.175016069", :mulog/namespace "sanatoriocolegiales.lad-webhook.system", :app-name "sanatoriocolegiales lad-webhook service", :mulog/timestamp 1728048901176, :environment :prod, :version "0.1.0", :mulog/trace-id "4zh2JToOXTW4ef0eBjsi_WBQuQvRLaKz", :mulog/event-name :sanatoriocolegiales.lad-webhook.system/log-publish-component}
             {:especificaciones {:dbtype "relativity", :dbname "Maestros", :classname "relativity.jdbc.Driver", :user "ADMIN", :password "", :host "10.200.0.30", :port 1583}, :mulog/namespace "sanatoriocolegiales.lad-webhook.sql.conexiones", :app-name "sanatoriocolegiales lad-webhook service", :mulog/timestamp 1728048901329, :environment :prod, :version "0.1.0", :mulog/trace-id "4zh2JUNs5mUTM6L0MQ8kGQlNl3Sk4dm0", :mulog/event-name :sanatoriocolegiales.lad-webhook.sql.conexiones/connexion-simple-creada, :fecha "2024-10-04T10:35:01.329434876"}
             {:especificaciones {:dbtype "postgres", :dbname "bases_auxiliares", :user "cabboud", :username "cabboud", :password "4Nt01n3.2024", :host "10.200.0.30"}, :mulog/namespace "sanatoriocolegiales.lad-webhook.sql.conexiones", :app-name "sanatoriocolegiales lad-webhook service", :mulog/timestamp 1728048901610, :environment :prod, :version "0.1.0", :mulog/trace-id  "4zh2JVQk82gQQ3OfrcsfqZQB3oaXRK8z", :mulog/event-name :sanatoriocolegiales.lad-webhook.sql.conexiones/connection-pool-creada, :fecha "2024-10-04T10:35:01.609888584"}
             {:especificaciones {:dbtype "postgres", :dbname "desal", :user "desal", :username "desal", :password "desal2016", :host "10.200.0.30"}, :mulog/namespace "sanatoriocolegiales.lad-webhook.sql.conexiones", :app-name "sanatoriocolegiales lad-webhook service", :mulog/timestamp 1728048901749, :environment :prod, :version "0.1.0", :mulog/trace-id "4zh2JVwuxTYTdNT1e9w2ymaZ65GeImcV", :mulog/event-name :sanatoriocolegiales.lad-webhook.sql.conexiones/connection-pool-creada, :fecha "2024-10-04T10:35:01.749106378"}
             {:especificaciones {:dbtype "relativity", :dbname "asistencial", :classname "relativity.jdbc.Driver", :user "ADMIN", :password "", :host "10.200.0.30", :port 1583}, :mulog/namespace "sanatoriocolegiales.lad-webhook.sql.conexiones", :app-name "sanatoriocolegiales lad-webhook service", :mulog/timestamp 1728048901753, :environment :prod, :version "0.1.0", :mulog/trace-id  "4zh2JVxrSywdznil60OzPrH1z3-wfouB", :mulog/event-name :sanatoriocolegiales.lad-webhook.sql.conexiones/connexion-simple-creada, :fecha "2024-10-04T10:35:01.753077066"}
             {:app-name "sanatoriocolegiales lad-webhook service", :version "0.1.0", :environment :prod, :mulog/trace-id  "4zh2Jj4c4YkcxEMsjU6ZyF3sUdAZPSBp", :mulog/timestamp 1728048905543, :mulog/event-name :sanatoriocolegiales.lad-webhook.router/router-app, :mulog/namespace "sanatoriocolegiales.lad-webhook.router", :system-config {:maestros "relativity.jdbc.RelativityConnection@331e9a2a", :bases_auxiliares "HikariDataSource (null)", :desal "HikariDataSource (null)", :asistencial "relativity.jdbc.RelativityConnection@46cd4583", :env :prod}}
             {:local-time "2024-10-04T10:35:05.764193464", :mulog/namespace "sanatoriocolegiales.lad-webhook.system", :app-name "sanatoriocolegiales lad-webhook service", :port 2000, :mulog/timestamp 1728048905764, :environment :prod, :version "0.1.0", :handler "clojure.lang.AFunction$1@26901437", :mulog/trace-id "4zh2JjuBJP-LR1g2N2WQkqHvpsj6qNsd", :mulog/event-name :sanatoriocolegiales.lad-webhook.system/http-server-component}
             {:local-time "2024-10-04T10:41:45.984178605", :mulog/namespace "sanatoriocolegiales.lad-webhook.system", :app-name "sanatoriocolegiales lad-webhook service", :http-server-instance "clojure.lang.AFunction$1@5b755aa4", :mulog/timestamp 1728049305984, :environment :prod, :version "0.1.0", :mulog/trace-id #mulog/flake "4zh2g1q4lWQI0Dnqu6hQzk7rK4k5MM9S", :mulog/event-name :sanatoriocolegiales.lad-webhook.system/http-server-component-shutdown}
             {:publisher  "com.brunobonacci.mulog.core$start_publisher_BANG_$stop_publisher__11673@458812b", :local-time "2024-10-04T10:41:45.999760863", :mulog/namespace "sanatoriocolegiales.lad-webhook.system", :app-name "sanatoriocolegiales lad-webhook service", :mulog/timestamp 1728049305999, :environment :prod, :version "0.1.0", :mulog/trace-id "4zh2g1tm_ZmXDXIlVgx6wX6Gn8sBRE0n", :mulog/event-name :sanatoriocolegiales.lad-webhook.system/log-publish-component-shutdown}])

  (map #(remove #{:especificaciones :system-config} %) logs)

  (map #(dissoc % :especificaciones :system-config) logs)




  :rcf)