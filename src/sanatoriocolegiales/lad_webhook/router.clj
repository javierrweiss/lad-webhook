(ns sanatoriocolegiales.lad-webhook.router
  "API global request routing"
  (:require
   ;; Core Web Application Libraries
   [reitit.ring   :as ring]
   [muuntaja.core :as muuntaja]
   ;; Routing middleware
   [reitit.ring.middleware.muuntaja   :as middleware-muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   ;; Service middleware
   [sanatoriocolegiales.lad-webhook.middleware :as middleware-service]
   [sanatoriocolegiales.lad-webhook.error.error :refer [exception-middleware]]
   ;; Service Routing
   [sanatoriocolegiales.lad-webhook.api.system-admin :as system-admin]
   [sanatoriocolegiales.lad-webhook.api.atencion-guardia :as atencion-guardia]
   ;; Self-documenting API
   [reitit.swagger    :as api-docs]
   [reitit.swagger-ui :as api-docs-ui]
   ;; Provide details of parameters to API documentation UI (swagger)
   [reitit.coercion.spec]
   [reitit.ring.coercion :as coercion]
   ;; Error handling
   [reitit.dev.pretty :as pretty]
   [com.brunobonacci.mulog :as mulog]  ; Event Logging 
   ))

;; --------------------------------------------------
;; Open API documentation endpoint

(def open-api-docs 
  ["/swagger.json"
   {:get {:no-doc  true
          :swagger {:info {:title "sanatoriocolegiales lad-webhook Service API"
                           :description "Webhook para servicios de teleconsultas"
                           :version "0.1.0" 
                           :license {:name "© Jrivero, 2024"
                                     :url "http://creativecommons.org/licenses/by-sa/4.0/"}
                           :x-logo {:url "./favicon.png"}}}
          :handler (api-docs/create-swagger-handler)}}])
;; --------------------------------------------------


;; --------------------------------------------------
;; Global route Configuration
;; - coersion and middleware applied to all routes

(def router-configuration
  "Reitit configuration of coercion, data format transformation and middleware for all routing"
  {:data {:coercion   reitit.coercion.spec/coercion
          :muuntaja   muuntaja/instance
          :middleware [;; swagger feature for OpenAPI documentation
                       api-docs/swagger-feature
                       ;; query-params & form-params
                       parameters/parameters-middleware
                       ;; content-negotiation
                       middleware-muuntaja/format-middleware
                       ;; exceptions
                       exception-middleware
                       ;; coercing response bodys
                       coercion/coerce-response-middleware
                       ;; coercing request parameters
                       coercion/coerce-request-middleware
                       ;; Pretty print exceptions
                       coercion/coerce-exceptions-middleware
                       ;; logging with mulog
                       [middleware-service/wrap-trace-events :trace-events]]}
   ;; pretty-print reitit exceptions for human consumptions
   :exception pretty/exception})

;; --------------------------------------------------
;; Routing

(defn app
  "Router for all requests to the Guardia and OpenAPI documentation,
  using `ring-handler` to manage HTTP request and responses.
  Arguments: `system-config containt Donut configuration for the running system
  including persistence connection to store and retrieve data"
  [system-config]

  (mulog/log ::router-app :system-config system-config)

  (ring/ring-handler
   (ring/router
    [;; --------------------------------------------------
     ;; All routing for service

     ;; OpenAPI Documentation routes
     open-api-docs

     ;; --------------------------------------------------
     ;; System routes & Status
     ;; - `/system-admin/status` for simple service healthcheck
     (system-admin/routes)

     ;; --------------------------------------------------
     ;; sanatoriocolegiales lad-webhook API routes
     ["/api"
      ["/v1"
       (atencion-guardia/routes system-config)]]]

    ;; End of All routing for Guardia service
    ;; --------------------------------------------------

    ;; --------------------------------------------------
    ;; Router configuration
    ;; - middleware, coersion & content negotiation
    router-configuration)

   ;; --------------------------------------------------
   ;; Default routes
   (ring/routes
    ;; Open API documentation as default route
    (api-docs-ui/create-swagger-ui-handler {:path "/"})

    ;; Respond to any other route - returns blank page
    ;; TODO: create page template for routes not recognised
    (ring/create-default-handler))))
