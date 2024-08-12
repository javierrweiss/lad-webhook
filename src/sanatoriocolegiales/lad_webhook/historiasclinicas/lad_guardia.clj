(ns sanatoriocolegiales.lad-webhook.historiasclinicas.lad-guardia
  (:require [sanatoriocolegiales.lad-webhook.sql.enunciados :refer [inserta-en-tbc-histpac 
                                                                    inserta-en-tbc-histpac-txt 
                                                                    actualiza-tbc-guardia]]
            [sanatoriocolegiales.lad-webhook.sql.ejecucion :refer [ejecuta! obtiene-numerador!]] 
            [sanatoriocolegiales.lad-webhook.historiasclinicas.utils :refer [obtener-hora obtener-minutos]]))

(defn prepara-registros
  "Adapta el mapa que viene del request y devuelve un vector con tres registros (también vectores) listos para ser persistidos"
  [{:keys [guar-hist-clinica 
           guar-fecha-ingreso 
           guar-hora-ingreso
           hora_inicio_atencion
           hora_final_atencion 
           fecha_inicio_atencion 
           guar-obra 
           guar-plan
           guar-nroben
           diagnostico  
           historia  
           patologia 
           histpactratam
           medico
           matricula]}] 
 (let [hora (obtener-hora guar-hora-ingreso)
       minutos (obtener-minutos guar-hora-ingreso)
       hora-fin (obtener-hora hora_final_atencion)
       minutos-fin (obtener-minutos hora_final_atencion)
       hora-ini (obtener-hora hora_inicio_atencion)
       minutos-ini (obtener-minutos hora_inicio_atencion)]
   [[guar-hist-clinica guar-fecha-ingreso guar-hora-ingreso diagnostico hora_final_atencion fecha_inicio_atencion]
   [guar-hist-clinica 
    guar-fecha-ingreso 
    hora 
    minutos 
    0 
    2
    0 ;; especialidad por definir
    guar-hist-clinica
    guar-fecha-ingreso
    guar-hist-clinica
    0 ;; especialidad por definir
    0 ;; código de médico por definir 
    0
    0
    0
    hora-fin
    minutos-fin
    0
    diagnostico
    patologia ;; patologia por definir
    histpactratam
    medico
    matricula
    hora-ini
    minutos-ini
    0
    0
    0
    ""
    guar-obra
    guar-plan
    guar-plan
    guar-nroben 
    0
    ""
    0
    0
    0
    "N"
    0]
   [histpactratam historia]]))

(defn guarda-texto-de-historia
  [conn [numerador texto]]
  (let [len (count texto)
        textos (if (> len 77) 
                 (->> (partition 77 77 [] texto)
                      (map #(apply str %))) 
                 [texto])
        cantidad (count textos)
        contador (atom 1)]
    (ejecuta! conn (inserta-en-tbc-histpac-txt [numerador 1 0 0 "" cantidad]))
    (doseq [text textos] 
      (ejecuta! conn (inserta-en-tbc-histpac-txt [numerador 1 @contador @contador text 0]))
      (swap! contador inc))))

(defn crea-historia-clinica
  "Persiste 3 registros a sus respectivas tablas. Recibe una conexión y tres vectores con los datos a ser persistidos"
  [db registro-guardia registro-historia-paciente registro-historia-texto]
  (let [#_#_maestros (-> db :maestros)
        #_#_desal (-> db :desal)
        asistencial (-> db :asistencial)]
    (future (ejecuta! asistencial (inserta-en-tbc-histpac registro-historia-paciente)))
    (future (guarda-texto-de-historia asistencial registro-historia-texto))
    (future (ejecuta! asistencial (actualiza-tbc-guardia registro-guardia)))))

(defn persiste-historia-clinica
  "Toma la información del paciente y crea la historia clínica. Recibe el request y la conexión a la BD."
  [db paciente]
  (let [histpactratam (obtiene-numerador! db)
        [guardia hc hc-texto] (prepara-registros (assoc paciente :histpactratam histpactratam))]
    (crea-historia-clinica db guardia hc hc-texto)))

(comment

  (let [m {:a 1
           :z {:b 12 :c 332}}]
    (merge (select-keys (:z m) [:c]) (select-keys m [:a])))

(tap> (->> (partition 77 77 [] 
                      "It’s important for programmers like you to learn concurrent and parallel programming techniques 
               so you can design programs that run efficiently on modern hardware. Concurrency refers to a program’s 
               ability to carry out more than one task, and in Clojure you achieve this by placing tasks on separate threads. 
               Programs execute in parallel when a computer has more than one CPU, which allows more than one thread to be executed 
               at the same time.
               Concurrent programming refers to the techniques used to manage three concurrency risks: reference cells, mutual exclusion, 
               and deadlock. Clojure gives you three basic tools that help you mitigate those risks: futures, delays, and promises. 
               Each tool lets you decouple the three events of defining a task, executing a task, and requiring a task’s result. 
               Futures let you define a task and execute it immediately, allowing you to require the result later or never. Futures also cache 
               their results. Delays let you define a task that doesn’t get executed until later, and a delay’s result gets cached. Promises 
               let you express that you require a result without having to know about the task that produces that result. You can only deliver 
               a value to a promise once. ")
           (map #(apply str %))
           #_(map count)))
 
(partition 3 3 [] (range 1 11))

(->> 1152 str (take 2) (apply str) (Integer/parseInt))
(->> 1152 str (drop 2) (apply str) (Integer/parseInt))

:rcf)
