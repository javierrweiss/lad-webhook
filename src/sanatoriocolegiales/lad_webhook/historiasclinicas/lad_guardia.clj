(ns sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia
  (:require
   [manifold.deferred :as d]
   [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [inserta-en-tbc-histpac
                                                           inserta-en-tbc-histpac-txt
                                                           actualiza-tbc-guardia
                                                           busca-en-tbc-patologia]]
   [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta! obtiene-numerador!]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.utils :refer [obtener-hora
                                                                    obtener-minutos
                                                                    extraer-fecha-y-hora
                                                                    obtener-hora-finalizacion]]
   [hyperfiddle.rcf :refer [tests]]
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(defn extraer-event-object
  "Recibe el event-object del request y devuelve un mapa con los datos pre-procesados"
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
           descripcion-patologia
           ;;patologia // Se ignora cie10 y se introduce código fijo: 3264
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
        nro-afiliado (let [len (count guar-nroben)]
                       (if (> len 15)
                         (->> guar-nroben (take 15) (apply str))
                         guar-nroben))
        cod-patol 9]
    [;; tbc_guardia
     [guar-hist-clinica guar-fecha-ingreso guar-hora-ingreso cod-patol hora-final-atencion fecha-inicio-atencion]
    ;; tbc_histpac
     [guar-hist-clinica
      guar-fecha-ingreso
      hora
      minutos
      0
      2
      407
      guar-hist-clinica
      guar-fecha-ingreso
      guar-hist-clinica
      407
      999880
      0
      0
      0
      hora-fin
      minutos-fin
      0
      descripcion-patologia 
      3264
      histpactratam
      medico
      matricula
      hora-ini
      minutos-ini
      0
      0
      0
      ""
      (or guar-obra 0)
      (or guar-plan "")
      ""
      nro-afiliado
      0
      ""
      0
      0
      0
      "N"
      0]
   ;; tbc_histpac_txt
     [histpactratam historia histpacmotivo diagnostico medico matricula]]))

