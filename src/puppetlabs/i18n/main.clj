(ns puppetlabs.i18n.main
  "Some I18N examples"
  (:gen-class)
  (:require [puppetlabs.i18n.core :as i18n :refer [tru trs trun trsn mark]]))

(def ^:const const-string (mark "I do not speak German"))

;; Some simple examples of using tru/trs
;; The unit tests rely on the message catalog and translation generated for
;; the messages in this file. If you make changes to the messages here, you
;; might also have to change the tests.
(defn -main []
  ;; You have to use a literal string as the first argument to i18n/tr
  ;; This code ensures that translators never see this message since
  ;; xgettext doesn't see the string
  (let [dont-do-this "Current Locale: {0}"]
    (println (tru dont-do-this (str (i18n/user-locale)))))

  ;; Very simple localization
  (println (trs "Welcome! This is localized"))

  ;; Localizing a previously-extracted string
  (println (tru const-string))

  ;; Localizing an empty string
  (println (tru ""))
  (println "-----")
  (println (trs ""))
  (println "-----")
  (println (trun "" "" 1))
  (println "-----")
  (println (trun "" "" 6))
  (println "-----")
  (println (trun "" "non empty string" 1))
  (println "-----")
  (println (trun "non empty string" "" 6))
  (println "-----")
  (println (trsn "" "" 1))
  (println "-----")
  (println (trsn "" "" 6))
  (println "-----")
  (println (trsn "" "non empty string" 1))
  (println "-----")
  (println (trsn "non empty string" "" 6))
  (println "-----")

  ;; Very simple plural system localization
  (doseq [beers (range 5 0 -1)]
    (println (trsn "There is one bottle of beer on the wall."
                   "There are {0} bottles of beer on the wall."
                   beers)))

  ;; String with arguments
  (let [nprog 3
        nmonths 5]
    (println (i18n/tru "It took {0} programmers {1} months to implement this"
                       nprog nmonths)))

  ;; String with special formatting
  (let [nbikes 9000000]
    (println (i18n/tru "There are {0,number,integer} bicycles in Beijing"
                      nbikes))))
