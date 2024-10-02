(ns sanatoriocolegiales.auxiliares
  (:require [honey.sql :as sql]))

(defn sql-insertar-registro-en-guardia
  [hc fechaing horaing esp estado nombre obra plan benef letra cama medicab]
  (sql/format {:insert-into :tbc_guardia
               :columns [:guar_histclinica 
                         :guar_fechaingreso 
                         :guar_horaingreso
                         :guar_especialidad
                         :guar_estado
                         :guar_fecha_ingreso ;; Difiere del original para poder resolver discordancia con el modo en el que next.jdbc traduce los campos en tablas relativity y postgres
                         :guar_hora_ingreso  ;; Difiere del original para poder resolver discordancia con el modo en el que next.jdbc traduce los campos en tablas relativity y postgres
                         :guar_hist_clinica ;; Difiere del original para poder resolver discordancia con el modo en el que next.jdbc traduce los campos en tablas relativity y postgres
                         :guar_especialidad1
                         :guar_estado1
                         :guar_fechaingreso3
                         :guar_horaingreso3
                         :guar_estado3 
                         :guar_especialidad3
                         :guar_apenom
                         :guar_obra 
                         :guar_plan 
                         :guar_nroben
                         :guar_letrafc
                         :guar_cama
                         :guarmedicab]
               :values [[hc fechaing horaing esp estado fechaing horaing hc esp estado fechaing horaing estado esp nombre obra plan benef letra cama medicab]]}))


