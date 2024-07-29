(ns sanatoriocolegiales.lad-webhook.utils.utils
  (:require [clojure.spec.alpha :as spec]
            [hyperfiddle.rcf :refer [tests]]))

(defmacro al-abrir
  "asociaciones => [name init ...]

  Clon del macro with-open. Evalua el cuerpo en una expresion try con los nombres asociados a los valores
  de inicialización y por último clausura esas llamadas (.close name) en cada nombre en orden inverso. Captura
   las excepciones y loggea el mensaje usando la función proporcionada `log-fn`.
   Esta debe ser una función de aridad 1 y su uso es responsabilidad del usuario"
  [asociaciones log-fn & cuerpo]
  (cond
    ((complement vector?) asociaciones) (throw (IllegalArgumentException. "Las asociaciones deben estar en un vector"))
    (odd? (count asociaciones)) (throw (IllegalArgumentException. "Las asociaciones deben ser pares")))
  (cond
    (= (count asociaciones) 0) `(do ~@cuerpo)
    (symbol? (asociaciones 0)) `(let ~(subvec asociaciones 0 2)
                                  (try
                                    (al-abrir ~(subvec asociaciones 2) ~log-fn ~@cuerpo)
                                    (catch java.lang.Exception e# (~log-fn (.getMessage e#)))
                                    (finally 
                                      (. ~(asociaciones 0) close))))
    :else (throw (IllegalArgumentException.
                  "al-abrir solo permite simbolos en asociaciones"))))
 
(tests 
  
 )     

(comment 
 
 

  (defn log 
    [x]
    (println "Excepcion impresa por mi log: "x))

  (let [asociaciones [1 22]
        log-fn  #(println %)]
    (cond
      ((complement vector?) asociaciones) (throw (IllegalArgumentException. "Las asociaciones deben estar en un vector"))
      (odd? (count asociaciones)) (throw (IllegalArgumentException. "Las asociaciones deben ser pares"))
      ((complement fn?) log-fn) (throw (IllegalArgumentException. "Debe agregar una función que invoque su función de loggeo"))))
  
  (al-abrir [r (clojure.java.io/reader "cosa.txt")] (fn [x] (println "Excepcion impresa por mi log: " x))
              (throw (java.io.IOException. "Excepcion pajúa")))
  
  (al-abrir [r (clojure.java.io/reader "cosa.txt")] log
            (throw (java.io.IOException. "Excepcion pajúa")))

  (fn? `log)  
  (fn? `(fn [x] (println x)))

  
  

  ) 