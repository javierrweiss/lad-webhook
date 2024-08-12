(ns sanatoriocolegiales.lad-webhook.historiasclinicas.utils
  (:require [hyperfiddle.rcf :refer [tests]]
            [clojure.string :as s]))

(defn extraer-fecha-y-hora
  [date-str]
  (eduction
   (comp
    (filter (complement #{\T \Z \- \: \. \/ \space}))
    (take 12)
    (partition-all 8)
    (map (fn [ls]
           (->> ls
                (apply str)
                (Integer/parseInt)))))
   date-str))

(defn obtener-hora-finalizacion
  [hora-inicio duracion])

(defn obtener-hora
  [hora]
  (->> hora str (take 2) (apply str) (Integer/parseInt)))

(defn obtener-minutos
  [hora]
  (->> hora str (drop 2) (apply str) (Integer/parseInt)))


(comment
  
  (eduction 
   (comp 
    (filter (complement #{\T \Z \- \: \.})) 
    (take 12)
    (partition-all 8)
    (map (fn [ls]
           (->> ls
                (apply str)
                (Integer/parseInt)))))  
   "2024-07-12T01:17:19.813Z")
  
   (filter (complement #{\T \Z \- \: \. \space}) "2024-07-12T01:17:19.813Z 122") 
  
  
  )

(tests 
 (extraer-fecha-y-hora "2024-07-12T01:17:19.813Z") := [20240712 117] 
 (extraer-fecha-y-hora "2024/11/10 10:40") := [20241110 1040]
 (extraer-fecha-y-hora "2024/12/31 10:40pm") := [20241231 1040]
 (extraer-fecha-y-hora "2024-01-23 00:01") := [20240123 1]
 (extraer-fecha-y-hora "2024-08-23 01:01") := [20240823 101]
 (extraer-fecha-y-hora "2024-08-23 00:00") := [20240823 0] 
 )   