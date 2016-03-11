;; This has the same package as de-es-ru.clj which means we will merge the
;; locales from both files
;; This is mainly useful for tests where it lets us add a fake message
;; catalog that is not available during normal use
{
  :locales #{"eo"}
  :package "example.i18n"
}
