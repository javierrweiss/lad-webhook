(ns sanatoriocolegiales.lad-webhook.seguridad.validacion
  (:require [buddy.sign.jwt :as jwt]))

(defn validar
  [req]
  (constantly true))