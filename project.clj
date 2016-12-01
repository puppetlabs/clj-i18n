(defproject puppetlabs/i18n "0.5.0-SNAPSHOT"
  :description "Clojure i18n library"
  :url "http://github.com/puppetlabs/clj-i18n"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cpath-clj "0.1.2"]
                 [org.gnu.gettext/libintl "0.18.3"]]

  :profiles {:dev {:dependencies [[puppetlabs/kitchensink "2.1.0"
                                   :exclusions [org.clojure/clojure]]]}}

  :main puppetlabs.i18n.main
  :aot [puppetlabs.i18n.main]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]])
