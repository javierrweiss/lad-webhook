(ns sanatoriocolegiales.lad-webhook.historiasclinicas.utils
  (:require [hyperfiddle.rcf :refer [tests]] 
            [java-time.api :as jt]
            [clojure.string :as string]
            [clojure.core.match.regex]
            [clojure.core.match :refer [match]]))

(defn- split-date-&-time 
  [fecha-hora]
  (as-> fecha-hora f
    (string/split f #"\s")
    (map #(string/split % #"-|:|/") f)))

(defn- instant-to-number
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

(defn extraer-fecha-y-hora
  [fecha] 
  (match [fecha]
    [#"(\d{4}(-)\d{2}(-)\d{2})\s{1,}\d{2}:\d{2}|(\d{4}(/)\d{2}(/)\d{2})\s{1,}\d{2}:\d{2}"] (instant-to-number fecha)
    [#"(\d{4}(-)\d{1}(-)\d{2})\s{1,}\d{2}:\d{2}|(\d{4}(/)\d{1}(/)\d{2})\s{1,}\d{2}:\d{2}"] (let [[[yyyy month dd] [hh min]] (split-date-&-time fecha)]
                                                                                             (mapv Integer/parseInt [(str yyyy "0" month dd) (str hh min)]))
    [#"(\d{2}(-)\d{2}(-)\d{4})\s{1,}\d{2}:\d{2}|(\d{2}(/)\d{2}(/)\d{4})\s{1,}\d{2}:\d{2}"] (let [[[dd month yyyy] [hh min]] (split-date-&-time fecha)]
                                                                                             (mapv Integer/parseInt [(str yyyy month dd) (str hh min)]))
    [#"(\d{2}(-)\d{1}(-)\d{4})\s{1,}\d{2}:\d{2}|(\d{2}(/)\d{1}(/)\d{4})\s{1,}\d{2}:\d{2}"] (let [[[dd month yyyy] [hh min]] (split-date-&-time fecha)]
                                                                                             (mapv Integer/parseInt [(str yyyy "0" month dd) (str hh min)]))
    :else (instant-to-number (str (jt/instant)))))

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
    (->> hora str (drop 2) (take 2) (apply str) (Integer/parseInt))))

(comment

  (let [fecha #_"2025-12-02 12:13" #_"2025/02/12 14:49" #_"02/11/2028 12:45" #_"10-08-2025 02:11" #_"12-2-2028 12:56" "2024/6/28 10:09"]
    (match [fecha]
      [#"(\d{4}(-)\d{2}(-)\d{2})\s{1,}\d{2}:\d{2}|(\d{4}(/)\d{2}(/)\d{2})\s{1,}\d{2}:\d{2}"] (instant-to-number fecha)
      [#"(\d{4}(-)\d{1}(-)\d{2})\s{1,}\d{2}:\d{2}|(\d{4}(/)\d{1}(/)\d{2})\s{1,}\d{2}:\d{2}"] (let [[[yyyy month dd] [hh min]] (split-date-&-time fecha)]
                                                                                               (mapv Integer/parseInt [(str yyyy "0" month dd) (str hh min)]))
      [#"(\d{2}(-)\d{2}(-)\d{4})\s{1,}\d{2}:\d{2}|(\d{2}(/)\d{2}(/)\d{4})\s{1,}\d{2}:\d{2}"] (let [[[dd month yyyy] [hh min]] (split-date-&-time fecha)]
                                                                                               (mapv Integer/parseInt [(str yyyy month dd) (str hh min)]))
      [#"(\d{2}(-)\d{1}(-)\d{4})\s{1,}\d{2}:\d{2}|(\d{2}(/)\d{1}(/)\d{4})\s{1,}\d{2}:\d{2}"] (let [[[dd month yyyy] [hh min]] (split-date-&-time fecha)]
                                                                                               (mapv Integer/parseInt [(str yyyy "0" month dd) (str hh min)]))
      :else (instant-to-number (str (jt/instant)))))

  
  
  (let [[[dd month yyyy] [hh min]] (as-> "10-08-2025 02:11" f
                                     (string/split f #"\s")
                                     (map #(string/split % #"-|:") f))]
    (mapv Integer/parseInt [(str yyyy month dd) (str hh min)]))



  (extraer-fecha-y-hora "2025/8/22 15:06")
  (extraer-fecha-y-hora "2025-05-12   12:43")
  (extraer-fecha-y-hora "2026/03/25 16.37")
  (extraer-fecha-y-hora "2026/4/13 17:43")

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

  (obtener-hora 12000000)
  (obtener-minutos 16530000)
  )  

(tests
 (extraer-fecha-y-hora "2024-07-12T01:17:19.813Z") := (vec (instant-to-number (str (jt/instant))))
 (extraer-fecha-y-hora "2024/11/10 10:40") := [20241110 1040]
 (extraer-fecha-y-hora "2024/12/31 10:40pm") := (vec (instant-to-number (str (jt/instant))))
 (extraer-fecha-y-hora "2024-01-23 00:01") := [20240123 1]
 (extraer-fecha-y-hora "2024-08-23 01:01") := [20240823 101]
 (extraer-fecha-y-hora "2024-08-23 00:00") := [20240823 0]
 (extraer-fecha-y-hora "2025-05-12   12:43") := [20250512 1243]
 (extraer-fecha-y-hora "2030-05-12  10:43") := [20300512 1043]
 (extraer-fecha-y-hora "2030-10-12        10:11") := [20301012 1011]
 (extraer-fecha-y-hora "12-12-2025 12:34") := [20251212 1234]
 (extraer-fecha-y-hora "09/05/2029 06:05") := [20290509 605]
 (extraer-fecha-y-hora "10/5/2029 16:05") := [20290510 1605]
 (extraer-fecha-y-hora "2021-3-16 06:45") := [20210316 645]

 (obtener-hora-finalizacion 1140 20) := 1200
 (obtener-hora-finalizacion 1155 20) := 1215
 (obtener-hora-finalizacion 859 15) := 914
 (obtener-hora-finalizacion 0 20) := 20
 (obtener-hora-finalizacion 101 20) := 121
 (obtener-hora-finalizacion 59 25) := 124
 (obtener-hora-finalizacion 2350 15) := 5

 (obtener-hora "Camba") :throws clojure.lang.ExceptionInfo
 (obtener-hora 12000000) := 12
 (obtener-minutos "dae qefs arwef") :throws clojure.lang.ExceptionInfo
 (obtener-minutos 16530000) := 53
 (obtener-minutos 12000000) := 0)     