(ns sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia
  (:require [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [inserta-en-tbc-histpac 
                                                                    inserta-en-tbc-histpac-txt 
                                                                    actualiza-tbc-guardia]]
            [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta! obtiene-numerador!]] 
            [sanatoriocolegiales.lad-webhook.historiasclinicas.utils :refer [obtener-hora 
                                                                             obtener-minutos
                                                                             extraer-fecha-y-hora 
                                                                             obtener-hora-finalizacion]]))

(defn extraer-event-object
  [{:keys [call_diagnosis
           call_cie10
           call_motive
           call_doctor_comment
           call_duration
           call_start_datetime
           order_id  ;; Acá recibiremos fecha y hora
           doctor_name
           doctor_enrollment_type
           patient_name
           patient_external_id ;; Acá recibimos el número de HC
           ]}]
  (let [[fecha hora] (extraer-fecha-y-hora order_id)
        [fecha-ini hora-ini] (extraer-fecha-y-hora call_start_datetime)
        hora-finalizacion (obtener-hora-finalizacion hora-ini call_duration)]
    {:fecha fecha
     :hora hora
     :fecha-inicio-atencion fecha-ini
     :hora-inicio-atencion hora-ini
     :hora-final-atencion hora-finalizacion
     :diagnostico call_diagnosis
     :matricula (Integer/parseInt doctor_enrollment_type)
     :medico doctor_name
     :motivo call_motive
     :historia call_doctor_comment
     :nombre patient_name
     :hc (Integer/parseInt patient_external_id)
     :patologia call_cie10}))

(defn prepara-registros
  "Adapta el mapa que viene del request y devuelve un vector con tres registros (también vectores) listos para ser persistidos"
  [{:keys [guar-hist-clinica 
           guar-fecha-ingreso 
           guar-hora-ingreso
           hora-inicio-atencion
           hora-final-atencion 
           fecha-inicio-atencion 
           guar-obra 
           guar-plan
           guar-nroben
           diagnostico  
           historia  
           patologia 
           histpactratam
           histpacmotivo 
           medico
           matricula]}] 
 (let [hora (obtener-hora guar-hora-ingreso)
       minutos (obtener-minutos guar-hora-ingreso)
       hora-fin (obtener-hora hora-final-atencion)
       minutos-fin (obtener-minutos hora-final-atencion)
       hora-ini (obtener-hora hora-inicio-atencion)
       minutos-ini (obtener-minutos hora-inicio-atencion)
       cod-patol 9]
   [[guar-hist-clinica guar-fecha-ingreso guar-hora-ingreso cod-patol hora-final-atencion fecha-inicio-atencion]
   [guar-hist-clinica 
    guar-fecha-ingreso 
    hora 
    minutos 
    0 
    2
    0 ;; especialidad por definir
    guar-hist-clinica
    guar-fecha-ingreso
    guar-hist-clinica
    0 ;; especialidad por definir
    0 ;; código de médico por definir 
    0
    0
    0
    hora-fin
    minutos-fin
    0
    diagnostico
    patologia ;; patologia por definir
    histpactratam
    medico
    matricula
    hora-ini
    minutos-ini
    0
    0
    0
    ""
    guar-obra
    guar-plan
    guar-plan
    guar-nroben 
    0
    ""
    0
    0
    0
    "N"
    0]
   [histpactratam historia histpacmotivo diagnostico medico matricula]]))

(defn guarda-texto-de-historia
  ([conn numerador texto]
   (guarda-texto-de-historia conn numerador texto nil nil))
  ([conn numerador texto medico matricula]
   (let [len (count texto)
         textos (if (> len 77)
                  (->> (partition-all 77 texto)
                       (map #(apply str %)))
                  [texto])
         cantidad (count textos)
         contador (atom 1)]
     (ejecuta! conn (inserta-en-tbc-histpac-txt [numerador 1 0 0 "" cantidad]))
     (doseq [text textos :let [t (if-not medico (str "Diagnostico: " text) text)]]
       (ejecuta! conn (inserta-en-tbc-histpac-txt [numerador 1 @contador @contador t 0]))
       (swap! contador inc))
     (when medico
       (ejecuta!
        conn
        (inserta-en-tbc-histpac-txt [numerador 1 (inc @contador) (inc @contador) (str "Profesional: " medico " Matricula: " matricula) cantidad]))))))
 
(defn crea-historia-clinica
  "Persiste 4 registros a sus respectivas tablas. Recibe una conexión y tres vectores con los datos a ser persistidos"
  [db registro-guardia registro-historia-paciente registro-historia-texto] 
  (future (ejecuta! db (inserta-en-tbc-histpac registro-historia-paciente)))
  (future (apply guarda-texto-de-historia db (take 2 registro-historia-texto)))
  (future (apply guarda-texto-de-historia db (drop 2 registro-historia-texto)))
  (future (ejecuta! db (actualiza-tbc-guardia registro-guardia))))

(defn persiste-historia-clinica
  "Toma la información del paciente y crea la historia clínica. Recibe el request y la conexión a la BD."
  [db paciente]
  (let [histpactratam (obtiene-numerador! (:desal db))
        histpacmotivo (obtiene-numerador! (:desal db))
        [guardia hc hc-texto] (prepara-registros (assoc paciente :histpactratam histpactratam :histpacmotivo histpacmotivo))]
    @(crea-historia-clinica (:asistencial db) guardia hc hc-texto)))


(comment
(let [asistencial (-> (system-repl/system) :donut.system/instances :conexiones :asistencial)
      desal (-> (system-repl/system) :donut.system/instances :conexiones :desal) 
      req {:guar-hist-clinica 182222,
           :patologia "H92",
           :hora-inicio-atencion 112,
           :medico "Amezqueta Marcela",
           :guar-obra 1820,
           :guar-plan "4000",
           :matricula 123456,
           :hc 182222,
           :hora-final-atencion 343,
           :guar-fecha-ingreso 20240808,
           :diagnostico "Otalgia y secreción del oído",
           :guar-nroben "",
           :fecha-inicio-atencion 20240712,
           :historia
           "FIEBRE DESDE HOY, REFIERE OTALGIA ESTUVO EN PISCINA Y MAR? DICE NO MOCO NI TOS CONTROL PRESENCIAL PARA OTOSCOPIA OTITIS EXTERNA O MEDIA?",
           :nombre "John Doe",
           :guar-hora-ingreso 1300,
           :motivo "Fiebre / Sin otros síntomas mencionados"}
      [_ reg] (prepara-registros req)]
  (prepara-registros req)
  #_@(future (ejecuta! asistencial (inserta-en-tbc-histpac reg)))
  #_(persiste-historia-clinica {:asistencial asistencial
                              :desal desal} req))

(let [asistencial (-> (system-repl/system) :donut.system/instances :conexiones :asistencial)
      registro [182222 20240808 1300 9 343 20240712]]
  #_(actualiza-tbc-guardia registro)
  (ejecuta! asistencial (actualiza-tbc-guardia registro)))

:rcf)
