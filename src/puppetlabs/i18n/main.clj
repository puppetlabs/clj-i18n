(ns puppetlabs.i18n.main
  "Some I18N examples"
  (:gen-class)
  (:require [puppetlabs.i18n.core :as i18n :refer [tr]]))

(defn -main []
  ;; You have to use a literal string as the first argument to i18n/tr
  ;; This code ensures that translators never see this message since
  ;; xgettext doesn't see the string
  (let [dont-do-this "Current Locale: {0}"]
    (println (tr dont-do-this (.toString (i18n/current-locale)))))

  ;; Very simple localization
  (println (tr "Welcome! This is localized"))

  ;; String with arguments
  (let [nprog 3 nmonths 5]
    (println (i18n/tr "It took {0} programmers {1} months to implement this"
                      nprog nmonths)))

  ;; String with special formatting
  (let [nbikes 9000000]
    (println (i18n/tr "There are {0,number,integer} bicycles in Beijing"
                      nbikes))))
