(defproject puppetlabs/i18n "0.9.2-SNAPSHOT"
  :description "Clojure i18n library"
  :url "http://github.com/puppetlabs/clj-i18n"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cpath-clj "0.1.2"]
                 [org.gnu.gettext/libintl "0.18.3"]]

  :profiles {:dev {:dependencies [[puppetlabs/kitchensink "2.1.0"
                                   :exclusions [org.clojure/clojure]]]
                   :plugins [[jonase/eastwood "0.8.1"
                              :exclusions [org.clojure/clojure]]]}}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]])
