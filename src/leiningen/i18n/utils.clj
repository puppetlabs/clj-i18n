(ns leiningen.i18n.utils
  "Plugin for i18n tasks. Start by using i18n init"
  (:require [clojure.string :as cstr]))

(defn replace-first-in-multiline
  "Replace first instance of regex-match in the passed in string with the
   replacement. Uses RegExp's m flag (?m) so that ^/$ matches beginning of line."
  [s regex-match replacement]
  (cstr/replace-first s
                      (re-pattern (str "(?m)^" (str regex-match) "$"))
                      replacement))
