;; ---------------------------------------------------------
;; Build Script
;;
;; Build project and package for deployment
;; - `uberjar` packaged service for deployment
;; - `clean` remove all build assets and jar files
;;
;; All functions are passed command line arguments
;; - `nil` is passed if there are no arguments
;;
;;
;; tools.build API commands
;; - `create-basis` create a project basis
;; - `copy-dir` copy Clojure source and resources into a working dir
;; - `compile-clj` compile Clojure source code to classes
;; - `delete` - remove path from file space
;; - `write-pom` - write pom.xml and pom.properties files
;; - `jar` - to jar up the working dir into a jar file
;;
;; ---------------------------------------------------------

(ns build
  (:require
   [clojure.tools.build.api :as build-api]
   [clojure.pprint :as pprint]
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]))


;; ---------------------------------------------------------
;; Project configuration

(def version (-> (read-config (io/resource "config.edn") {:profile :prod}) :version))

(def project-config
  "Project configuration to support all tasks"
  {:class-directory "target/classes"
   :main-namespace  'sanatoriocolegiales/lad-webhook.service
   :project-basis   (build-api/create-basis)
   :uberjar-file    (str "target/sanatoriocolegiales-lad-webhook-standalone-" version ".jar")})

(defn config
  "Display build configuration"
  [config]
  (pprint/pprint (or config project-config)))

;; End of Build configuration
;; ---------------------------------------------------------


;; ---------------------------------------------------------
;; Testing tasks
;; - optionally include a test runner
;; End of Testing tasks
;; ---------------------------------------------------------


;; ---------------------------------------------------------
;; Build tasks

(defn clean
  "Remove a directory
  - `:path '\"directory-name\"'` for a specific directory
  - `nil` (or no command line arguments) to delete `target` directory
  `target` is the default directory for build artefacts
  Checks that `.` and `/` directories are not deleted"
  [directory]
  (when
   (not (contains? #{"." "/"} directory))
    (build-api/delete {:path (or (:path directory) "target")})))


(defn uberjar
  "Create an archive containing Clojure and the build of the project
  Merge command line configuration to the default project config"
  [options]
  (let [config (merge project-config options)
        {:keys [class-directory main-namespace project-basis uberjar-file]} config]
    (clean "target")
    (build-api/copy-dir {:src-dirs   ["src" "resources"]
                         :target-dir class-directory})

    (build-api/compile-clj {:basis     project-basis
                            :class-dir class-directory
                            :src-dirs  ["src"]})

    (build-api/uber {:basis     project-basis
                     :class-dir class-directory
                     :main      main-namespace
                     :uber-file uberjar-file})))

;; End of Build tasks
;; ---------------------------------------------------------


;; ---------------------------------------------------------
;; Deployment tasks
;; - optional deployment tasks for services or libraries

;; End of Deployment tasks
;; ---------------------------------------------------------
