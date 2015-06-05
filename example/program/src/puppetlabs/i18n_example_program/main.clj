(ns puppetlabs.i18n-example-program.main
  (:gen-class)
  (:require [puppetlabs.i18n.core :as i18n :refer [trs tru]]))

(defn -main
  "I don't do a whole lot."
  []
  ;; The bundle to use is based on the namespace we are working in, from
  ;; which we derive the name of the Leiningen project. We have one catalog
  ;; per Leiningen project
  (println "Using bundle" (i18n/bundle-name))

  ;; Simple system message
  (println (trs "Hello, World"))

  ;; Interpolate using java.text.MessageFormat
  (println (trs "{0} bottles on the wall" 99))

  ;; You can also translate a message first, and later interpolate values
  ;; into the translation. Note that i18n/fmt takes the values to
  ;; interpolate as a seq
  (let [msg (trs "If we take {0} bottles away, we have {1} left")]
    (println (i18n/fmt (i18n/system-locale) msg [7 92]))))
