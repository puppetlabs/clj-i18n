# i18n

A Clojure library designed to make i18n easier. Provides convenience
functions to access the JVM's localization facilities and some guidance on
how to use the GNU `gettext` tools.

The `main.clj` in this repo contains some simple code that demonstrates how
to use the `tr` function. Before you can use it, you need to run `make
msgfmt` to generate the necessary `ResourceBundles`.

Then you can do `lein run` or `LANG=de_DE lein run` to look at English and
German output.

## Developer usage

Any Clojure code that needs to generate human-readable text must use the
function `puppetlabs.i18n.core/tr` to do so. When you require it into your
namespace, you *must* call it either `tr` or `i18n/tr` (these are the names
that `xgettext` will look for when it extracts strings)

You use `tr` very similar to how you use `format`, except that the format
string must be a valid
[`java.text.MessageFormat`](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html)
pattern. For example, you would write

    (println (tr "It takes {0} women {1} months to have a child" 3 9))

### Project setup

1. Make `puppetlabs.i18n.core` a dependency and a plugin __details on how__
1. Run `lein i18n init`. This will
   * put a `Makefile.i18n` into `dev-resources/` in your project and
   include it into an existing toplevel `Makefile` resp. create a new one
   that does that
   * add hooks to the `compile`, `jar` and `uberjar` that will refresh i18n
     data (equivalent of running `make i18n`)
1. Before checking in code changes, run `make i18n` (__do we need to
     require that ?__)

## Translator usage

When a translator gets ready to translate messages, they need to update the
corresponding `.po` file. For example, to update German translations,
they'd run

    msgmerge -U locales/de.po locales/messages.pot

and then edit `locales/de.po`

## Release usage

When it comes time to make a release, or if you want to use your code in a
different locale before then, you need to generate Java `ResourceBundle`
classes that contain the localized messages. This is done by running `make
msgfmt` on your project.

## Project layout

The (not quite realized) vision is that projects that want to do i18n with
this library need to have a `locales/` directory for the pot and po
files. They should all be checked into source control.

During a build, `msgfmt` will put class files into
`resources/PACKAGE/Message*.class` and create a file
`resources/locales.txt`; these files are automatically slurped into the
uberjar and shouldn't require any additional intervention.

## Todo

* allow setting a thread-specific locale, and use that for l10n
* propagating locale to background threads
* make running xgettext and msgfmt a leiningen plugin
* figure out the right project-specific namespace in which to look for the
  Messages `ResourceBundle` (not just `puppetlabs.i18n`)
* figure out how to combine the message catalogs of multiple
  libraries/projects into one at release time (msgcat)
* add Ring middleware to do language negotiation based on the
  Accept-Language header and set the per-thread locale accordingly
* should `ResourceBundle` class files be checked into git ?
