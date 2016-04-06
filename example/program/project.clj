(defproject puppetlabs/i18n-example-program "0.1.0"
  :description "A sample use of the i18n library"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [puppetlabs/i18n "0.2.3-SNAPSHOT"]]
  :plugins      [[puppetlabs/i18n "0.2.3-SNAPSHOT"]]
  :main puppetlabs.i18n-example-program.main
  :aot [puppetlabs.i18n-example-program.main])
