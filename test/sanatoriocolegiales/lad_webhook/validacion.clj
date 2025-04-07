(ns sanatoriocolegiales.lad-webhook.validacion
  (:require [sanatoriocolegiales.lad-webhook.seguridad.validacion :as validacion]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.spec.alpha :as spec]
            [sanatoriocolegiales.lad-webhook.especificaciones.especificaciones :as especificaciones]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test :refer [deftest is testing use-fixtures run-test run-all-tests]]
            [clojure.spec.gen.alpha :as gen]))

(defspec valida-eventos-formados-correctamente
  100
  (prop/for-all [call_ended (spec/gen :call_ended/event_object)]
                (map? (validacion/valida-event-object {:event_object call_ended}))))



(comment
  
(run-test valida-eventos-formados-correctamente)
 
  (gen/sample (gen/map (gen/elements ["clojure" "haskell" "erlang" "scala" "python"]) (gen/int)))
  )
  