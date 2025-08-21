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

CREATE TABLE tbl_hist_txt (
	ht_histclin numeric(10) NOT NULL,
	ht_fecha numeric(8) NOT NULL,
	ht_hora numeric(8) NOT NULL,
	ht_entrada numeric(1) NOT NULL,
	ht_motivo varchar(7800) NULL,
	ht_tratamiento varchar(7800) NULL,
	ht_estudios varchar(7800) NULL,
	CONSTRAINT pk_hist_txt PRIMARY KEY (ht_histclin, ht_fecha, ht_hora, ht_entrada)
);