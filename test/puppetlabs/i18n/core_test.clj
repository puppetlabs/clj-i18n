(ns puppetlabs.i18n.core-test
  (:require [clojure.test :refer :all]
            [puppetlabs.i18n.core :refer :all]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]))

;; Set the JVM's default locale so we run in a known environment
(. java.util.Locale setDefault (string-as-locale "en-US"))

(def de (string-as-locale "de-DE"))
(def eo (string-as-locale "eo"))
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
      (is (= welcome_eo (tru welcome_en))))))

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
      (is (= welcome_en (trs welcome_en))))))


(deftest test-trsn
  (testing "trun with no user locale"
    (is (= one_bottle_en (trsn one_bottle n_bottles 1)))
    (is (= six_bottle_en (trsn one_bottle n_bottles 6))))
  (testing "trun with a user locale"
    (with-user-locale de (is (= one_bottle_en (trsn one_bottle n_bottles 1))))
    (with-user-locale de (is (= six_bottle_en (trsn one_bottle n_bottles 6))))))

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

(defn merge-locale-files
  []
  [(io/resource "test-locales/de-ru-es.clj")
   (io/resource "test-locales/merge-eo.clj")])

(defn multi-pacakge-locale-file
  []
  [(io/resource "test-locales/multi-pacakge-es.clj")])

(defn overlapping-pacakge-locale-file
  []
  [(io/resource "test-locales/overlapping-packages.clj")])

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
    (testing "info-map with overlapping packages"
      (is (= ["example.i18n.another_package" "example.i18n" "example"] (keys (info-map'))))))

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
    [puppetlabs.i18n.core/message-locale #(string-as-locale "oc")]
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
     puppetlabs.i18n.core/message-locale #(string-as-locale "oc")]
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
      (testing "falls back to message-locale for empty/invalid headers"
        (map #(check "oc" %) ["" nil "en;q=garbage" ",,," ",;," "xyz" "de-US"])
        (is (= (message-locale) (neg {:headers {}})))
        (is (= (message-locale) (neg {}))))
      (testing "conveys the locale"
        (is (= (string-as-locale "de")
               ((locale-negotiator (fn [request] @(future (user-locale))))
                (mk-request "de"))))))))
