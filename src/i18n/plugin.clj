(ns i18n.plugin
  (:require [leiningen.core.main :as l]
            [robert.hooke :as rh]
            [leiningen.compile]
            [clojure.java.shell :as sh :refer [sh]]))

(defn compile-hook
  [task project]
  (l/debug "i18n: running 'make i18n'")
  (sh "make" "i18n")
  (task project))

(defn hooks []
  (rh/add-hook #'leiningen.compile/compile #'compile-hook))
