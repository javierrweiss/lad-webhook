(ns sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia
  (:require
   [manifold.deferred :as d]
   [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [inserta-en-tbc-histpac
                                                           inserta-en-tbc-histpac-txt 
                                                           busca-en-tbc-patologia]]
   [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta! obtiene-numerador!]]
   [sanatoriocolegiales.lad-webhook.historiasclinicas.utils :refer [obtener-hora
                                                                    obtener-minutos
                                                                    extraer-fecha-y-hora
                                                                    obtener-hora-finalizacion]]
   [sanatoriocolegiales.lad-webhook.sql.conexiones :refer [devolver]]
   [hyperfiddle.rcf :refer [tests]]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.brunobonacci.mulog :as µ]
   [next.jdbc :as jdbc]
   [com.potetm.fusebox.timeout :as to])
  (:import java.time.LocalDateTime))

(def timeout
  (to/init {::to/timeout-ms 10000}))

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
           doctor_enrollment
           patient_name
           patient_external_id ;; Acá recibimos el número de HC
           ] :as req}]
  #_(tap> req)
  (let [[fecha hora] (extraer-fecha-y-hora order_id)
        [fecha-ini hora-ini] (extraer-fecha-y-hora call_start_datetime)
        hora-finalizacion (obtener-hora-finalizacion hora-ini call_duration)]
    {:fecha fecha
     :hora hora
     :fecha-inicio-atencion fecha-ini
     :hora-inicio-atencion hora-ini
     :hora-final-atencion hora-finalizacion
     :diagnostico call_diagnosis
     :matricula (Integer/parseInt doctor_enrollment)
     :medico doctor_name
     :motivo call_motive
     :historia call_doctor_comment
     :nombre patient_name
     :hc (Integer/parseInt patient_external_id)
     :patologia call_cie10}))

(defn prepara-registros
  "Adapta el mapa que viene del request y devuelve un vector con tres registros (también vectores) listos para ser persistidos"
  [{:keys [hc
           reservasfech
           reservashora
           hora-inicio-atencion
           hora-final-atencion
           reservasobra
           reservasobrpla
           reservasnroben
           diagnostico
           historia
           descripcion-patologia
           ;;patologia // Se ignora cie10 y se introduce código fijo: 3264
           histpactratam
           histpacmotivo
           medico
           matricula
           motivo] :as data}]
  #_(tap> data)
  (let [hora (obtener-hora reservashora)
        minutos (obtener-minutos reservashora)
        hora-fin (obtener-hora hora-final-atencion)
        minutos-fin (obtener-minutos hora-final-atencion)
        hora-ini (obtener-hora hora-inicio-atencion)
        minutos-ini (obtener-minutos hora-inicio-atencion)
        nro-afiliado (let [len (count reservasnroben)]
                       (if (> len 15)
                         (->> reservasnroben (take 15) (apply str))
                         reservasnroben))]
    [;; tbc_histpac
     [hc
      reservasfech
      hora
      minutos
      0
      2
      407 
      hc
      reservasfech
      hc
      407
      999880 
      histpacmotivo
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
      (or reservasobra 0)
      (or reservasobrpla "")
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
     [histpactratam diagnostico histpacmotivo (str "Motivo: " motivo " Tratamiento: " historia) medico matricula]]))

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
     (jdbc/execute! conn (inserta-en-tbc-histpac-txt [numerador 1 0 0 "" cantidad]))
     (doseq [text textos :let [t (if-not medico (str "Diagnostico: " text) text)]]
       (jdbc/execute! conn (inserta-en-tbc-histpac-txt [numerador 1 @contador @contador t 0]))
       (swap! contador inc))
     (when medico
       (jdbc/execute!
         conn
        (inserta-en-tbc-histpac-txt [numerador 1 (inc @contador) (inc @contador) profesional-y-matricula cantidad]))))))

