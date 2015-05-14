(ns puppetlabs.i18n.core-test
  (:require [clojure.test :refer :all]
            [puppetlabs.i18n.core :refer :all]))

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
