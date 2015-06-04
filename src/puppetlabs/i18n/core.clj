(ns puppetlabs.i18n.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.set]
            [clojure.string :as str]))

;;; General setup/info
(defn info-files
  "Find all locales.clj files on the context class path and return them as
  a seq of URLs"
  []
  (-> (Thread/currentThread)
           .getContextClassLoader
           (.getResources "locales.clj")
           enumeration-seq))

(defn infos
  "Read all the locales.clj files on the classpath and return them as an
  array of maps. There is one locales.clj file per Clojure project.

  Each entry in the map uses the following keys:
    :locales - set of the locale names in which translations are available
    :package - the toplevel package name (lein project name) for this library
    :bundle  - the name of the resource bundle"
  []
  ;; this will not change over the lifetime of the program and should be
  ;; memoized; there are a few thunks involving infos that could be
  ;; precomputed in a similar manner
  (map #(-> % slurp read-string) (info-files)))

(defn info-map
  "Turn the result of infos into a map mapping the package name to locales
  and bundle name"
  []
  (reduce
   (fn [map item]
     (assoc map (:package item)
            (select-keys item [:locales :bundle])))
   {} (infos)))

(defn available-locales
  "Return a list of all the locales for which we have translations based on
  the information in locales.clj generated at compile time"
  []
  ;; intersection would be another option; in a well-managed code base, the
  ;; assumption is that all bundles are available in the same locales.
  ;; If there are differences, we make a best effort to give users as much
  ;; in their desired language as we can
  (apply clojure.set/union (map :locales (infos))))

;;; Handling the current locale
(defn system-locale
  "Get the globally set locale"
  []
  (. java.util.Locale getDefault))

(def ^:dynamic *locale*
  "The current user locale. You should not modify this variable
  directly. Instead, call user-locale to read its value and use
  with-user-locale to evaluate forms with the user locale set to a specific
  value"
  nil)

(defmacro with-user-locale
  "Evaluate body with the user locale set to locale"
  [locale & body]
  `(let [locale# ~locale]
     (if (instance? java.util.Locale locale#)
       (binding [*locale* locale#] ~@body)
       (throw (IllegalArgumentException.
               (str "Expected java.util.Locale but got "
                    (.getName (.getClass locale#))))))))

(defn user-locale []
  "Return the user's preferred locale. If none is set, return the system
  locale"
  (or *locale* (system-locale)))

;; @todo lutter 2015-04-21: there are various formats of string locales
;; we need to make sure we have the right one. For example, "en_US" leads
;; to a bad locale, whereas "en-us" works
(defn string-as-locale [loc]
  (java.util.Locale/forLanguageTag loc))

(defn message-locale
  "The locale of the untranslated messages. This is used as a fallback if
  we don't have translations for any of the locales that the user would
  like to have. If you change this, it also needs to be changed in the
  Makefile that generates resource bundles"
  []
  (string-as-locale "en"))

;;; ResourceBundles
(defn bundle-for-namespace
  "Find the name of the ResourceBundle for the given namespace name"
  [namespace]
  (:bundle
   (get (info-map)
        (first (filter #(.startsWith namespace %)
                       (reverse (sort-by count (keys (info-map)))))))))

(defmacro bundle-name
  "Return the name of the ResourceBundle that the trs and tru macros will use"
  []
  `(bundle-for-namespace ~(namespace-munge *ns*)))

(defn get-bundle
  "Get the java.util.ResourceBundle for the given locale (a string)"
  [namespace loc]
  (try
    (let [base-name (bundle-for-namespace namespace)]
      (and base-name
           (java.util.ResourceBundle/getBundle base-name loc)))
    (catch java.lang.NullPointerException e
      ;; base-name or loc were nil
      nil)
    (catch java.util.MissingResourceException e
      ;; no bundle for the base-name and/or locale
      nil)))

;;; Message lookup/formatting
(defn lookup
  "Look msg up in the resource bundle for namespace in the locale loc. If
  there is no resource bundle for it, or the resource bundle does not
  contain an entry for msg, return msg itself"
  [namespace loc msg]
  (let [bundle (get-bundle namespace loc)]
    (if bundle
      (try
        (.getString bundle msg)
        (catch java.util.MissingResourceException e
          ;; no key for msg
          msg))
      msg)))

(defn fmt
  "Use msg as a java.text.MessageFormat and interpolate the args
  into it according to locale loc.

  See the documentation for java.text.MessageFormat for the details of what
  patterns are available."
  ([msg args] (fmt (user-locale) msg args))
  ([loc msg args]
   ;; we might want to cache these MessageFormat's in some way
   ;; maybe in a size-bounded LRU cache
   (.format (new java.text.MessageFormat msg loc) args)))

(defn translate
  "Translate a message into the given locale, interpolating as
  needed. Messages are looked up in the resource bundle associated with the
  given namespace"
  [namespace loc msg & args]
  (fmt loc (lookup namespace loc msg) (to-array args)))

(defmacro tru
  "Translate a message into the user's locale, interpolating as needed"
  [& args]
  `(translate ~(namespace-munge *ns*) (user-locale) ~@args))

(defmacro trs
  "Translate a message into the system locale, interpolating as needed"
  [& args]
  `(translate ~(namespace-munge *ns*) (system-locale) ~@args))

;;
;; Ring middleware for language negotiation
;;

(defn as-number
  "Parse a string into a float. If the string is not a valid number,
  return 0"
  [s]
  (cond
    (nil? s) 0
    (number? s) s
    (string? s)
    (try
      (Double/parseDouble s)
      (catch NumberFormatException _ 0))
    :else 0))

(defn parse-http-accept-header
  "Parses HTTP Accept header and returns sequence of [choice weight] pairs
  sorted by weight."
  [header]
  (sort-by second #(compare %2 %1)
           (remove
            ;; q values can only have three decimal places; we need to
            ;; remove all q values that are 0
            (fn [[lang q]] (< q 0.0001))
            (for [choice (remove str/blank? (str/split (str header) #","))]
              (let [[lang q] (str/split choice #";")]
                [(str/trim lang)
                 (or (when q (as-number (get (str/split q #"=") 1)))
                     1)])))))

(defn negotiate-locale
  "Given a string sequence of wanted locale (sorted by preference) and a
  set of available locales, all expressed as strings, find the first string
  in wanted that is available, and return the corresponding Locale object.

  This function will always return a locale. If we can't negotiate a
  suitable locale, we fall back to the message-locale"
  [wanted available]
  ;; @todo lutter 2015-05-20: if wanted contains only a country-specific
  ;; variant, and we have the general variant, we might want to match those
  ;; up if we don't find a better match. This is not what the HTTP spec
  ;; says, but helps work around broken browsers.
  ;;
  ;; For example, if we have locales #{"de" "es"} available, and the user
  ;; asks for ["de_AT" "fr"], we should probably return "de" rather than
  ;; falling back to the message locale
  (if-let [loc (some available wanted)]
    (string-as-locale loc)
    (message-locale)))

(defn locale-negotiator
  "Ring middleware that performs locale negotiation.

  It parses the Accept-Language header and selects the best available
  locale according to the user's preference. That locale is set as the user
  locale while evaluating handler."
  [handler]
  (fn [request]
    ;; @todo lutter 2015-06-03: remove our hand-crafted language
    ;; negotiation and use java.util.Locale/filterTags instead; this would
    ;; remove the gnarly parse-http-accept-header business. Requires Java 8
    (let [headers (:headers request)
          parsed  (parse-http-accept-header (get headers "accept-language"))
          wanted  (mapv first parsed)
          negotiated (negotiate-locale wanted (available-locales))]
      (with-user-locale negotiated (handler request)))))
