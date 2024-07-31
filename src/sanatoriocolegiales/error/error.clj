(ns sanatoriocolegiales.error.error)

(defn lanza-error
  [^Throwable err]
  (assoc (ex-data err)
         :error (ex-message err)))