(ns puppetlabs.i18n.core
  (:gen-class)
  (:require [clojure.java.io :as io]))

;;; General setup/info
(defn info
  "Read the locales.clj file created by our makefile and return the
  resulting map"
  []
  ;; this data won't change while the program is running and should be
  ;; memoized
  (read-string (slurp (io/resource "locales.clj"))))

(defn available-locales
  "Return a list of all the locales for which we have translations based on
  the information in locales.clj generated at compile time"
  []
  (:locales (info)))

(defn bundle-name
  "Return the name of the resource bundle we should use based on the
  information in locales.clj"
  []
  (first (:bundles (info))))

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

;;; ResourceBundles
(defmulti get-bundle
  "Get the java.util.ResourceBundle for the given locale (a string)"
  class)

(defmethod get-bundle java.lang.String [loc]
  (get-bundle (string-as-locale loc)))

(defmethod get-bundle java.util.Locale [loc]
  (java.util.ResourceBundle/getBundle (bundle-name) loc))

;;; Message lookup/formatting
(defn lookup
  "Look msg up in the resource bundle for loc. If there is no resource
  bundle for it, or the resource bundle does not contain an entry for msg,
  return msg itself"
  ([msg] (lookup (user-locale) msg))
  ([loc msg]
   (try
     (.getString (get-bundle loc) msg)
     (catch java.util.MissingResourceException e
       ;; This gets thrown both when there is no bundle for the given locale
       ;; and when the bundle exists but does not contain a key for msg
       msg))))

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

(defn tr
  "Translate a message into the current locale, interpolating as needed"
  [msg & args]
  (let [loc (user-locale)]
    (fmt loc (lookup loc msg) (to-array args))))
