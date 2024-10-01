(ns sanatoriocolegiales.lad-webhook.sql.enunciados
  (:require [honey.sql :as sql]))

(defn inserta-en-tbc-histpac
  [values] 
  (sql/format {:insert-into :tbc_histpac
               :columns [:histpacnro ;; hc
                         :histpacfec ;; fecha ingreso tbc_guardia
                         :histpach ;; hora
                         :histpacm ;; minutos
                         :histpacr ;; resto (segundos...) completar con ceros
                         :histpace ;; 2, guardia
                         :histpacespfir ;; 407
                         :histpacnro1 ;; hc
                         :histpacfec1 ;; fecha ingreso tbc_guardia
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

(defn inserta-en-tbc-histpac-txt
  [values]
  (sql/format {:insert-into :tbc_histpac_txt
               :columns [:txt1
                         :txt1g 
                         :txt2 
                         :txt3 
                         :txt4 
                         :txt6]
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

(defn actualiza-tbc-guardia
  [[histclinica fecha hora diagnostico hora-atencion fecha-atencion]]
  (sql/format {:update :tbc_guardia
               :set {:guar_estado 4 
                     :guar_diagnostico diagnostico        ;; Es el código de patología
                     :guar_fechaalta fecha-atencion
                     :guar_horaalta hora-atencion}
               :where [:and 
                       [:= :guar_histclinica histclinica] 
                       [:= :guar_fechaingreso fecha] 
                       [:= :guar_horaingreso hora]]}))

(defn selecciona-guardia
  [histclinica fecha hora]
  (sql/format {:select [:guar_histclinica :guar_fechaingreso :guar_horaingreso :guar_obra :guar_plan :guar_nroben]
               :from :tbc_guardia
               :where [:and [:= :guar_histclinica histclinica] [:= :guar_fechaingreso fecha] [:= :guar_horaingreso hora]]}))

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
  )