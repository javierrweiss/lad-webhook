(ns sanatoriocolegiales.lad-webhook.historiasclinicas.utils
  (:require [hyperfiddle.rcf :refer [tests]] 
            [java-time.api :as jt]))

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
  [hora-inicio duracion]
  (let [hhmm (if (< hora-inicio 1000)
               (cond
                 (== 0 hora-inicio) [0 0]
                 (< hora-inicio 100) [0 hora-inicio]
                 :else (let [[hora & minutos] (str hora-inicio)]
                         [(Integer/parseInt (str hora)) (->> minutos (apply str) (Integer/parseInt))]))
               (transduce
                (comp
                 (partition-all 2)
                 (map #(apply str %))
                 (map #(Integer/parseInt %)))
                conj [] (str hora-inicio)))
        hora (first hhmm)
        minutos (second hhmm)
        hora-final (as-> (jt/local-time hora minutos) h
                     (jt/+ h (jt/minutes duracion))
                     (.toString h)
                     (filter (complement #{\:}) h))]
    (Integer/parseInt (apply str hora-final))))
 
(defn obtener-hora
  [hora]
  (if-not (number? hora)
    (throw (ex-info "La hora no es un número" {:hora hora}))
    (->> hora str (take 2) (apply str) (Integer/parseInt))))

(defn obtener-minutos
  [hora]
  (if-not (number? hora)
    (throw (ex-info "La hora no es un número" {:hora hora}))
    (->> hora str (drop 2) (apply str) (Integer/parseInt))))

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
  
  (->> (str 1823) (partition-all 2) (map #(apply str %)) (map #(Integer/parseInt %)))

  (transduce (comp (partition-all 2) (map #(apply str %)) (map #(Integer/parseInt %))) conj [] (str 1156))
    
   (let [[hour & minutes] (str 156)]
     [(Integer/parseInt (str hour)) (->> minutes (apply str) (Integer/parseInt))])
  )  

(tests 
 (extraer-fecha-y-hora "2024-07-12T01:17:19.813Z") := [20240712 117] 
 (extraer-fecha-y-hora "2024/11/10 10:40") := [20241110 1040]
 (extraer-fecha-y-hora "2024/12/31 10:40pm") := [20241231 1040]
 (extraer-fecha-y-hora "2024-01-23 00:01") := [20240123 1]
 (extraer-fecha-y-hora "2024-08-23 01:01") := [20240823 101]
 (extraer-fecha-y-hora "2024-08-23 00:00") := [20240823 0] 

 (obtener-hora-finalizacion 1140 20) := 1200
 (obtener-hora-finalizacion 1155 20) := 1215
 (obtener-hora-finalizacion 859 15) := 914
 (obtener-hora-finalizacion 0 20) := 20 
 (obtener-hora-finalizacion 101 20) := 121
 (obtener-hora-finalizacion 59 25) := 124
 (obtener-hora-finalizacion 2350 15) := 5

 (obtener-hora "Camba") :throws clojure.lang.ExceptionInfo
 (obtener-minutos "dae qefs arwef") :throws clojure.lang.ExceptionInfo
 )     