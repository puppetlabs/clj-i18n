;; This is a sample of the locales.clj file that the current version fo the
;; Makefile generates.
;; It is used by the tests in core_test.clj.
{
  :locales  #{"es"}
  :packages ["example.i18n.another_package" "example" "example.i18n"]
  :bundle   "multi_package.i18n.Messages"
}
