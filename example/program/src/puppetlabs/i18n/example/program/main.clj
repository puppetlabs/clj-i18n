(ns puppetlabs.i18n.example.program.main
  (:gen-class)
  (:require [puppetlabs.i18n.core :as i18n :refer [tr]]))

(defn -main
  "I don't do a whole lot."
  []
  (println "Using bundle" (i18n/bundle-name))
  (println (tr "Hello, World"))
  (println (tr "{0} bottles on the wall" 99)))
