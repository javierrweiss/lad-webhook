(ns sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia
  (:require [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [inserta-en-tbc-histpac inserta-en-tbl-hist-txt actualiza-tbc-guardia]]
            [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta!]]))

(defn- request->registros
  "Adapta el mapa que viene del request y devuelve un vector con tres registros (también vectores) listos para ser persistidos"
  [request-body]
  (let [main-map (merge 
                  (select-keys (:event_object request-body) [:call_diagnosis 
                                                             :call_cie10 
                                                             :call_motive
                                                             :call_doctor_comment
                                                             :patient_name
                                                             :patient_external_id]) 
                  (select-keys request-body [:paciente
                                             :fecha
                                             :hora]))
        hc (get-in main-map [:patient_external_id :uid])]
    [nil nil nil]))

(defn- crea-historia-clinica
  "Persiste 3 registros a sus respectivas tablas. Recibe una conexión y tres vectores con los datos a ser persistidos"
  [db registro-guardia registro-historia-paciente registro-historia-texto]
  (let [maestros (-> db :maestros)
        desal (-> db :desal)
        asistencial (-> db :asistencial)]
    (ejecuta! asistencial (inserta-en-tbc-histpac registro-historia-paciente))
    (ejecuta! desal (inserta-en-tbl-hist-txt registro-historia-texto))
    (ejecuta! asistencial (actualiza-tbc-guardia registro-guardia))))

(defn persiste-historia-clinica
  "Toma el request y crea la historia clínica del paciente. Recibe el request y la conexión a la BD."
  [db request-body]
  (let [[guardia hc hc-texto] (request->registros request-body)]
    (crea-historia-clinica db guardia hc hc-texto)))

(comment
  
  (let [m {:a 1
           :z {:b 12 :c 332}}]
    (merge (select-keys (:z m) [:c]) (select-keys m [:a]))) 
  
  )