(defn guarda-texto-de-historia
  ([conn numerador texto]
   (guarda-texto-de-historia conn numerador texto nil nil))
  ([conn numerador texto medico matricula]
   (let [len (count texto)
         textos (if (> len 77)
                  (->> (partition-all 60 texto)
                       (map #(apply str %)))
                  [texto])
         profesional-y-matricula (let [s (str "Profesional: " medico " Matricula: " matricula)
                                       len (count s)]
                                   (if (> len 77)
                                     (->> s (take 77) (apply str))
                                     s))
         cantidad (count textos)
         contador (atom 1)]
     (ejecuta! conn (inserta-en-tbc-histpac-txt [numerador 1 0 0 "" cantidad]))
     (doseq [text textos :let [t (if-not medico (str "Diagnostico: " text) text)]]
       (ejecuta! conn (inserta-en-tbc-histpac-txt [numerador 1 @contador @contador t 0]))
       (swap! contador inc))
     (when medico
       (ejecuta!
        conn
        (inserta-en-tbc-histpac-txt [numerador 1 (inc @contador) (inc @contador) profesional-y-matricula cantidad]))))))

(defn crea-historia-clinica!
  "Persiste 4 registros a sus respectivas tablas. Recibe una conexión y tres vectores con los datos a ser persistidos"
  [db registro-guardia registro-historia-paciente registro-historia-texto]
  (try
    (d/zip
     (d/future (ejecuta! db (inserta-en-tbc-histpac registro-historia-paciente)))
     (d/future (apply guarda-texto-de-historia db (take 2 registro-historia-texto)))
     (d/future (apply guarda-texto-de-historia db (drop 2 registro-historia-texto)))
     (d/future (ejecuta! db (actualiza-tbc-guardia registro-guardia))))
    (catch Exception e (throw e))))

(defn persiste-historia-clinica!
  "Toma la información del paciente y crea la historia clínica. Recibe el request y la conexión a la BD."
  [db paciente]
  @(d/let-flow [histpactratam (obtiene-numerador! (:desal db))
                histpacmotivo (obtiene-numerador! (:desal db))
                descripcion-patologia (ejecuta! (:maestros db) (busca-en-tbc-patologia 3264))
                [guardia hc hc-texto] (prepara-registros (assoc paciente 
                                                                :histpactratam histpactratam 
                                                                :histpacmotivo histpacmotivo
                                                                :descripcion-patologia (-> descripcion-patologia first :pat-descrip)))]
               (-> (crea-historia-clinica! (:asistencial db) guardia hc hc-texto)
                   (d/catch Exception #(throw %)))))

(defn ingresar-historia-a-sistema
  [db paciente]
  (when (persiste-historia-clinica! db paciente)
    {:id (:guar-hist-clinica paciente)}))

 
(tests

 "Cuando event-object recibe request, devuelve mapa con tipos de datos esperados..."

 (def event-object (-> (io/resource "payload_model.edn")
                       slurp
                       edn/read-string
                       :event_object))

 (let [obj (extraer-event-object event-object)]
   (number? (:fecha obj)) := true
   (number? (:hora obj)) := true
   (number? (:fecha-inicio-atencion obj)) := true
   (number? (:hora-inicio-atencion obj)) := true
   (number? (:hora-final-atencion obj)) := true
   (string? (:diagnostico obj)) := true
   (number? (:matricula obj)) := true
   (string? (:medico obj)) := true
   (string? (:motivo obj)) := true
   (string? (:historia obj)) := true
   (string? (:nombre obj)) := true
   (number? (:hc obj)) := true
   (string? (:patologia obj)) := true)

 "Cuando se recibe request y registro de guardia de paciente, se crean tres registros con el tamaño y los tipos de datos adecuados..."

 (def test-obj
   {:guar-hist-clinica 145233
    :guar-fecha-ingreso 20240924
    :guar-hora-ingreso 1156
    :hora-inicio-atencion 1245
    :hora-final-atencion 1305
    :fecha-inicio-atencion 20240924
    :guar-obra 1820
    :guar-plan "XILOPORTE"
    :guar-nroben "ERFD·DDSDSDS-DSDS"
    :diagnostico "Este es un diagnóstico muy, pero muy arrecho, ¡arreeechoo!"
    :historia "Estas son las anotaciones del médico"
    :descripcion-patologia "PATOLOGIA PATOLOGICA"
    :histpactratam 123122
    :histpacmotivo 545656
    :medico "JUNA MACENO"
    :matricula 1234587})

 (let [registros (prepara-registros test-obj)]
   (count registros) := 3
   (map count registros) := [6 40 6]
   (every? number? (first registros)) := true
   (mapv type (second registros))  := [java.lang.Long
                                       java.lang.Long
                                       java.lang.Integer
                                       java.lang.Integer
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Integer
                                       java.lang.Integer
                                       java.lang.Long
                                       java.lang.String
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.String
                                       java.lang.Long
                                       java.lang.Integer
                                       java.lang.Integer
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.String
                                       java.lang.Long
                                       java.lang.String
                                       java.lang.String
                                       java.lang.String
                                       java.lang.Long
                                       java.lang.String
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.Long
                                       java.lang.String
                                       java.lang.Long]
   (mapv type (last registros)) := [java.lang.Long java.lang.String java.lang.Long java.lang.String java.lang.String java.lang.Long])
   
   :rcf) 

(comment

  (extraer-event-object (-> (io/resource "payload_model.edn")
                            slurp
                            edn/read-string))

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
        registros (prepara-registros req)]
    #_(prepara-registros req)
    (persiste-historia-clinica! {:asistencial asistencial
                                 :desal desal} req)
    #_@(apply crea-historia-clinica! asistencial registros))

  (let [asistencial (-> (system-repl/system) :donut.system/instances :conexiones :asistencial)
        registro [182222 20240808 1300 9 343 20240712]]
    #_(actualiza-tbc-guardia registro)
    (ejecuta! asistencial (actualiza-tbc-guardia registro)))

  (d/let-flow [a (do (Thread/sleep 1000)
                     1)
               b (do (Thread/sleep 2000) 2)
               c (+ a b)]
              (d/future (* 10 c)))
  @(d/let-flow [a (do (Thread/sleep 1000)
                      1)
                b (do (Thread/sleep 2000) 2)]
               (d/future (+ a b)))

  @(d/let-flow [a (do (Thread/sleep 2000) 2)]
               (+ a (d/zip
                     (d/future (inc 20))
                     (d/future (throw (ex-info "Upps!" {})))
                     (d/future (inc 23)))))

  (when (throw (ex-info "excepcion boluda" {}))
    1)
  
  
  :rcf)
