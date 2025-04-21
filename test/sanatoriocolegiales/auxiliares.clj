(ns sanatoriocolegiales.auxiliares
  (:require [honey.sql :as sql])
  (:import [java.time LocalDateTime]))

(defn obtener-fecha-y-hora-aleatoria []
  (let [year (rand-nth ["2023" "2024" "2025" "2026" "2027" "2028" "2029"])
        month (rand-nth ["01" "02" "03" "04" "05" "06" "07" "08" "09" "10" "11" "12"])
        day (rand-nth ["01" "02" "03" "04" "05" "06" "07" "08" "09" "10" "11" "12"
                       "13" "14" "15" "16" "17" "18" "19" "20" "21" "22"
                       "23" "24" "25" "26" "27" "28"])
        hour (rand-nth ["00" "01" "02" "03" "04" "05" "06" "07" "08" "09"
                       "10" "11" "12" "13" "14" "15" "16" "17" "18"
                       "19" "20" "21" "22" "23"])
        minute (rand-nth ["00" "01" "02" "03" "04" "05" "06" "07" "08" "09"
                          "10" "11" "12" "13" "14" "15" "16" "17" "18"
                          "19" "20" "21" "22" "23" "24" "25" "26" "27"
                          "28" "29" "30" "31" "32" "33" "34" "35"
                          "36" "37" "38" "39" "40" "41" "42" "43"
                          "44" "45" "46" "47" "48" "49" "50"
                          "51" "52" "53" "54" "55" "56" "57" "58" "59"])] 
    [(Integer/parseInt 
       (str year month day))
     (Integer/parseInt 
      (str hour minute))]))

(defn sql-insertar-registro-en-reservas
  [hc]
  (let [[fecha hora] (obtener-fecha-y-hora-aleatoria)
        benef (str (rand-int 1000000))
        obra (rand-int 10000)
        plan (str (rand-int 1000))
        esp (rand-int 10000)]
    (sql/format {:insert-into :tbc_reservas
                 :columns [:reservashiscli
                           :reservasfech
                           :reservashora
                           :reservasnroben
                           :reservasobra
                           :reservasobrpla
                           :reservasmed
                           :reservasesp
                           :reservastipo
                           :reservasletrafc
                           :reservastipsol
                           :reservasllamadaestado
                           :reservasmedicab
                           :reservasautoriza
                           :reservasnordeges]
                 :values [[hc fecha hora benef obra plan 999880 esp 1 "F" "A" "S" "cualquier" "cualquier" "eges"]]})))


(comment
  
  (obtener-fecha-y-hora-aleatoria)
  (.getHour (LocalDateTime/now))
  (.getMinute (LocalDateTime/now))
  (sql-insertar-registro-en-reservas 1323)
  
  )