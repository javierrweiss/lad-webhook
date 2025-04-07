(ns sanatoriocolegiales.lad-webhook.especificaciones.especificaciones
  (:require [clojure.spec.gen.alpha :as gen]
            [clojure.spec.alpha :as spec]
            [hyperfiddle.rcf :refer [tests]])
  (:import java.time.Instant
           java.time.LocalDateTime
           (java.time.format DateTimeParseException
                             DateTimeFormatter)))

(defn rand-string-nums
  [max]
  (take 100 (repeatedly #(rand-int max))))

(def numero-string-spec (spec/with-gen (spec/and string? #(re-matches #"\d+" %))
                          (fn [] (spec/gen (into #{} (mapv str (rand-string-nums 500000)))))))

(def phone-spec (spec/with-gen (spec/and string? #(re-matches #"\+\d+" %))
                  (fn [] (spec/gen (into #{} (mapv (fn [e] (str "+" e)) (rand-string-nums 3000000)))))))

(def name-spec (spec/with-gen (spec/and string? #(re-matches #"([A-Z][a-z]+)(\s[A-Z][a-z]+){1,}" %))
                 #(spec/gen #{"Julian Castro" "María Salazar" "Eder Vanega" "Tito Fuentes" "Tom Cruise" "Ana Molina" "Mirko Kovac" "Chimbo Chimiborazo" "Samba Llena"})))

(def uppercase-letter-spec (spec/with-gen (spec/and string? #(re-matches #"[A-Z]" %))
                             (fn [] (spec/gen #{"A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K"}))))

(def uuid-spec (spec/with-gen (fn [val] 
                                (if (string? val) 
                                  (uuid? (try 
                                           (java.util.UUID/fromString val)
                                           (catch IllegalArgumentException e false)))
                                  (uuid? val)))
                 #(spec/gen (into #{} (mapv str (take 100 (repeatedly random-uuid))))))) 

(def instant-spec (spec/with-gen (fn [d] 
                                   (if (string? d)
                                     (inst? (try 
                                              (Instant/parse d)
                                              (catch DateTimeParseException e false)))
                                     (inst? d)))
                    #(spec/gen (conj #{} (Instant/now)))))

(def custom-date-spec (spec/with-gen (spec/and string? #(re-matches #"\d{4}(/|-)\d{2}(/|-)\d{2} \d{2}:\d{2}" %))
                       #(spec/gen #{(-> (LocalDateTime/now)
                                        (.format (DateTimeFormatter/ofPattern "uuuu/MM/dd HH:mm")))})))
 
(spec/def ::datetime instant-spec)

(spec/def ::event_type #{"CALL_ENDED" "PRESCRIPTION" "PRACTICES" "CASE_CLOSED" "COULD_NOT_CONTACT" "APPOINTMENT_CREATED" "APPOINTMENT_CANCELLED"})

(spec/def ::call_id uuid-spec)

(spec/def ::patient_name name-spec)

(spec/def ::patient_id numero-string-spec)

(spec/def ::provider_id uuid-spec)

(spec/def ::doctor_id numero-string-spec)

(spec/def ::call_resolution #{"resolved" "urgent" "referred"})

(spec/def ::call_start_datetime instant-spec)

(spec/def ::patient_external_id numero-string-spec)

;; Debe ser fecha y hora con formato YYYY/MM/dd HH:MM Debe coincidir con el ingreso de Guardia
(spec/def ::order_id custom-date-spec) 

(spec/def ::rest_indication boolean?)

(spec/def ::call_motive string?)

(spec/def ::call_duration nat-int?)

(spec/def ::call_doctor_comment string?)

(spec/def ::call_diagnosis string?)

(spec/def ::call_cie10 string?)

(spec/def ::call_patient_rating int?)

(spec/def ::call_doctor_rating int?)

(spec/def ::call_patient_comment string?)

(spec/def ::patient_age (spec/and nat-int? #(< % 120)))

(spec/def ::patient_phone phone-spec)

(spec/def ::patient_email (spec/with-gen (spec/and string? #(re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$" %))
                            #(spec/gen #{"changuito@gmail.com" "desperar@sanatoriocolegiales.com.ar" "pipalaloca@hotmail.com" "li_li_mi@fibertel.com.ar" "dong_ong@yahoo.com"})))

(spec/def ::patient_gender #{"M" "F"})

(spec/def ::patient_location_latitude double?)

(spec/def ::patient_location_longitude double?)

(spec/def ::patient_location_city string?)

(spec/def ::patient_location_region_code uppercase-letter-spec)

(spec/def ::patient_location_country_code (spec/with-gen (spec/and string? #(re-matches #"[A-Z]{2}" %))
                                            (fn [] (spec/gen #{"BZ" "FR" "NM" "FT" "OP" "AR" "US"}))))

(spec/def ::doctor_name name-spec)

(spec/def ::doctor_enrollment_type #{"MN" "MP" "OP" "ON" "CRM" "MM"})

(spec/def ::doctor_enrollment numero-string-spec)

(spec/def ::doctor_enrollment_prov uppercase-letter-spec)

(spec/def ::medicines (spec/coll-of map?)) 

(spec/def ::practices (spec/coll-of map?)) 

(spec/def ::name string?)

(spec/def ::document_number string?)

(spec/def ::start_date string?)

(spec/def ::provider string?)

(spec/def ::specialty string?)

(spec/def ::cuil string?)

(spec/def ::appointment_id string?)

(spec/def ::appointment_date string?)

(spec/def ::user string?)

(spec/def ::doctor (spec/keys :req-un [::name ::doctor_id ::cuil]))

(spec/def ::patient (spec/keys :req-un [::name ::document_number]))

(spec/def :call_ended/event_object (spec/keys :req-un [::call_id
                                                       ::patient_name
                                                       ::patient_id
                                                       ::provider_id
                                                       ::doctor_id
                                                       ::call_resolution
                                                       ::call_start_datetime
                                                       ::patient_external_id
                                                       ::order_id
                                                       ::rest_indication
                                                       ::call_motive
                                                       ::call_duration
                                                       ::call_doctor_comment
                                                       ::call_diagnosis
                                                       ::call_cie10
                                                       ::call_patient_rating
                                                       ::call_doctor_rating
                                                       ::call_patient_comment
                                                       ::patient_age
                                                       ::patient_phone
                                                       ::patient_email
                                                       ::patient_gender
                                                       ::patient_location_latitude
                                                       ::patient_location_longitude
                                                       ::patient_location_city
                                                       ::patient_location_region_code
                                                       ::patient_location_country_code
                                                       ::doctor_name
                                                       ::doctor_enrollment_type
                                                       ::doctor_enrollment
                                                       ::doctor_enrollment_prov]))

(spec/def :prescription/event_object (spec/keys :req-un [::call_id
                                                         ::doctor_id
                                                         ::medicines
                                                         ::patient_name
                                                         ::patient_id
                                                         ::patient_external_id
                                                         ::provider_id]))

(spec/def :practices/event_object (spec/keys :req-un [::call_id
                                                      ::patient_name
                                                      ::patient_id
                                                      ::patient_external_id
                                                      ::provider_id
                                                      ::doctor_id
                                                      ::practices]))

(spec/def :case_closed/event_object (spec/keys :req-un [::patient_id
                                                        ::provider_id
                                                        ::patient_external_id
                                                        ::order_id]))

(spec/def :could_not_contact/event_object (spec/keys :req-un [::patient_id
                                                              ::provider_id
                                                              ::patient_external_id
                                                              ::order_id]))

(spec/def :appointment_created/event_object (spec/keys :req-un [::patient
                                                                ::start_date
                                                                ::provider
                                                                ::specialty
                                                                ::doctor
                                                                ::appointment_id
                                                                ::appointment_date
                                                                ::user]))

(spec/def :appointment_cancelled/event_object :appointment_created/event_object)

(defmulti message-type :event_type)

(defmethod message-type "CALL_ENDED" [_] 
  (spec/keys :req-un [::datetime ::event_type :call_ended/event_object]))

(defmethod message-type "PRACTICES" [_]
  (spec/keys :req-un [::datetime ::event_type :practices/event_object]))

(defmethod message-type "CASE_CLOSED" [_]
  (spec/keys :req-un [::datetime ::event_type :case_closed/event_object]))

(defmethod message-type "PRESCRIPTION" [_]
  (spec/keys :req-un [::datetime ::event_type :prescription/event_object]))

(defmethod message-type "COULD_NOT_CONTACT" [_]
  (spec/keys :req-un [::datetime ::event_type :could_not_contact/event_object]))

(defmethod message-type "APPOINTMENT_CREATED" [_]
  (spec/keys :req-un [::datetime ::event_type :appointment_created/event_object]))

(defmethod message-type "APPOINTMENT_CANCELLED" [_]
  (spec/keys :req-un [::datetime ::event_type :appointment_cancelled/event_object]))

(spec/def :message/message (spec/multi-spec message-type :event_type))


(tests
 (->> (java.time.Instant/parse "2025-04-03T12:16:59.737Z")
      (spec/valid? ::datetime)) := true

 (spec/valid? ::datetime "2024-07-12T01:17:19.813Z") := true

 (spec/valid? ::datetime "2024-07-12") := false

 (spec/valid? ::datetime "El chunior!!!") := false

 (spec/valid? ::datetime "2024-07-12T01:17") := false

 (spec/valid? ::event_type "Chingada") := false

 (spec/valid? ::event_type "(spec/valid? ::event_type \"Chingada\")") := false

 (spec/valid? ::event_type "COULD_NOT_CONTACT") := true

 (spec/valid? ::call_id "2232snspas3223") := false

 (spec/valid? ::call_id (random-uuid)) := true

 (spec/valid? ::call_id "e4ff8723-9257-45ad-a9ab-4639bb106648") := true

 (spec/valid? ::patient_name "Christian Ciero") := true

 (spec/valid? ::patient_name "Christian pio") := false

 (spec/valid? ::patient_name "vhristian Ciero") := false

 (spec/valid? ::patient_name "Christian Ciero Martinez") := true

 (spec/valid? ::patient_id "Christian Ciero") := false

 (spec/valid? ::patient_id "12 23") := false

 (spec/valid? ::patient_id "125ñlsa1") := false

 (spec/valid? ::patient_id "3566") := true

 (spec/valid? ::rest_indication "sds") := false

 (spec/valid? ::rest_indication true) := true

 (spec/valid? ::rest_indication false) := true

 (spec/valid? ::rest_indication (not false)) := true

 (spec/valid? ::patient_age 23) := true

 (spec/valid? ::patient_age "23") := false

 (spec/valid? ::patient_age 185) := false

 (spec/valid? ::patient_age 120) := false

 (spec/valid? ::patient_age 119) := true

 (spec/valid? ::patient_age -23) := false

 (spec/valid? ::patient_phone "15565+") := false

 (spec/valid? ::patient_phone "-+332342") := false

 (spec/valid? ::patient_phone "-232332342") := false

 (spec/valid? ::patient_phone "+10332342") := true

 (spec/valid? ::patient_location_country_code "sd") := false

 (spec/valid? ::patient_location_country_code "SD") := true

 (spec/valid? ::patient_location_country_code "ABC") := false

 (spec/valid? ::patient_location_country_code "ABc") := false

 (spec/valid? ::patient_location_country_code "aBC") := false

 (spec/valid? ::patient_location_country_code "Ab") := false

 (spec/valid? ::patient_location_country_code "552") := false

 (spec/valid? ::order_id "2024-02-05 10:45") := true

 (spec/valid? ::order_id "2024/02/05 10:45") := true

 (spec/valid? ::order_id "2024-02-05 10/45") := false

 (spec/valid? ::order_id "2024-02/05 10:45") := false ;; Test falla

 (spec/valid? ::order_id "2024/02/05 10/45") := false

 (spec/valid? :message/message {:datetime (Instant/now)
                                :event_type "PRESCRIPTION"
                                :event_object {:call_id (str (random-uuid))
                                               :doctor_id "64564564"
                                               :medicines [{:active "string"
                                                            :pot "string"
                                                            :cont 5445
                                                            :form "string"
                                                            :codProd "string"
                                                            :codDrug "string"
                                                            :instructions "string"
                                                            :drugs "string"
                                                            :packaging "string"
                                                            :price 322323
                                                            :quantity 213241
                                                            :highCostDrug boolean}]
                                               :patient_name "Juan Castro"
                                               :patient_id "55454554"
                                               :patient_external_id "16546456"
                                               :provider_id (str (random-uuid))}}) := true
 (spec/valid? :message/message
              {:datetime (Instant/now)
               :event_type "PRACTICES"
               :event_object {:call_id (str (random-uuid)),
                              :doctor_id "65654",
                              :patient_external_id "65465",
                              :patient_id "466565",
                              :patient_name "Juan Mecen",
                              :practices [{:code 465456, :name "Jusn Al"}],
                              :provider_id (str (random-uuid))}}) := true


 (spec/valid? :message/message {:datetime (java.time.Instant/now)
                                :event_type "COULD_NOT_CONTACT"
                                :event_object {:patient_id "12121"
                                               :provider_id (str (random-uuid))
                                               :patient_external_id "455656"
                                               :order_id "2024-01-19 11:23"}}) := true

 (spec/valid? :message/message {:datetime (java.time.Instant/now)
                                :event_type "CASE_CLOSED"
                                :event_object {:order_id "2025/08/25 18:56",
                                               :patient_external_id "14565",
                                               :patient_id "4654456",
                                               :provider_id (str (random-uuid))}}) := true

 (spec/valid? :message/message
              {:datetime (java.time.Instant/now)
               :event_type "APPOINTMENT_CREATED"
               :event_object {:patient {:name "Juan Monn"
                                        :document_number "45454"}
                              :start_date "44545460"
                              :provider ""
                              :specialty "cualquiera"
                              :doctor {:name "Juan Fmuero"
                                       :doctor_id "45656456"
                                       :cuil "654565454"}
                              :appointment_id ""
                              :appointment_date "dss"
                              :user "Chan Gil"}}) := true

 (spec/valid? :message/message
              {:datetime (java.time.Instant/now)
               :event_type "APPOINTMENT_CANCELLED"
               :event_object {:patient {:name "Juan Monn"
                                        :document_number "45454"}
                              :start_date "44545460"
                              :provider ""
                              :specialty "cualquiera"
                              :doctor {:name "Juan Fmuero"
                                       :doctor_id "45656456"
                                       :cuil "654565454"}
                              :appointment_id ""
                              :appointment_date "dss"
                              :user "Chan Gil"}}) := true


 (spec/valid? :message/message (clojure.edn/read-string
                                (slurp
                                 (clojure.java.io/resource "payload_model.edn")))) := true

 :rcf)


(comment

  (ns-unmap *ns* 'message-type)
  (ns-unmap *ns* 'event-object)

  (java.time.Instant/now)
  
  (gen/generate (spec/gen ::event_type))

  (gen/sample (spec/gen ::event_type))

  (gen/generate (spec/gen ::patient_email))

  (spec/exercise ::patient_email)

  (spec/exercise ::patient_external_id)

  (gen/generate (spec/gen ::patient_name))

  (gen/sample (spec/gen ::patient_name))

  (gen/sample (spec/gen ::patient_id))

  (gen/sample (spec/gen ::doctor_id))

  (gen/generate (spec/gen ::patient_id))

  (gen/generate (spec/gen ::datetime))

  (gen/generate (spec/gen ::call_start_datetime))

  (gen/generate (spec/gen ::patient_age))

  (gen/sample (spec/gen ::patient_age))

  (gen/generate (spec/gen ::doctor_enrollment_prov))

  (gen/generate (spec/gen ::patient_phone))

  (gen/generate (spec/gen ::call_id)) 

  (gen/generate (spec/gen ::order_id))
 
  (gen/generate (spec/gen :message/message)) 
  
  (spec/explain :message/message (clojure.edn/read-string
                                  (slurp
                                   (clojure.java.io/resource "payload_model.edn"))))
  
(gen/generate (spec/gen :call_ended/event_object))
  :rcf)