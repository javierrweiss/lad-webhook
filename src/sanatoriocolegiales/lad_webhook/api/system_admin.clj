(ns sanatoriocolegiales.lad-webhook.api.system-admin
  (:require [ring.util.response :refer [response]]))

(def status
  "Simple status report for external monitoring services, e.g. Pingdom
  Return:
  - `constantly` returns an anonymous function that returns a ring response hash-map"
  (constantly (response {:application "sanatoriocolegiales lad-webhook Service" :status "Alive"})))

(defn routes
  "Reitit route configuration for system-admin endpoint"
  []
  ["/system-admin"
   {:swagger {:tags ["Application Support"]}}
   ["/status"
    {:get {:summary "Status of sanatoriocolegiales lad-webhook service"
           :description "Ping sanatoriocolegiales lad-webhook service to see if is responding to a simple request and therefore alive"
           :responses {200 {:body {:application string? :status string?}}}
           :handler status}}]])

