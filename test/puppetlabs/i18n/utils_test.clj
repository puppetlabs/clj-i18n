(ns puppetlabs.i18n.utils-test
  (:require [clojure.test :refer :all]
            [leiningen.i18n.utils :as utils]))

(deftest replace-first-in-multiline
  (testing "replace-first-in-multiline"
    (testing "only changes lines that begin with pattern"
      (is (= "asdf ABC-DEF stuff\nOther stuff"
             (utils/replace-first-in-multiline "asdf ABC-DEF stuff\nOther stuff"
                                               #"ABC-DEF"
                                               "New stuff"))))
    (testing "only changes first instance of pattern"
      (is (= "New stuff\nOther stuff\nABC-DEF"
             (utils/replace-first-in-multiline "ABC-DEF\nOther stuff\nABC-DEF"
                                               #"ABC-DEF"
                                               "New stuff"))))
    (testing "can pass regex to select to the EOL"
      (is (= "New stuff\nOther stuff"
              (utils/replace-first-in-multiline "ABC-DEF stuff\nOther stuff"
                                                #"ABC-DEF.*"
                                                "New stuff"))))
    (testing "can pass regex to select EOL on a single line"
      (is (= "New stuff"
             (utils/replace-first-in-multiline "ABC-DEF stuff"
                                               #"ABC-DEF.*"
                                               "New stuff"))))
    (testing "can pass regex to select EOL on last line"
      (is (= "\nNew stuff"
             (utils/replace-first-in-multiline "\nABC-DEF stuff"
                                               #"ABC-DEF.*"
                                               "New stuff"))))))
