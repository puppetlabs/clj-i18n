(ns leiningen.i18n
  "Plugin for i18n tasks. Start by using i18n init"
  (:require [leiningen.core.main :as l]
            [leiningen.core.eval :as e]
            [leiningen.i18n.utils :as utils]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as cstr]
            [clojure.java.shell :as sh :refer [sh]]
            [cpath-clj.core :as cp])
  (:import
   (java.io File)))

(defn help
  []

" The i18n tooling expects that you have GNU make and the gettext tools
  installed.

  The following subtasks are supported:
    init  - add i18n tool support to the project, then run 'make help'
    make  - invoke 'make i18n'
")

(defn path-join
  [ & args ]
  (apply str (cons (first args)
                   (map #(str java.io.File/separator %) (rest args)))))

(defn dev-resources-path
  "Return the first path in the project's resource-paths that ends in
  dev-resources or create dev-resources in the first :source-paths"
  [project]
  (or
   (first (filter #(.endsWith % "dev-resources") (:resource-paths project)))
   (l/abort "You must have a dev-resources directory in your project's resource-paths")))

(defn dev-resources-dir
  "Return the dev-resources-path as a file. Create the directory if it does
  not exist"
  [project]
  (let [dir (io/as-file (dev-resources-path project))]
    (if (or (.isDirectory dir) (.mkdirs dir))
      dir
      (l/abort (str "Could not create directory " dir)))))

(defn bundle-package-name
  "Return the Java package name in which our resource bundles should
  live. This is the project's group and name in dotted notation. If the
  project has no group, we use 'nogroup'"
  [project]
  (namespace-munge
   (str (or (:group project) "nogroup") "."
        (cstr/replace (:name project) "/" "."))))

(defn copy-makefile-to-dev-resources
  [project]
  (let [dest (path-join (dev-resources-dir project) "Makefile.i18n")
        makefile (io/resource "leiningen/i18n/Makefile")]
    (spit (io/as-file dest)
          (-> (utils/replace-first-in-multiline
                (slurp makefile)
                #"BUNDLE=.*"
                (str "BUNDLE=" (bundle-package-name project)))
              (utils/replace-first-in-multiline
                #"POT_NAME=.*"
                (str "POT_NAME=" (:name project) ".pot"))))))

(defn copy-scripts-to-dev-resources
  [project]
  (let [dest-dir (io/as-file (path-join (dev-resources-dir project) "i18n" "bin"))
        scripts (cp/resources (io/resource "leiningen/i18n/bin"))]
    (when-not (.exists dest-dir)
      (.mkdirs dest-dir))
    (doseq [[basename [script-uri]] scripts]
      (let [dest-file (io/as-file (path-join (.getPath dest-dir) basename))]
        (io/copy (io/input-stream script-uri) dest-file)
        (.setExecutable dest-file true false))))) ; second false means set it for everyone

(defn project-file
  "Construct a path in the project's root by appending rest to it and
  return a file"
  [project & rest]
  (let [root (:root project)]
    (io/as-file (apply path-join (cons root rest)))))

(defn update-scripts-with-pot-name
  "CI script task needs to know the name of the POT file to be modified and
   committed. This will insert the correct name of the POT in the /bin files."
  [project]
  (let [dest (path-join (dev-resources-dir project) "i18n" "bin" "update-pot.sh")
        pot-script (io/resource "leiningen/i18n/bin/update-pot.sh")]
    (spit (io/as-file dest)
          (utils/replace-first-in-multiline
               (slurp pot-script)
               #"POT_NAME=.*"
               (str "POT_NAME=" (:name project) ".pot")))))

(defn ensure-contains-line
  "Make sure that file contains the given line, if not append it. If file
  does not exist yet, create it and put line into it"
  [^File file line]
  (if (.isFile file)
    (let [contents (slurp file)]
      (if-not (.contains contents line)
        (do
          (if-not (.endsWith contents "\n")
            (spit file "\n" :append true))
          (spit file (str line "\n") :append true))))
      (spit file (str line "\n"))))

(defn edit-toplevel-makefile
  "Add a line to include Makefile.i18n to an existing Makefile or create a
  new one with just the include statement"
  [project]
  (let [include-line "include dev-resources/Makefile.i18n"
        makefile (project-file project "Makefile")]
    (ensure-contains-line makefile include-line)))

(defn edit-gitignore
  "Add generated i18n files that should not be checked in to .gitignore"
  [project]
  (let [lines ["/resources/locales.clj" "/dev-resources/i18n/bin"]
        gitignore (project-file project ".gitignore")]
    (doseq [line lines]
      (ensure-contains-line gitignore line))))

(defn i18n-init
  [project]
  (l/info "Setting up Makefile; don't forget to check it in")
  (copy-makefile-to-dev-resources project)
  (l/info "Adding i18n scripts in `dev-resources/i18n/bin`")
  (copy-scripts-to-dev-resources project)
  (update-scripts-with-pot-name project)
  (edit-toplevel-makefile project)
  (edit-gitignore project))

(defn i18n-make
  [project]
  (l/info "Running 'make i18n'")
  (let [{:keys [exit out err]} (sh "make" "i18n")]
    (print out)
    (binding [*out* *err*]
      (print err))
    (when-not (zero? exit)
      (l/abort))))

(defn abort
  [& rest]
  (apply l/abort (concat '("Error:") rest (list "\n\n" (help)))))

(defn i18n
  [project command]

  (if-not (:root project)
    (abort "The i18n plugin can only be run inside a project"))

  (condp = command
    nil       (abort "You need to provide a subcommand")
    "init"    (i18n-init project)
    "make"    (i18n-make project)
    (abort "Unexpected command:" command)))
