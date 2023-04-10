(ns puppetlabs.i18n.core-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [puppetlabs.i18n.core
     :refer [as-number
             available-locales
             bundle-for-namespace
             info-map'
             locale-negotiator
             negotiate-locale
             parse-http-accept-header
             string-as-locale
             system-locale
             trs
             trsn
             tru
             trun
             user-locale
             with-user-locale]])
  (:import (java.util Locale)))

;; Set the JVM's default locale so we run in a known environment
(. Locale setDefault (string-as-locale "en-US"))

(def de (string-as-locale "de-DE"))
(def eo (string-as-locale "eo"))
(def en (string-as-locale "en-US"))

(def welcome_en "Welcome! This is localized")
(def welcome_de "Willkommen ! Draußen nur Kännchen")
(def welcome_eo "Welcome_pseudo_localized")

(def one_bottle "There is one bottle of beer on the wall.")
(def n_bottles "There are {0} bottles of beer on the wall.")
(def one_bottle_en "There is one bottle of beer on the wall.")
(def one_bottle_de "Es gibt eine Flasche Bier an der Wand.")

(def six_bottle_en "There are 6 bottles of beer on the wall.")
(def six_bottle_de "Es gibt 6 Flaschen Bier an der Wand.")

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
      (is (= welcome_de (tru welcome_en)))))
  (testing "tru in Esperanto"
    ;; We use Esperanto as our test locale
    (with-user-locale eo
      (is (= welcome_eo (tru welcome_en)))))
  (testing "multiple argument formatting not in the POT file"
   (is (= "This is a format string with one, 2, three, 4 arguments" (trs "This is a format string with {0}, {1}, {2}, {3} arguments" "one" 2 "three" 4))))
  (testing "multiple argument formatting"
    (is (= "It took 5 programmers 10 months to implement this" (tru "It took {0} programmers {1} months to implement this" 5 10))))
  (testing "multiple argument formatting with a user locale"
    (with-user-locale de
      (is (= "5 Programmierer brauchten 10 Monat(e) um das zu implementieren" (tru "It took {0} programmers {1} months to implement this" 5 10))))))

(deftest test-trun
  (testing "trun with no user locale"
    (is (= one_bottle_en (trun one_bottle n_bottles 1)))
    (is (= six_bottle_en (trun one_bottle n_bottles 6))))
  (testing "trun in German"
    (with-user-locale de (is (= one_bottle_de (trun one_bottle n_bottles 1))))
    (with-user-locale de (is (= six_bottle_de (trun one_bottle n_bottles 6))))))


(deftest test-trs
  (testing "trs with no user locale"
    (is (= welcome_en (trs welcome_en))))
  (testing "trs with a user locale"
    (with-user-locale de
      (is (= welcome_en (trs welcome_en)))))
  (testing "multiple argument formatting not in the POT file"
    (is (= "This is a format string with one, 2, three, 4 arguments" (trs "This is a format string with {0}, {1}, {2}, {3} arguments" "one" 2 "three" 4))))
  (testing "multiple argument formatting"
    (is (= "It took 5 programmers 10 months to implement this" (trs "It took {0} programmers {1} months to implement this" 5 10))))
  (testing "multiple argument formatting with a user locale"
    (with-user-locale de
      (is (= "It took 5 programmers 10 months to implement this" (trs "It took {0} programmers {1} months to implement this" 5 10))))))

(deftest test-trsn
  (testing "trsn with no user locale"
    (is (= one_bottle_en (trsn one_bottle n_bottles 1)))
    (is (= six_bottle_en (trsn one_bottle n_bottles 6))))
  (testing "trsn with a user locale"
    (with-user-locale de (is (= one_bottle_en (trsn one_bottle n_bottles 1))))
    (with-user-locale de (is (= six_bottle_en (trsn one_bottle n_bottles 6))))))