(defn ejecutar-tx-historia-clinica!
  "Persiste 3 registros a sus respectivas tablas. Recibe una conexión y dos vectores con los datos a ser persistidos"
  [db registro-historia-paciente registro-historia-texto]
  #_(tap> db)
  (let [[reg1 reg2] (take 2 registro-historia-texto)
        [reg3 reg4] (drop 2 registro-historia-texto)]
    (try
      (to/with-timeout
        timeout
        (jdbc/with-transaction [conn db]
          (jdbc/execute! conn (inserta-en-tbc-histpac registro-historia-paciente))
          (guarda-texto-de-historia conn reg1 reg2)
          (guarda-texto-de-historia conn reg3 reg4)))
      ;;Devolverá true si no arroja excepción!
      true
      (catch Exception e (do
                           (µ/log ::error-al-crear-historia-clinica :mensaje (ex-message e) :datos (ex-data e) :fecha (LocalDateTime/now))
                           (throw (ex-info "Error al crear historia clínica" {:mensaje (ex-message e)
                                                                              :datos (ex-data e)
                                                                              :fecha (LocalDateTime/now)}))))
      ;; OJO! with-transaction si recibe una conexión no la cierra. Caso contrario si recibe un datasource.
      (finally (devolver db)))))

(comment
  (def asistencial (-> (system-repl/system) :donut.system/instances :conexiones :asistencial))
  (let [paciente {:hc 1000001
                  :hora-inicio-atencion 1259
                  :hora-final-atencion 1422
                  :nombre "Pedro Manuel Juarez"
                  :historia "eh?c dcs sdac pqscja op jpofd svdpvojfpd sbdsvpjopfkdpdobv pokpo fddkbsvpodjpofjvdp ojpojdposvjkvfvdjcojpwf vdjpofvdjc"
                  :patologia "patos"
                  :diagnostico "logia asdsas"
                  :motivo "Motivado re"
                  :reservasfech 20250101
                  :reservashora 1125
                  :reservasobra 1820
                  :reservasobrpla "sdssdds"
                  :reservasnroben "dssfdsd"
                  :medico "Marcoandrea Gallegos"
                  :matricula 10101010}
        [hc hc-texto] (prepara-registros (assoc paciente
                                                :histpactratam 1990
                                                :histpacmotivo 1990
                                                :descripcion-patologia "Motivo!!!"))]
    #_(tap> hc)
    #_(tap> (ejecutar-tx-historia-clinica! (asistencial) hc hc-texto))
    #_(tap> (to/with-timeout
              timeout
              (jdbc/with-transaction [conn (asistencial)]
                (apply guarda-texto-de-historia conn (take 2 hc-texto)))))
    (tap> (apply guarda-texto-de-historia (asistencial) (drop 2 hc-texto))))

  (tap> (guarda-texto-de-historia (asistencial) 77777711
                                  "Prueba muy larga est e eee repdsvp p 
                                                     wrpopoasgzpmbpr gopsjpowporgdjpo 
                                                     jrgopfdbjmotrg djbjdefsbpxocggrpfjdboj
                                                     pgofjdbpgogsfxbjcjgposjvxbvfgpsfbcpoxvcvx
                                                     dfdafdsbxc  esrgdhfnsdfgzfbvc zgsgxgbx ewee
                                                     ewr r rerwe agest rthhhrtesgdxnfxrte sges
                                                    ewerqwreweteytrytere waryethr rwahsgjd er
                                                     oioiiojjlkjjk jkewljkljf klwejflkjelk  jlkfjwelkfwf
                                                          sgdgewfa<sgzfdgxndafdsgzdg nxngdfgsdfaafsgdb
                                                          gbs waefsgz fdbdfsgz sgzhdxngdres gdgxnfdgfs 
                                                          errgfhdgh aesgbd bdtyh zdncbxzfdshd jhgdfsazvx easdg
                                                          s gfdhgdgdfs dgxbsetgfb gsg hdgnggra  sghdrgd trsgads
                                                          e sgzeaetsdhtgsre twredfgf awrtrzeh dzgfar wtzrgfs 
                                                           egsrdhgxfhdr tes dearstgfd fgtszegdetsgrd etgr trhgzs
                                                           grdh tesd raesgfe fsd retsgf"))

  (tap> (guarda-texto-de-historia (asistencial) 17777777 "texto corto"))

  :dbg
  :rcf)

(defn persiste-historia-clinica!
  "Toma la información del paciente y crea la historia clínica. Recibe el request y la conexión a la BD."
  [db paciente] 
  (let [histpactratam (obtiene-numerador! (:desal db))
        histpacmotivo (obtiene-numerador! (:desal db))
        descripcion-patologia (ejecuta! ((:maestros db)) (busca-en-tbc-patologia 3264))
        [hc hc-texto] (prepara-registros (assoc paciente
                                                :histpactratam histpactratam
                                                :histpacmotivo histpacmotivo
                                                :descripcion-patologia (-> descripcion-patologia first :pat_descrip)))]
    (ejecutar-tx-historia-clinica! ((:asistencial db)) hc hc-texto)))

(defn ingresar-historia-a-sistema
  [db paciente]
  (when (persiste-historia-clinica! db paciente)
    {:id (:hc paciente)}))

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
 
 (def event-object2 {:datetime "2025-04-03T12:16:59.737Z",
                     :event_type "CALL_ENDED",
                     :event_object
                     {:patient_id "32323388",
                      :doctor_enrollment_prov "C",
                      :patient_phone "+5491167526045",
                      :call_diagnosis "Enfermedad del reflujo gastroesofágico",
                      :patient_gender "M",
                      :call_cie10 "K21",
                      :call_doctor_rating 5,
                      :call_motive "dcss fs bsgagfb ds",
                      :call_patient_comment "",
                      :call_patient_rating 0,
                      :patient_email "ccieri.christian@gmail.com",
                      :call_start_datetime "2025-04-03T12:14:20.424Z",
                      :patient_location_city "Mar del Plata",
                      :rest_indication false,
                      :call_resolution "solved",
                      :doctor_enrollment "123456",
                      :provider_id "5ef21520359c9f0087212b1f",
                      :patient_location_longitude -57.5351,
                      :order_id "2024/01/05 14:00",
                      :call_doctor_comment "Evolucion ",
                      :patient_name "Christian Cieri",
                      :patient_age 38,
                      :patient_external_id "144160",
                      :call_duration 241,
                      :doctor_enrollment_type "MN",
                      :doctor_name "Núñez Simón",
                      :patient_location_latitude -37.9954,
                      :patient_location_country_code "AR",
                      :doctor_id "23329543129",
                      :patient_location_region_code "B",
                      :call_id "67ee7bb6812ed1489773ed9e"}})
 
 (let [obj (extraer-event-object (:event_object event-object2))]
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
   {:hc 145233
    :reservasfech 20240924
    :reservashora 1156
    :hora-inicio-atencion 1245
    :hora-final-atencion 1305
    :fecha-inicio-atencion 20240924
    :reservasobra 1820
    :reservasobrpla "XILOPORTE"
    :reservasnroben "ERFD·DDSDSDS-DSDS"
    :diagnostico "Este es un diagnóstico muy, pero muy arrecho, ¡arreeechoo!"
    :historia "Estas son las anotaciones del médico"
    :descripcion-patologia "PATOLOGIA PATOLOGICA"
    :histpactratam 123122
    :histpacmotivo 545656
    :medico "JUNA MACENO"
    :matricula 1234587})
 
 (let [registros (prepara-registros test-obj)]
   (count registros) := 2
   (mapv count registros) := [40 6] 
   (mapv type (first registros))  := [java.lang.Long
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

 "Cuando persiste HC devuelve id"

 (with-redefs-fn {#'persiste-historia-clinica! (constantly true)}
   #(let [paciente {:hc 123}
         db {}]
     (ingresar-historia-a-sistema db paciente))) := {:id 123}
 
 "Cuando no persiste HC lanza excepción"

 (with-redefs-fn {#'persiste-historia-clinica! (throw (ex-info "Excepción x" {}))}
   #(let [paciente {:hc 123}
          db {}]
      (ingresar-historia-a-sistema db paciente))) :throws clojure.lang.ExceptionInfo
 
 :rcf) 


