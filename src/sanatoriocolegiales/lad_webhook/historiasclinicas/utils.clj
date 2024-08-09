(ns sanatoriocolegiales.lad-webhook.historiasclinicas.utils)

(defn extraer-fecha-y-hora
  [date-str])

(defn obtener-hora-finalizacion
  [hora-inicio duracion])

(defn obtener-hora
  [hora]
  (->> hora str (take 2) (apply str) (Integer/parseInt)))

(defn obtener-minutos
  [hora]
  (->> hora str (drop 2) (apply str) (Integer/parseInt)))