(deftest test-empty-string-msgid-fallback-to-pot-no-header
  (testing "trsn with no user locale"
      (is (= "" (trsn "" "" 1)))
      (is (= "" (trsn "" "" 6)))
      (is (= "" (trsn "" "fred" 1)))
      (is (= "fred" (trsn "" "fred" 6)))
      ; msgid/msgstr not in po/bundle render correctly
      (is (= "fred" (trsn "fred" "" 1)))
      (is (= "" (trsn "fred" "" 6))))
  (testing "trsn with a user locale"
      (with-user-locale de
        (is (= "" (trsn "" "" 1)))
        (is (= "" (trsn "" "" 6)))
        (is (= "" (trsn "" "fred" 1)))
        (is (= "fred" (trsn "" "fred" 6)))
        ; msgid/msgstr not in po/bundle render correctly
        (is (= "fred" (trsn "fred" "" 1)))
        (is (= "" (trsn "fred" "" 6)))))
  (testing "trun can display an empty string"
    (with-user-locale de
      (is (= "" (trun "" "" 1)))
      (is (= "" (trun "" "" 6)))
      (is (= "" (trun "" "fred" 1)))
      (is (= "fred" (trun "" "fred" 6)))
      ; msgid/msgstr not in po/bundle render correctly
      (is (= "fred" (trun "fred" "" 1)))
      (is (= "" (trun "fred" "" 6)))))
  (testing "trs can display an empty string"
    (with-user-locale de
      (is (= "" (trs "")))
      (is (= " " (trs " ")))))
  (testing "tru can display an empty string"
    (with-user-locale de
      (is (= "" (tru "")))
      (is (= " " (tru " "))))))
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

(defn conflicting-locale-files
  []
  [(io/resource "test-locales/de-ru-es.clj")
   (io/resource "test-locales/merge-eo.clj")
   (io/resource "test-locales/conflicting-de.clj")])

(defn uberjar-locale-file
  []
  [(io/resource "test-locales/uberjar-en-de.clj")])

(defn merge-locale-files
  []
  [(io/resource "test-locales/de-ru-es.clj")
   (io/resource "test-locales/merge-eo.clj")])

(defn multi-pacakge-locale-file
  []
  [(io/resource "test-locales/multi-pacakge-es.clj")])

(defn overlapping-pacakge-locale-file
  []
  [(io/resource "test-locales/overlapping-packages.clj")
   (io/resource "test-locales/multi-pacakge-es.clj")])

