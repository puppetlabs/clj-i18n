(ns puppetlabs.i18n.bin-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [clojure.java.shell :refer [sh]]
            [puppetlabs.kitchensink.core :as ks]))

(defn git-head-sha
  []
  (-> (sh "git" "rev-parse" "HEAD")
    :out
    str/trim))

(defn temp-file-from-resource
  "Given a prefix & suffix for a temp filename, and the classpath of a Java
  resource, create a new temp file with the resource's content that will be
  deleted when the JVM shuts down. Returns a File object for the temp file."
  [prefix suffix resource-path]
  (let [temp-file (ks/temp-file prefix suffix)
        resource (io/resource resource-path)]
    (io/copy (io/file resource) temp-file)
    temp-file))

(defn- path [f] (.getPath f))

(deftest add-gitref-test
  (testing "the src/leiningen/i18n/bin/add-gitref.sh script"
    (let [current-git-ref-line (re-pattern (str "\n\"X-Git-Ref: "
                                                (git-head-sha)
                                                "\\\\n\"\\s*\n"))]

      (testing "adds a git ref header when none is present"
        (let [po (temp-file-from-resource "i18n-add-gitref-test" ".po"
                                          "test-pos/no-git-ref.po")
              proc (sh "src/leiningen/i18n/bin/add-gitref.sh"
                       (path po))]
          (is (zero? (:exit proc)))
          (is (re-find current-git-ref-line (slurp po)))))

      (testing "replaces git ref header when one is already present"
        (let [fake-git-ref-line  #"\n\"X-Git-Ref: deadb33f\\n\""
              po (temp-file-from-resource "i18n-add-gitref-test" ".po"
                                          "test-pos/git-ref.po")
              _ (is (re-find fake-git-ref-line (slurp po)))
              proc (sh "src/leiningen/i18n/bin/add-gitref.sh"
                       (path po))]
          (is (zero? (:exit proc)))
          (let [post-script-contents (slurp po)]
            (is (re-find current-git-ref-line post-script-contents))
            (is (nil? (re-find fake-git-ref-line post-script-contents)))))))

    (testing "when given a PO file without a Project-Id-Version header"
      (let [po (temp-file-from-resource "i18n-add-gitref-test" ".po"
                                        "test-pos/no-project-id-version.po")
            {:keys [exit err]} (sh "src/leiningen/i18n/bin/add-gitref.sh"
                                   (path po))]
        (is (= 1 exit))
        (is (re-find #"X-Git-Ref\s+header\s+must\s+follow\s+the\s+Project-Id-Version" err))
        (is (re-find #"no\s+Project-Id-Version\s+header\s+was\s+found" err))
        (is (re-find (re-pattern (path po)) err))))))

(deftest remove-line-numbers-test
  (testing "the remove-line-numbers.sh script behaves as expected"
    (let [numbered-po (temp-file-from-resource "i18n-rm-line-nos-test" ".po"
                                               "test-pos/line-numbers.po")
          proc (sh "src/leiningen/i18n/bin/remove-line-numbers.sh"
                   (path numbered-po))]
      (is (zero? (:exit proc)))
      (let [post-script-contents (slurp numbered-po)]
        (is (= post-script-contents
               (-> (io/resource "test-pos/no-line-numbers.po")
                 io/file
                 slurp)))))))
