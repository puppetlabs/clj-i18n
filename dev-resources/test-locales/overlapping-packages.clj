;; This is a sample of the locales.clj file that the current version fo the
;; Makefile generates.
;; It is used by the tests in core_test.clj.
{
  :locales  #{"es"}
  ;; alt3rnate3.i18n has the same length as multi-package-es.clj, dexample.i18n.* overlaps with multi-package-es.clj
  :packages ["example.i18n.another_package" "example" "example.i18n.another_package" "alt3rnat3.i18n"]
  :bundle   "overlapped_package.i18n.Messages"
}
