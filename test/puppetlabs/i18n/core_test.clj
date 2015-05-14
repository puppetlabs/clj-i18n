(ns puppetlabs.i18n.core-test
  (:require [clojure.test :refer :all]
            [puppetlabs.i18n.core :refer :all]))

(def de (string-as-locale "de-DE"))

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
