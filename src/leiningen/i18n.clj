(ns leiningen.i18n
  "Plugin for i18n tasks. Start by using i18n init"
  (:require [leiningen.core.main :as l]
            [leiningen.core.eval :as e]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.java.shell :as sh :refer [sh]]))

(defn help
  []

" The i18n tooling expects that you have GNU make and the gettext tools
  installed.

  Invoking this command without arguments will generate a Makefile with
  the appropriate gettext/msgfmt calls and generates the relevant message
  files, putting them in the lein :compile-path.
")

(defn path-join
  [root & args]
  (apply str root (map #(str java.io.File/separator %) args)))

(defn bundle-package-name
  "Return the Java package name in which our resource bundles should
  live. This is the project's group and name in dotted notation. If the
  project has no group, we use 'nogroup'"
  [project]
  (namespace-munge
   (str (:group project "nogroup") "."
        (clojure.string/replace (:name project) "/" "."))))

(defn makefile-i18n-path
  [{:keys [target-path] :as project}]
  (path-join target-path "Makefile.i18n"))

(defn copy-makefile-to-target
  [{:keys [target-path compile-path] :as  project}]
  (.mkdirs (io/file target-path))
  (let [package (str "PACKAGE=" (bundle-package-name project))
        prefix (str "COMPILE_PATH=" compile-path)
        contents (-> (io/resource "leiningen/i18n/Makefile")
                     io/input-stream
                     slurp
                     (clojure.string/replace #"PACKAGE=.*" package)
                     (clojure.string/replace #"COMPILE_PATH=.*" prefix))]
    (spit (io/as-file (makefile-i18n-path project))
          contents)))

(defn i18n-make
  [project]
  (l/debug "Running 'make i18n'")
  (copy-makefile-to-target project)
  (sh "make" "i18n" "-f" (makefile-i18n-path project)))

(defn abort
  [& rest]
  (apply l/abort (concat '("Error:") rest (list "\n\n" (help)))))

(defn i18n
  [project command]

  (when-not (:root project)
    (abort "The i18n plugin can only be run inside a project"))
  
  (i18n-make project))
