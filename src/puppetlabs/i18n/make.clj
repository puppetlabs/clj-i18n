(ns puppetlabs.i18n.make
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh :refer [sh]])  )

(defn path-join
  [root & args]
  (apply str root (map #(str java.io.File/separator %) args)))

(defn bundle-package-name
  "Return the Java package name in which our resource bundles should
  live. This is the project's group and name in dotted notation. If the
  project has no group, we use 'nogroup'"
  [package-name package-group]
  (namespace-munge
   (str (or package-group "nogroup") "."
        (clojure.string/replace package-name "/" "."))))

(defn makefile-i18n-path
  [target-path]
  (path-join target-path "Makefile.i18n"))

(defn copy-makefile-to-target
  [package-name package-group target-path compile-path]
  (.mkdirs (io/file target-path))
  (let [package (str "PACKAGE=" (bundle-package-name package-name package-group))
        prefix (str "COMPILE_PATH=" compile-path)
        contents (-> (io/resource "leiningen/i18n/Makefile")
                     io/input-stream
                     slurp
                     (clojure.string/replace #"PACKAGE=.*" package)
                     (clojure.string/replace #"COMPILE_PATH=.*" prefix))]
    (spit (io/as-file (makefile-i18n-path target-path))
          contents)))

(defn i18n-make
  [package-name package-group target-path compile-path]
  (copy-makefile-to-target package-name package-group target-path compile-path)
  (sh "make" "i18n" "-f" (makefile-i18n-path target-path)))

(defn -main []
  (i18n-make "i18n" "puppetlabs" "target" "target/classes")
  (shutdown-agents))