(deftest test-infos
  (with-redefs
    [puppetlabs.i18n.core/info-files one-locale-file]
    (testing "info-map"
      (is (= ["example.i18n"] (keys (info-map'))))
      (is (= "example.i18n.Messages" (:bundle (get (info-map') "example.i18n")))))
    (testing "available-locales"
      (is (= #{"de" "ru" "es"} (available-locales)))))

  (with-redefs
    [puppetlabs.i18n.core/info-files overlapping-pacakge-locale-file]
    (testing "bundle-for-namespace ordering with overlapping packages and same length namespaces"
      (are [bundle i18n-ns]
          (= bundle (bundle-for-namespace (info-map') i18n-ns))
        "overlapped_package.i18n.Messages" "example"
        "multi_package.i18n.Messages" "example.i18n"
        "multi_package.i18n.Messages" "alternate.i18n"
        "overlapped_package.i18n.Messages" "alt3rnat3.i18n"
        "overlapped_package.i18n.Messages" "example.i18n.another_package")))

  (with-redefs
    [puppetlabs.i18n.core/info-files two-locale-files]
    (testing "info-map #2"
      (is (= #{"example.i18n" "other.i18n"} (into #{} (keys (info-map')))))
      (is (= "example.i18n.Messages" (:bundle (get (info-map') "example.i18n")))))
    (testing "available-locales #2"
      (is (= #{"it" "fr" "de" "ru" "es"} (available-locales))))
    (testing "bundle-for-namespace"
      (is (= "example.i18n.Messages" (bundle-for-namespace (info-map') "example.i18n")))
      (is (= "example.i18n.Messages"
             (bundle-for-namespace (info-map') "example.i18n.dog.cat")))
      (is (nil? (bundle-for-namespace (info-map') "example")))
      (is (= "other.i18n.Messages"
             (bundle-for-namespace (info-map') "other.i18n.abbott.costello")))))
  (with-redefs
    [puppetlabs.i18n.core/info-files conflicting-locale-files]
    (testing "conflicting locales"
      (is (thrown-with-msg? Exception #"Invalid locales info: .* are both for package .* but set different bundles"
                            (info-map')))))
  (with-redefs
    [puppetlabs.i18n.core/info-files uberjar-locale-file]
    (testing "info-map"
      (is (= #{"puppetlabs.i18n" "puppetlabs.foo"} (set (keys (info-map')))))
      (is (= "puppetlabs.foo.Messages" (:bundle (get (info-map') "puppetlabs.foo"))))
      (is (= "puppetlabs.i18n.Messages" (:bundle (get (info-map') "puppetlabs.i18n")))))
    (testing "available-locales"
      (is (= #{"de" "en" "fr"} (available-locales)))))
  (with-redefs
    [puppetlabs.i18n.core/info-files merge-locale-files]
    (testing "merged langauges"
      (is (= #{"de" "ru" "es" "eo"} (available-locales)))
      (is (= 1 (count (info-map'))))))
  (with-redefs
    [puppetlabs.i18n.core/info-files multi-pacakge-locale-file]
    (testing "multi package locales"
      (is (= "multi_package.i18n.Messages"
             (bundle-for-namespace (info-map') "example.i18n.abbott.costello")))
      (is (= "multi_package.i18n.Messages"
             (bundle-for-namespace (info-map') "alternate.i18n.abbott.costello"))))))

(deftest test-as-number
  (testing "convert number strings properly"
    (is (= 0.1 (as-number "0.1")))
    (is (= 1.0 (as-number "1.0")))
    (is (= 0.5 (as-number 0.5))))
  (testing "turns garbage into 0"
    (is (= 0 (as-number "xyz")))
    (is (= 0 (as-number true)))
    (is (= 0 (as-number nil)))))

(deftest test-parse-http-accept-header
  (let [p #(parse-http-accept-header %)]
    (testing "parses q-values properly"
      (is (= '(["de-DE" 1]) (p "de-DE")))
      (is (= '(["de-DE" 0.5]) (p "de-DE;q=0.5")))
      (is (= '() (p "de-DE;q=0.0")))
      (is (= '() (p "de-DE;q=garbage"))))
    (testing "sorts locales properly"
      (is (= '(["de-DE" 1] ["de" 1]) (p "de-DE, de")))
      (is (= '(["de-DE" 1.0] ["de" 1]) (p "de-DE;q=1, de")))
      (is (= '(["de-DE" 1] ["de" 0.9] ) (p "de;q=0.9, de-DE")))
      (is (= '(["de-DE" 0.8] ["de" 0.7]) (p "de;q=0.7, de-DE;q=0.8")))
      (is (= '(["de-DE" 0.8] ["de" 0.7]) (p "de-DE;q=0.8 , de;q=0.7, ")))
      (is (= '(["de" 1] ["en-gb" 0.8] ["en" 0.7])
             (p "de, en-gb;q=0.8, en;q=0.7"))))))

(deftest test-negotiate-locale
  (with-redefs
    [puppetlabs.i18n.core/system-locale #(string-as-locale "oc")]
    (let [check
          (fn [exp wanted]
            (is (= (string-as-locale exp)
                   (negotiate-locale wanted #{"de" "fr-FR" "en"}))))]
      (testing "works"
        (check "de" ["it" "de" "en"])
        (check "en" ["it" "en" "de"])
        (check "en" ["en_US" "en" "de"])
        (check "oc" ["da" "no"]))
      ;; The next two tests are here to document current behavior, not
      ;; because that behavior is nevessarily a great idea
      (testing "country variants for a locale we have are ignored"
        (check "oc" ["de-CH" "it"]))
      (testing "generic locale when we have country variant is ignored"
        (check "fr-FR" ["fr" "fr-FR"])))))

(deftest test-locale-negotiator
  (with-redefs
    [puppetlabs.i18n.core/available-locales (fn [] #{"de" "fr-FR" "en"})
     puppetlabs.i18n.core/system-locale #(string-as-locale "oc")]
    (let [neg        (locale-negotiator (fn [request] (user-locale)))
          mk-request (fn [accept]
                       {:headers {"accept-language" accept}})
          check      (fn [exp accept]
                       (is (= (string-as-locale exp)
                              (neg (mk-request accept)))))]
      (testing "works for valid headers"
        (check "de" "de")
        (check "de" "de_DE, de;q=0.9, en;q=0.8")
        (check "oc" "it, fr"))
      (testing "falls back to system-locale for empty/invalid headers"
        (doseq [x ["" nil "en;q=garbage" ",,," ",;," "xyz" "de-US"]]
          (check "oc" x))
        (is (= (system-locale) (neg {:headers {}})))
        (is (= (system-locale) (neg {}))))
      (testing "conveys the locale"
        (is (= (string-as-locale "de")
               ((locale-negotiator (fn [request] @(future (user-locale))))
                (mk-request "de"))))))))
