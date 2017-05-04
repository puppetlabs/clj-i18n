# i18n

A Clojure library and leiningen plugin to make i18n easier. Provides
convenience functions to access the JVM's localization facilities and
automates managing messages and resource bundles. The tooling for
translators uses [GNU gettext](http://www.gnu.org/software/gettext/), so
that translators can work with `.po` files which are widely used and for
which a huge amount of tooling exists.

The `main.clj` and `example/program` in this repo contain some simple code
that demonstrates how to use the translation functions. Before you can use
it, you need to run `make` to generate the necessary
`ResourceBundles`. After that, you can use `lein run` or `LANG=de_DE lein
run` to look at English and German output.

## Developer usage

Any Clojure code that needs to generate human-readable text must use the
functions `puppetlabs.i18n.core/trs` and `puppetlabs.i18n.core/tru` to do
so. Use `trs` for messages that should be formatted in the system's locale,
for example log messages, and `tru` for messages that will be shown to the
current user, for example an error that happened processing a web request.

When you require `puppetlabs.i18n.core` into your namespace, you *must*
call it either `trs`/`tru`/`trun`/`trsn` or
`i18n/trs`/`i18n/tru`/`i18n/trun`/`i18n/trsn` (these are the names that
`xgettext` will look for when it extracts strings) Typically, you
would have this in your namespace declaration

    (ns puppetlabs.myproject
      (:require [puppetlabs.i18n.core :as i18n :refer [trs trsn tru trun]]))

You use `trs`/`tru` very similar to how you use `format`, except that the
format string must be a valid
[`java.text.MessageFormat`](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html)
pattern. Note that these patterns offer support for localized formatting;
see the Javadocs for details. For example, you would write

    (println (trs "It takes {0} software engineers {1} hours to change a light bulb" 3 9))

`trsn`/`trun` are similar to `trs`/`tru` except that they support pluralization
of strings.  The first argument is the singular version of the string, the second
argument must be the plural form of the string.  The third argument is the count value
to determine the level of pluralization.  Any additional arguments will be used for additional formatting

    (println (trsn "We found one cute puppy" "We found {0} cute puppies" 5))

### How to find the Strings

Here is a crappy Ruby script that you can point at a Clojure source tree to find *most* of the strings that will need to be translated:

https://github.com/cprice404/stringtracker/blob/master/getstrings.rb

### Comments for translators

It is sometimes useful to tell the translator something about the message;
you can do that by preceding the message string in the`trs`/`tru`
invocation with a comment; in the above example you might want to say

    ;; This is really just a silly example message. It gets the following
    ;; arguments:
    ;; 0 : number of software engineers (an integer)
    ;; 1 : number of hours (also an integer)
    (println (trs "It takes {0} software engineers {1} hours to change a light bulb" 3 9))

The comment will be copied to `<project-name>.pot` together with the actual
message so that translators have some context on what they are working
on. Note that such comments must be immediately preceding the string that
is the message. When you write

    ;; No translator will see this
    (trs
      "A message on another line")

the comments do *not* get extracted into `<project-name>.pot`.

### Single quotes in messages

Single quotes have a special meaning in
[`java.text.MessageFormat`](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html)
patterns and need to be escaped with another single quote:

    ;; The following will produce "Hes going to the store"
    (trs "He's going to the store")

    ;; You may want to supply a comment for devs and
    ;; translators to make sure the quoting is preserved.
    ;; The following will produce "He's going to the store"
    (trs "He''s going to the store")

### Separating message extraction from translation

In some cases, messages need to be generated separately from when they're
translated; this is common in specialized `def` forms or when defining a
constant for reuse. In that case, use the `mark` macro to mark strings for
xgettext extraction, and the standard `trs`/`tru` at the translation site.

### Development tools

Extracting messages and building ResourceBundles requires the command line tools
from [GNU gettext](https://www.gnu.org/software/gettext/) which you will have to
install manually.

If you are using Homebrew on OSX, run `brew install gettext`. OSX provides the
BSD gettext library by default and because of that the Homebrew formula for
`gettext` is keg-only. keg-only formulas are not symlinked. This can be remedied
by running `brew link gettext --force`.

On Red Hat-based operating systems, including Fedora, install gettext via
`yum install gettext`

### Project setup

[![Clojars Project](https://img.shields.io/clojars/v/puppetlabs/i18n.svg)](https://clojars.org/puppetlabs/i18n)

1. In your `project.clj`, add `[puppetlabs/i18n "0.8.0"]` to your project's
   :plugins and :dependencies vectors (without the version number in
   :dependencies if your project uses clj-parent). Also add
   ```
   :uberjar-merge-with {"locales.clj"  [(comp read-string slurp)
                                        (fn [new prev]
                                          (if (map? prev) [new prev] (conj prev new)))
                                        #(spit %1 (pr-str %2))]}
   ```
   to merge in the translation locales.clj from upstream projects.
2. Run `lein i18n init`. This will
   * put a `Makefile.i18n` into `dev-resources/` in your project and include it
     into an existing toplevel `Makefile` resp. create a new one that does that.
     You should check these files into you source control system.
   * put scripts for comparing and updating PO & POT files in
     `dev-resources/i18n/bin`. (These scripts and the Makefile.i18n are updated to
     include your project name, so that the POT file will be named after your project.)
     These are used by [the clj-i18n CI job][ci-job]
     and can be ignored (they are added to the project's .gitignore file).
   * add hooks to the `compile` task that will rebuild the resource bundles
     (equivalent of running `make i18n`).
3. **If there are namespaces/packages in your project with names which do not
   start with a prefix derived from the project name:** you'll need to list all
   of your namespaces/package name prefixes in the `PACKAGES` variable in the
   top level `Makefile` before the inclusion of the `dev-resources/Makefile.i18n`
4. Add a job using [CI job configs' i18n-clj template][ci-job] to your project's
   CI pipelines. This job will automatically update the POT file when
   externalized strings are added or changed in the project.

[ci-job]: https://github.com/puppetlabs/ci-job-configs/blob/master/resources/job-templates/i18n-clj.yaml

This setup will ensure that compiling your project will also regenerate the Java
`ResourceBundle` classes that your code needs to do translations.

You can manually regenerate these files by running `make i18n`. Additional
information about the Make targets is available through running `make help`.

**Note: `make i18n` will fail if you don't have at least one string wrapped with a translation function, i.e. trs or tru.**

The i18n tools maintain files in three directories:
  * message catalogs in `locales/`
  * compiled translations in `resources/`
  * temporary files in the project root `/`, for example `/mp-e`

You should check the files in `locales/` into source control, but not the ones
 in `resources/` or the `mp-*` files. A sample `.gitignore` for a project might
 look something like:

 ```
 # Ignore these files for clj-i18n
 /resources/example/*.class
 /resources/locales.clj
 /mp-*
 ```

### Web service changes

If you are working on an HTTP service, you will also need to make sure that
we properly handle the locale that the user requests via the
`Accept-Language` header. The library contains the function
`locale-negotiator` that you should use as a Ring middleware. It stores the
negotiated locale in the `*locale*` binding - ultimately, that's the locale
that the `tru` macro will use.

### Testing and pseudo-localization

For testing, it is often useful to introduce translations that are
maintained separately from the generally used locales, and whose change is
controlled by developers rather than translators. The `i18n` library uses
the file `resources/locales.clj`, which is generated and maintained by the
`make` targets, to track for which locales translations are
available. Additional locales can be made available by putting one or more
`locales.clj` files on the class path whose `:package` entry is the same as
the one in `resources/locales.clj` but that mentions additional
`:locales`.

That makes it possible to introduce additional locales for testing by doing
the following:

1. Create a file `test/locales.clj` by copying `resources/locales.clj` and
   edit the copy by changing the `:locales` entry to the languages that
   should be used for testing
1. For each of the additional locales, create a message catalog. It will
   generally be easiest to base that message catalog on properties files
   rather than on `.po` files. If you added the `eo` locale, you need to
   create a file `test/<package path>/Messages_eo.properties`.  Note that
   pluralization is not currently supported in properties files.
1. Use those additional locales in your tests. The `test/` directory of
   this library has an example of that in the `test-tru` test in
   `core_test.clj`.

The macro `with-user-locale` can be used to change the locale under which a
certain test should run, for example, with

```clojure
(let [eo (string-as-locale "eo")]
  (with-user-locale eo
    (testing "user-locale is Esperanto"
      (is (= eo (user-locale))))))
```

## Translator usage
Translators for Puppet, don't use this workflow.  In the Puppet workflow POs are generated in our translation tool, from an up to date POT.  We don't, as developers, update or commit POs. So this may only be relevant should a developer want to test or generate a test language.

### Generate a test .po file
Prior to generating a po file, make sure the POT is up to date by running `make i18n`.
This will put new msgids from the app, into the POT.

To create a `.po` file for the language eo:

    make locales/eo.po

Note this will just take the contents of the current POT and write the PO from it.
Subsequent runs will not keep that file up to date.

### Update a test .po file
Prior to updating a po file, make sure the POT is up to date by running `make i18n`.
This will put new msgids from the app, into the POT.

To update the po:
    msgmerge -U locales/eo.po locales/<project-name>.pot

This uses the contents of the current POT to update msgids in the target po (eo.po).

## Release usage

When it comes time to make a release, or if you want to use your code in a
different locale before then, you need to generate Java `ResourceBundle`
classes that contain the localized messages. This is done by running `make
msgfmt` on your project.

# Hacking

The code is set up as an ordinary leiningen project, with the one exception
that you need to run `make` before running `lein test` or `lein run`, as
there are messages that need to be turned into a message bundle.

# Maintenance

Maintainers: David Lutterkort <lutter@puppetlabs.com> and Libby Molina <libby@puppetlabs.com>
Tickets: File bug tickets at https://tickets.puppetlabs.com/browse/INTL, and add the `clj` component to the ticket.
