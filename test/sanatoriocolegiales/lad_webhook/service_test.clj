(ns sanatoriocolegiales.lad-webhook.service-test
  (:require [clojure.test :refer [deftest is testing]]
            [sanatoriocolegiales.lad-webhook.service :as lad-webhook]))

(deftest service-test
  (testing "TODO: Start with a failing test, make it pass, then refactor"

    ;; TODO: fix greet function to pass test
    (is (= "sanatoriocolegiales lad-webhook service developed by the secret engineering team"
           (lad-webhook/greet)))

    ;; TODO: fix test by calling greet with {:team-name "Practicalli Engineering"}
    (is (= (lad-webhook/greet "Practicalli Engineering")
           "sanatoriocolegiales lad-webhook service developed by the Practicalli Engineering team"))))
