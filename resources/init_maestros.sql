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
