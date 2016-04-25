(ns leiningen.i18n
  "Plugin for i18n tasks. Start by using i18n init"
  (:require [leiningen.core.main :as l]
            [puppetlabs.i18n.make :refer [i18n-make]]))

(defn help
  []

" The i18n tooling expects that you have GNU make and the gettext tools
  installed.

  Invoking this command without arguments will generate a Makefile with
  the appropriate gettext/msgfmt calls and generates the relevant message
  files, putting them in the lein :compile-path.
")

(defn abort
  [& rest]
  (apply l/abort (concat '("Error:") rest (list "\n\n" (help)))))

(defn i18n
  [project command]

  (when-not (:root project)
    (abort "The i18n plugin can only be run inside a project"))
  
  (l/debug "Running 'make i18n'")
  (i18n-make project))
