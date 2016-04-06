(ns i18n.plugin
  (:require [leiningen.core.main :as l]
            [robert.hooke :as rh]
            [leiningen.compile]
            [leiningen.i18n :as l-i18n]
            [clojure.java.shell :as sh :refer [sh]]))

(defn compile-hook
  [task project]
  (l-i18n/i18n-make project)
  (task project))

(defn hooks []
  (rh/add-hook #'leiningen.compile/compile #'compile-hook))
