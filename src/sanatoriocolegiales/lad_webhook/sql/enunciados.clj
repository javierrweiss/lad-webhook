(ns sanatoriocolegiales.lad-webhook.sql.enunciados
  (:require [honey.sql :as sql]))

(defn inserta-en-tbc-histpac
  [values]
  (sql/format {:insert-into :tbc_histpac
               :columns [:histpacnro
                         :histpacfec
                         :histpach
                         :histpacm
                         :histpacr
                         :histpace
                         :histpacespfir
                         :histpacnro1
                         :histpacfec1
                         :histpacnro2
                         :histpacespfir1
                         :histpacmedfir
                         :histpacmotivo
                         :histpacestudi
                         :histpachorasobre
                         :histpachfinal
                         :histpacmfinal
                         :histpacrfinal
                         :histpacdiagno
                         :histpacpatolo
                         :histpactratam
                         :histpacmedfirnya
                         :histpacmedfirmat
                         :histpachatenc
                         :histpacmatenc
                         :histpacratenc
                         :histpacderiva
                         :histpacderivads
                         :histpacderivasec
                         :histpacobra
                         :histpacpplan
                         :histpacplan
                         :histpacafil
                         :histpacpedambula
                         :histpacconshiv
                         :histpacinterconsu
                         :histpacentregado
                         :histpacctro
                         :histpacyodo
                         :histpaccancd]
               :values [values]}))

(defn inserta-en-tbl-hist-txt
  [values]
  (sql/format {:insert-into :tbl_hist_txt
               :columns [:ht_histclin
                         :ht_fecha 
                         :ht_hora 
                         :ht_entrada 
                         :ht_motivo 
                         :ht_tratamiento
                         :ht_estudios ]
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
  [{:keys [histclinica fecha hora]}]
  (sql/format {:update :tbc_guardia
               :set {:guar_estado 0}
               :where [:and [:= :guar_histclinica histclinica] [:= :guar_fechaingreso fecha] [:= :guar_horaingreso hora]]}))

(defn selecciona-guardia
  [histclinica fecha hora]
  (sql/format {:select [:guar_histclinica :guar_fechaingreso :guar_horaingreso :guar_obra :guar_plan]
               :from :tbc_guardia
               :where [:and [:= :guar_histclinica histclinica] [:= :guar_fechaingreso fecha] [:= :guar_horaingreso hora]]}))
