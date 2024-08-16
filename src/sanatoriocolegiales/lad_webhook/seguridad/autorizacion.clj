(ns sanatoriocolegiales.lad-webhook.seguridad.autorizacion
  (:require [com.brunobonacci.mulog :as mulog]
            [sanatoriocolegiales.lad-webhook.system :refer [conf]])
  (:import java.time.LocalDateTime)) 

(defn autenticar-y-autorizar-solicitud
  [{{:strs [client_id client_secret]} :query-params}]
  (mulog/log ::autenticando-cliente :fecha (LocalDateTime/now)) 
  (let [cliente (:client-id conf)
        passwd (:client-secret conf)]
    (when (and (= cliente client_id) (= passwd client_secret))
      true)))
 