(ns sanatoriocolegiales.lad-webhook.sql.enunciados
  (:require [honey.sql :as sql]))

(defn inserta-en-tbc-histpac
  [values] 
  (sql/format {:insert-into :tbc_histpac
               :columns [:histpacnro ;; hc
                         :histpacfec ;; fecha reservas
                         :histpach ;; hora
                         :histpacm ;; minutos
                         :histpacr ;; resto (segundos...) completar con ceros
                         :histpace ;; 2, guardia
                         :histpacespfir ;; 407
                         :histpacnro1 ;; hc
                         :histpacfec1 ;; fecha reservas
                         :histpacnro2 ;; hc
                         :histpacespfir1 ;; 407
                         :histpacmedfir ;; 999880 (con dígito verificador)
                         :histpacmotivo ;; numerador => Hace referencia al diagnóstico
                         :histpacestudi ;; 0 
                         :histpachorasobre ;; 0
                         :histpachfinal ;; hora final atención
                         :histpacmfinal ;; minutos hora final atención
                         :histpacrfinal ;; completar con ceros
                         :histpacdiagno ;; diagnóstico => sacar de tbc_patologia
                         :histpacpatolo ;; 3264 
                         :histpactratam ;; motivo (guardar acá numerador ) tbl_parametros param_id 16, inc contador_entero y guardar ese número
                         :histpacmedfirnya ;; nombre médico (doctor_name)
                         :histpacmedfirmat ;; matricula (doctor_enrollment_type)
                         :histpachatenc ;; call_start_datetime
                         :histpacmatenc  ;; minutos
                         :histpacratenc ;; 00
                         :histpacderiva ;; 0
                         :histpacderivads ;; 0
                         :histpacderivasec ;; ""
                         :histpacobra ;; obra
                         :histpacpplan ;; plan 
                         :histpacplan ;; plan 
                         :histpacafil ;; nro afiliado
                         :histpacpedambula ;; 0
                         :histpacconshiv ;; ""
                         :histpacinterconsu ;; 0
                         :histpacentregado ;; 0
                         :histpacctro ;; 0
                         :histpacyodo ;; "N"
                         :histpaccancd ;; 0
                         ]
               :values [values]}))

(defn inserta-en-tbl-hist-txt
  [values]
  (sql/format {:insert-into :tbl-hist-txt
               :columns [:ht_histclin
                         :ht_fecha 
                         :ht_hora 
                         :ht_entrada 
                         :ht_motivo 
                         :ht_tratamiento]
               :values [values]}))

(defn inserta-en-tbl-ladguardia-fallidos
  [values]
  (sql/format {:insert-into :tbl_ladguardia_fallidos
               :columns [:hc
                         :fechaingreso
                         :horaingreso
                         :nombre 
                         :textohc 
                         :patolcie 
                         :diagnostico
                         :motivo]
               :values [values]}))

(defn busca-paciente-en-reservas
  [histclinica]
  (sql/format {:select [:reservasfech :reservashora :reservasobra :reservasobrpla :reservasnroben] 
               :from :tbc_reservas
               :where [:and [:= :reservashiscli histclinica] [:= :reservasmed 999880]]}))

(defn busca-en-tbc-patologia
  [codigo]
  (sql/format {:select :pat_descrip
               :from :tbc_patologia
               :where [:= :pat_codi codigo]}))
 
(defn obtener-numerador
  []
  (sql/format {:select :contador_entero
               :from :tbl_parametros
               :where [:= :paramid 16]}))
 
(defn actualiza-numerador
  [numerador_actual]
  (sql/format {:update :tbl_parametros
               :set  {:contador_entero (inc numerador_actual)}
               :where [:= :paramid 16]
               :returning [:contador_entero]})) 

(comment
  (let [numerador_actual 1]
    (sql/format {:update :tbl_parametros
                 :set  {:contador_entero (inc numerador_actual)}
                 :where [:= :paramid 16]
                 :returning [:contador_entero]})) 
  
  (busca-en-tbc-patologia 152)

  (let [hc 1]
    (busca-paciente-en-reservas hc))
  )