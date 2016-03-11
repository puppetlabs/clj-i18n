;; This file can go anywhere on the class path
;; It is used to add additional locales for testing
;; to the ones that are available for 'normal' use
{
  ;; we use Esperanto for testing
  :locales #{"eo"}
  ;; this should be the same as in resources/locales.clj
  :package "puppetlabs.i18n"
}
