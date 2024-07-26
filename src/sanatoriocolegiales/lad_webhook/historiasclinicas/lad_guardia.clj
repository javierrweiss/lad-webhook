(ns sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia)

(defn- request->registros
  "Adapta el mapa que viene del request y devuelve un vector con tres registros (también vectores) listos para ser persistidos"
  [request]
  [nil nil nil])

(defn- crea-historia-clinica
  "Persiste 3 registros a sus respectivas tablas. Recibe una conexión y tres vectores con los datos a ser persistidos"
  [db registro-guardia registro-historia-paciente registro-historia-texto])

(defn persiste-historia-clinica
  "Toma el request y crea la historia clínica del paciente. Recibe el request y la conexión a la BD."
  [request db]
  (let [[guardia hc hc-texto] (request->registros request)]
    (crea-historia-clinica db guardia hc hc-texto)))