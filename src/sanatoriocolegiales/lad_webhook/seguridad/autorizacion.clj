(ns sanatoriocolegiales.lad-webhook.seguridad.autorizacion
  (:require [com.brunobonacci.mulog :as mulog]
            [clojure.java.io :as io]
            [aero.core :refer [read-config]])
  (:import java.time.LocalDateTime)) 

(defn autenticar-y-autorizar-solicitud
  [{{:strs [client_id client_secret]} :query-params} env]
  (mulog/log ::autenticando-cliente :fecha (LocalDateTime/now)) 
  (let [conf (read-config (io/resource "config.edn") {:profile env})
        cliente  (:client-id conf) 
        passwd   (:client-secret conf)]
    (when (and (= cliente client_id) (= passwd client_secret))
      true)))
 
