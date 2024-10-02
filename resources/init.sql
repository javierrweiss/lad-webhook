CREATE TABLE tbc_histpac (
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
	CONSTRAINT X_1_2_3_4_5_6 PRIMARY KEY (HistpacNro,HistpacFec,HistpacH,HistpacM,HistpacR,HistpacE)
);
CREATE INDEX X_7_8_9 ON tbc_histpac (HistpacEspfir,HistpacNro1,HistpacFec1);
CREATE INDEX X_8_9 ON tbc_histpac (HistpacNro1,HistpacFec1);
CREATE INDEX X_A_B ON tbc_histpac (HistpacNro2,HistpacEspfir1);

CREATE TABLE tbc_guardia (
	Guar_HistClinica DECIMAL(10,0) NOT NULL,
	Guar_FechaIngreso INTEGER NOT NULL,
	Guar_HoraIngreso INTEGER NOT NULL,
	Guar_Especialidad INTEGER NOT NULL,
	Guar_Estado INTEGER NOT NULL,
	Guar_Fecha_Ingreso INTEGER NOT NULL,  -- Difiere del original para poder resolver discordancia con el modo en el que next.jdbc traduce los campos en tablas relativity y postgres
	Guar_Hora_Ingreso INTEGER NOT NULL,   -- Difiere del original para poder resolver discordancia con el modo en el que next.jdbc traduce los campos en tablas relativity y postgres
	Guar_Hist_Clinica DECIMAL(10,0) NOT NULL,  -- Difiere del original para poder resolver discordancia con el modo en el que next.jdbc traduce los campos en tablas relativity y postgres
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
	CONSTRAINT X_1_2_3 PRIMARY KEY (Guar_Hist_Clinica,Guar_Fecha_Ingreso,Guar_Hora_Ingreso)
);
CREATE INDEX X_4_5_6_7 ON tbc_guardia (Guar_Especialidad,Guar_Estado,Guar_Fecha_Ingreso,Guar_Hora_Ingreso);
CREATE INDEX X_8_9_A ON tbc_guardia (Guar_Hist_Clinica,Guar_Especialidad1,Guar_Estado1);
CREATE INDEX X_B_C_D_E ON tbc_guardia (Guar_FechaIngreso3,Guar_HoraIngreso3,Guar_Especialidad3,Guar_Estado3);

CREATE TABLE IF NOT EXISTS tbc_histpac_txt (
	Txt1 DECIMAL(10,0) NOT NULL,
	Txt1g INTEGER NOT NULL,
	Txt2 INTEGER NOT NULL,
	Txt3 INTEGER NOT NULL,
	Txt4 CHAR(78) NOT NULL,
	Txt6 INTEGER,
	CONSTRAINT X_1_2_3_4 PRIMARY KEY (Txt1,Txt1g,Txt2,Txt3)
);

CREATE TABLE IF NOT EXISTS tbl_ladguardia_fallidos( 
                        hc int,
                        fechaingreso int,
                        horaingreso int,
                        nombre varchar,
                        textohc varchar,
                        patolcie varchar,
                        diagnostico varchar,
                        motivo varchar
);

CREATE TABLE tbl_parametros (
	paramid int4 NOT NULL,
	fechadb date NULL,
	fechacbl int4 NULL,
	contador_entero numeric(10) NULL,
	contador_decimal numeric(10, 2) NULL,
	comentario bpchar(50) NULL,
	CONSTRAINT tbl_parametros_pkey PRIMARY KEY (paramid)
);

INSERT INTO tbl_parametros (paramid, fechadb, fechacbl, contador_entero, contador_decimal, comentario) VALUES(16,NOW(), 0, 0, 0, '');

CREATE TABLE tbc_PATOLOGIA (
	Pat_Codi INTEGER,
	Pat_DescripRedu CHAR(8) NOT NULL,
	Pat_DescripRedu_2 CHAR(8) NOT NULL,
	Pat_Modo INTEGER,
	Pat_CodiEsp INTEGER,
	Pat_TipInter INTEGER,
	Pat_8 INTEGER,
	Pat_Infec INTEGER,
	Pat_DerivaInter INTEGER,
	Pat_Score INTEGER,
	Pat_InterQuiru INTEGER,
	Pat_EspIntercons_5 INTEGER,
	Pat_EspIntercons_4 INTEGER,
	Pat_EspIntercons_3 INTEGER,
	Pat_EspIntercons_2 INTEGER,
	Pat_EspIntercons_1 INTEGER,
	Pat_Est INTEGER,
	Pat_CodiPatCie INTEGER,
	Pat_PatAisla INTEGER,
	Pat_TipLegModi INTEGER,
	Pat_LegModi INTEGER,
	Pat_FecModi INTEGER,
	Pat_MotAisla INTEGER,
	Pat_Filler CHAR(94) NOT NULL,
	Pat_Descrip CHAR(30) NOT NULL,
	CONSTRAINT X_1 PRIMARY KEY (Pat_Codi)
);
CREATE INDEX X_2 ON tbc_PATOLOGIA (Pat_DescripRedu);
CREATE INDEX X_5 ON tbc_PATOLOGIA (Pat_CodiEsp);

INSERT INTO tbc_PATOLOGIA (Pat_Codi, Pat_DescripRedu, Pat_DescripRedu_2, Pat_Modo, Pat_CodiEsp, Pat_TipInter, Pat_8, Pat_Infec, Pat_DerivaInter, Pat_Score, Pat_InterQuiru, Pat_EspIntercons_5, Pat_EspIntercons_4, Pat_EspIntercons_3, Pat_EspIntercons_2, Pat_EspIntercons_1, Pat_Est, Pat_CodiPatCie, Pat_PatAisla, Pat_TipLegModi, Pat_LegModi, Pat_FecModi, Pat_MotAisla, Pat_Filler, Pat_Descrip) 
VALUES(3264, 'Demanda', 'Demanda', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'X', 'Demanda espontánea');