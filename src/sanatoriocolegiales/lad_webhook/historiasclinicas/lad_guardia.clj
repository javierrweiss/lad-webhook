(ns sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia
  (:require [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [inserta-en-tbc-histpac 
                                                                    inserta-en-tbc-histpac-txt 
                                                                    actualiza-tbc-guardia]]
            [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta! obtiene-numerador!]]
            [hyperfiddle.rcf :refer [tests]]))

(defn prepara-registros
  "Adapta el mapa que viene del request y devuelve un vector con tres registros (también vectores) listos para ser persistidos"
  [{:keys [guar-hist-clinica guar-fecha-ingreso guar-hora-ingreso hora_final_atencion fecha_inicio_atencion guar-obra guar-plan diagnostico motivo historia nombre patologia histpactratam]}] 
  [[guar-hist-clinica guar-fecha-ingreso guar-hora-ingreso diagnostico hora_final_atencion fecha_inicio_atencion]
   [guar-hist-clinica guar-fecha-ingreso]
   [guar-hist-clinica guar-fecha-ingreso guar-hora-ingreso historia motivo "" ""]])

(defn crea-historia-clinica
  "Persiste 3 registros a sus respectivas tablas. Recibe una conexión y tres vectores con los datos a ser persistidos"
  [db registro-guardia registro-historia-paciente registro-historia-texto]
  (let [#_#_maestros (-> db :maestros)
        #_#_desal (-> db :desal)
        asistencial (-> db :asistencial)]
    (future (ejecuta! asistencial (inserta-en-tbc-histpac registro-historia-paciente)))
    (future (ejecuta! asistencial (inserta-en-tbc-histpac-txt registro-historia-texto)))
    (future (ejecuta! asistencial (actualiza-tbc-guardia registro-guardia)))))

(defn persiste-historia-clinica
  "Toma la información del paciente y crea la historia clínica. Recibe el request y la conexión a la BD."
  [db paciente]
  (let [histpactratam (obtiene-numerador! db)
        [guardia hc hc-texto] (prepara-registros (assoc paciente :histpactratam histpactratam))]
    (crea-historia-clinica db guardia hc hc-texto)))

(comment

  (let [m {:a 1
           :z {:b 12 :c 332}}]
    (merge (select-keys (:z m) [:c]) (select-keys m [:a]))))

(tests)