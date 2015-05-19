(ns puppetlabs.i18n.core-test
  (:require [clojure.test :refer :all]
            [puppetlabs.i18n.core :refer :all]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]))

;; Set the JVM's default locale so we run in a known environment
(. java.util.Locale setDefault (string-as-locale "en-US"))

(def de (string-as-locale "de-DE"))

(def welcome_en "Welcome! This is localized")
(def welcome_de "Willkommen ! Draußen nur Kännchen")

(deftest handling-of-user-locale
  (testing "user-locale defaults to system-locale"
    (is (= (system-locale) (user-locale))))
  (testing "with-user-locale changes user-locale"
    (with-user-locale de
      (is (= de (user-locale)))))
  (testing "user-locale is conveyed to future"
    (with-user-locale de
      (is (= de
             @(future (user-locale))))))
  (testing "with-user-locale fails when not passed a java.util.Locale"
    (is (thrown? IllegalArgumentException (with-user-locale "de" nil)))))

(deftest test-tru
  (testing "tru with no user locale"
    (is (= welcome_en (tru welcome_en))))
  (testing "tru in German"
    (with-user-locale de
      (is (= welcome_de (tru welcome_en))))))

(deftest test-trs
  (testing "trs with no user locale"
    (is (= welcome_en (trs welcome_en))))
  (testing "trs with a user locale"
    (with-user-locale de
      (is (= welcome_en (trs welcome_en))))))

;;
;; Helper files in dev-resources; they have the same format as the
;; locales.clj file that the Makefile generates
;;
(defn one-locale-file
  []
  [(io/resource "test-locales/de-ru-es.clj")])

(defn two-locale-files
  []
  [(io/resource "test-locales/de-ru-es.clj")
   (io/resource "test-locales/fr-it.clj")])

(deftest test-infos
  (with-redefs
    [puppetlabs.i18n.core/info-files one-locale-file]
    (testing "info-map"
      (is (= ["example.i18n"] (keys (info-map))))
      (is (= "example.i18n.Messages" (:bundle ((info-map) "example.i18n")))))
    (testing "available-locales"
      (is (= #{"de" "ru" "es"} (available-locales)))))
  (with-redefs
    [puppetlabs.i18n.core/info-files two-locale-files]
    (testing "info-map-two"
      (is (= #{"example.i18n" "other.i18n"} (into #{} (keys (info-map)))))
      (is (= "example.i18n.Messages" (:bundle ((info-map) "example.i18n")))))
    (testing "available-locales-two"
      (is (= #{"it" "fr" "de" "ru" "es"} (available-locales))))
    (testing "bundle-for-namespace"
      (is (= "example.i18n.Messages" (bundle-for-namespace "example.i18n")))
      (is (= "example.i18n.Messages"
             (bundle-for-namespace "example.i18n.dog.cat")))
      (is (nil? (bundle-for-namespace "example")))
      (is (= "other.i18n.Messages"
             (bundle-for-namespace "other.i18n.abbott.costello"))))))
