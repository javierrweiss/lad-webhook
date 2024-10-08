(ns sql-auxiliar
  (:require [next.jdbc :as jdbc]))

(defn crear-tabla-tbc-hist-pac
  [conn]
  (jdbc/execute! conn 
                 ["CREATE TABLE IF NOT EXISTS tbc_histpac  (
	               HistpacNro DECIMAL(10,0) NOT NULL,
	               HistpacFec INTEGER NOT NULL,
	               HistpacH INTEGER NOT NULL,
	               HistpacM INTEGER NOT NULL,
	               HistpacR INTEGER NOT NULL,
	               HistpacE INTEGER NOT NULL,
	               HistpacEspfir INTEGER NOT NULL,
	               HistpacNro1 DECIMAL(10,0) NOT NULL,
	               HistpacFec1 INTEGER NOT NULL,
	               HistpacNro2 DECIMAL(10,0) NOT NULL,
	               HistpacEspfir1 INTEGER NOT NULL,
	               HistpacMedfir INTEGER,
	               HistpacMotivo DECIMAL(10,0),
	               HistpacEstudi DECIMAL(10,0),
	               HistpacHoraSobre INTEGER,
	               HistpacHFinal INTEGER,
	               HistpacMFinal INTEGER,
	               HistpacRFinal INTEGER,
	               HistpacDiagno CHAR(28) NOT NULL,
	               HistpacPatolo INTEGER,
	               HistpacTratam DECIMAL(10,0),
	               HistpacMedfirNya CHAR(30) NOT NULL,
	               HistpacMedfirMat INTEGER,
	               HistpacHAtenc INTEGER,
	               HistpacMAtenc INTEGER,
	               HistpacRAtenc INTEGER,
	               HistpacDeriva INTEGER,
	               HistpacDerivaDs INTEGER,
	               HistpacDerivaSec CHAR(11) NOT NULL,
	               HistpacObra INTEGER,
	               HistpacPPlan CHAR(10) NOT NULL,
	               HistpacPlan CHAR(1) NOT NULL,
	               HistpacAfil CHAR(15) NOT NULL,
	               HistpacPedAmbula INTEGER,
	               HistpacConsHiv CHAR(1) NOT NULL,
	               HistpacInterconsu INTEGER,
	               HistpacEntregado INTEGER,
	               HistpacCtro INTEGER,
	               HistpacYodo CHAR(1) NOT NULL,
	               HistpacCancd INTEGER,
	               CONSTRAINT X_1_2_3_4_5_6 PRIMARY KEY (HistpacNro,HistpacFec,HistpacH,HistpacM,HistpacR,HistpacE))"]))

(defn crear-tabla-tbc-guardia
  [conn]
  (jdbc/execute! conn 
                 ["CREATE TABLE IF NOT EXISTS tbc_guardia  (
	               Guar_HistClinica DECIMAL(10,0) NOT NULL,
	               Guar_FechaIngreso INTEGER NOT NULL,
	               Guar_HoraIngreso INTEGER NOT NULL,
	               Guar_Especialidad INTEGER NOT NULL,
	               Guar_Estado INTEGER NOT NULL,
	               Guar_FechaIngreso1 INTEGER NOT NULL,
	               Guar_HoraIngreso1 INTEGER NOT NULL,
	               Guar_HistClinica1 DECIMAL(10,0) NOT NULL,
	               Guar_Especialidad1 INTEGER NOT NULL,
	               Guar_Estado1 INTEGER NOT NULL,
	               Guar_FechaIngreso3 INTEGER NOT NULL,
	               Guar_HoraIngreso3 INTEGER NOT NULL,
	               Guar_Especialidad3 INTEGER NOT NULL,
	               Guar_Estado3 INTEGER NOT NULL,
	               Guar_ApeNom CHAR(35) NOT NULL,
	               Guar_Obra INTEGER,
	               Guar_Plan CHAR(10) NOT NULL,
	               Guar_Nroben CHAR(15) NOT NULL,
	               Guar_Medico INTEGER,
	               Guar_TipoMed INTEGER,
	               Guar_Diagnostico INTEGER,
	               Guar_FechaAlta INTEGER,
	               Guar_HoraAlta INTEGER,
	               Guar_Turno INTEGER,
	               Guar_PrimeraVez INTEGER,
	               Guar_EspMed INTEGER,
	               Guar_HoraAtenc INTEGER,
	               Guar_LetraFc CHAR(1) NOT NULL,
	               Guar_NumeFc INTEGER,
	               Guar_TipoOperador INTEGER,
	               Guar_Operador INTEGER,
	               Guar_Deriva INTEGER,
	               Guar_DerivaSec INTEGER,
	               Guar_Habita INTEGER,
	               Guar_Cama CHAR(1) NOT NULL,
	               Guar_Pamifup INTEGER,
	               Guar_Anestesia INTEGER,
	               Guar_Ctro INTEGER,
	               Guar_Reingreso INTEGER,
	               Guar_FechaCama INTEGER,
	               Guar_HoraCama INTEGER,
	               Guar_FechaEpic INTEGER,
	               Guar_MenorAcom INTEGER,
	               Guar_OpeTipReingreso INTEGER,
	               Guar_OpeLegReingreso INTEGER,
	               Guar_OpeTipCama INTEGER,
	               Guar_OpeLegCama INTEGER,
	               Guar_EstadoFebril INTEGER,
	               Guar_QuienAnula INTEGER,
	               Guar_OtrCtro INTEGER,
	               GuarIva INTEGER,
	               GuarPresuFac INTEGER,
	               GuarNivel INTEGER,
	               GuarMedicab CHAR(7) NOT NULL,
	               GuarNropres INTEGER,
	               CONSTRAINT X_1_2_3 PRIMARY KEY (Guar_HistClinica,Guar_FechaIngreso,Guar_HoraIngreso))"]))

(defn crear-tabla-tbc-histpac-txt
  [conn]
  (jdbc/execute! conn ["CREATE TABLE IF NOT EXISTS tbc_histpac_txt (
	Txt1 DECIMAL(10,0) NOT NULL,
	Txt1g INTEGER NOT NULL,
	Txt2 INTEGER NOT NULL,
	Txt3 INTEGER NOT NULL,
	Txt4 CHAR(78) NOT NULL,
	Txt6 INTEGER,
	CONSTRAINT X_1_2_3_4 PRIMARY KEY (Txt1,Txt1g,Txt2,Txt3)
)"]))

(defn crear-tabla-tbl-ladguardia-fallidos
  [conn]
  (jdbc/execute! conn ["CREATE TABLE IF NOT EXISTS tbl_ladguardia_fallidos( 
                        hc int,
                        fechaingreso int,
                        horaingreso int,
                        nombre varchar,
                        textohc varchar,
                        patolcie varchar,
                        diagnostico varchar,
                        motivo varchar
)"]))

(defn crear-tabla-tbl-parametros
  [conn]
  (jdbc/execute! conn ["CREATE TABLE IF NOT EXISTS tbl_parametros (
	paramid int4 NOT NULL,
	fechadb date NULL,
	fechacbl int4 NULL,
	contador_entero numeric(10) NULL,
	contador_decimal numeric(10, 2) NULL,
	comentario bpchar(50) NULL,
	CONSTRAINT tbl_parametros_pkey PRIMARY KEY (paramid)
)"]))