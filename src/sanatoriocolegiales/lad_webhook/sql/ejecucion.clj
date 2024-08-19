(ns sanatoriocolegiales.lad-webhook.sql.ejecucion
  (:require [next.jdbc :as jdbc] 
            [next.jdbc.result-set :as rs]
            [com.brunobonacci.mulog :as mulog]
            [fmnoise.flow :as flow :refer [then else]]
            [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [obtener-numerador actualiza-numerador]])
  (:import java.time.LocalDateTime))
       
(defn ejecuta!
  [conn sentencia]
  (try
    (jdbc/execute! conn sentencia {:builder-fn rs/as-unqualified-kebab-maps})
    (catch Exception e (do (mulog/log ::excepcion-sql :fecha (LocalDateTime/now) :mensaje (ex-message e) :sentencia sentencia)
                           (throw (ex-info "ErrorSQL: No se pudo ejecutar la sentencia" {:type :sanatoriocolegiales.lad-webhook.error.error/excepcion-sql
                                                                                         :fecha (LocalDateTime/now)
                                                                                         :message (ex-message e)}))))))

(defn obtiene-numerador!
  [db]
  (try
    (jdbc/with-transaction [conn db]
      (->>  (jdbc/execute! conn (obtener-numerador) {:builder-fn rs/as-unqualified-kebab-maps})
            (then #(-> % first :contador-entero))
            (then #(jdbc/execute! conn (actualiza-numerador %) {:builder-fn rs/as-unqualified-kebab-maps}))
            (then #(-> % first :contador-entero))
            (else #(throw %))))
    (catch Exception e (do
                         (mulog/log ::excepcion-transaccion-sql :fecha (LocalDateTime/now) :mensaje (ex-message e))
                         (throw (ex-info "Hubo un problema al obtener el numerador" {:type :sanatoriocolegiales.lad-webhook.error.error/excepcion-sql
                                                                                     :fecha (LocalDateTime/now)
                                                                                     :message (ex-message e)}))))))
    
(comment  
  
  (def desal (-> (system-repl/system) :donut.system/instances :conexiones :desal))
  (def bases_auxiliares (-> (system-repl/system) :donut.system/instances :conexiones :bases_auxiliares))
  (def asistencial (-> (system-repl/system) :donut.system/instances :conexiones :asistencial)) 

  (->> (jdbc/execute! desal (obtener-numerador) {:builder-fn rs/as-unqualified-kebab-maps})
      (then #(-> % first :contador-entero)))
  
  (obtiene-numerador! asistencial)
 
  (then #(-> % first :contador-entero) [{:contador-entero 0}])

  (actualiza-numerador 10)
(ejecuta! asistencial ["PRAGMAF table_info(tbc_histpac_txt)"])
  (ejecuta! asistencial ["PRAGMA table_info(tbc_histpac_txt)"]) 
  (ejecuta! asistencial ["PRAGMA table_info(tbc_histpac)"])
  (ejecuta! asistencial ["PRAGMA table_info(tbc_guardia)"])
   
  (ejecuta! asistencial ["SELECT guar_fechaingreso, guar_horaingreso FROM tbc_guardia WHERE guar_histclinica = ?" 182222])
  (tap> (ejecuta! asistencial ["SELECT * FROM tbc_guardia"]))
  (ejecuta! asistencial ["SELECT * FROM tbc_guardia WHERE guar_histclinica = ?" 182222])
  (ejecuta! asistencial ["DELETE FROM tbc_histpac"])  
  (ejecuta! asistencial ["DELETE FROM tbc_histpac_txt"])
  (tap> (ejecuta! asistencial ["SELECT * FROM tbc_histpac"]))
  (ejecuta! asistencial ["SELECT * FROM tbc_histpac WHERE histpacnro = ? " 182222])
  (tap> (ejecuta! asistencial ["SELECT * FROM tbc_histpac_txt"]))
  (ejecuta! bases_auxiliares ["PRAGMA table_info(tbl_ladguardia_fallidos)"])
  (ejecuta! bases_auxiliares ["DROP TABLE tbl_ladguardia_fallidos"])
  (ejecuta! bases_auxiliares ["SELECT * FROM tbl_ladguardia_fallidos"])
  (ejecuta! bases_auxiliares ["SELECT rowid, hc, fechaingreso, horaingreso FROM tbl_ladguardia_fallidos"])
  (ejecuta! asistencial 
                  ["INSERT INTO tbc_guardia (
                    Guar_HistClinica,Guar_FechaIngreso, Guar_HoraIngreso, Guar_Especialidad, Guar_Estado, 
                    Guar_FechaIngreso1, Guar_HoraIngreso1, Guar_HistClinica1, Guar_Especialidad1, Guar_Estado1, 
                    Guar_FechaIngreso3, Guar_HoraIngreso3, Guar_Especialidad3, Guar_Estado3, Guar_ApeNom, Guar_Obra, 
                    Guar_Plan, Guar_Nroben, Guar_Medico, Guar_TipoMed, Guar_Diagnostico, Guar_FechaAlta, Guar_HoraAlta, 
                    Guar_Turno, Guar_PrimeraVez, Guar_EspMed, Guar_HoraAtenc, Guar_LetraFc, Guar_NumeFc, Guar_TipoOperador, 
                    Guar_Operador, Guar_Deriva, Guar_DerivaSec, Guar_Habita, Guar_Cama, Guar_Pamifup, Guar_Anestesia, 
                    Guar_Ctro, Guar_Reingreso, Guar_FechaCama, Guar_HoraCama, Guar_FechaEpic, Guar_MenorAcom, 
                    Guar_OpeTipReingreso, Guar_OpeLegReingreso, Guar_OpeTipCama, Guar_OpeLegCama, 
                    Guar_EstadoFebril, Guar_QuienAnula, Guar_OtrCtro, GuarIva, GuarPresuFac, GuarNivel, GuarMedicab, GuarNropres) 
                    VALUES(758922, 20240814, 1630, 4, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'JUANA DE ARCO', 1020, '1000', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, '',
                    0, 0, 0, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '', 0)"])
  

   (if-let [paciente (seq (ejecuta! asistencial (sanatoriocolegiales.lad-webhook.sql.enunciados/selecciona-guardia 180022 20240808 1430)))]
     paciente
     :not-found)
  

  )