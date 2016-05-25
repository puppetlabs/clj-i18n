# Example program

This little leiningen project is meant as a simple sandbox for
experimenting with the i18n library.

If you do not have the i18n library installed, or you want to use the lates
from the git checkout, simply run (in this directory)

    > mkdir checkouts
    > ln -s ../../.. checkouts/i18n

Let's see what a German user of this program would be told:

    > LANG=de_DE lein run

Before we can bring them any joy, we need to initialize the project for
using i18n:

    > lein i18n init

This will put a `Makefile` in place; run `make help` to learn more about
what that `Makefile` can do for you. You should run `lein i18n init` every
time the i18n library is updated to make sure you have the most recent
`Makefile`.

**Prior to running `make i18n`, you will need to have at least one string wrapped with a translation function, i.e. trs or tru.**

Until you are comfortable with how `.po` files are handled, it is safest to
only run `make i18n`, which is also run automtically every time `lein
compile` is run:

    > make i18n

There is now a file `locales/messages.pot` which is the 'template' file for
all translations. Translators will generate language-specific versions of
this file by running:

    > make locales/de.po

They will run this command (which is a thin wrapper aroung
`msgmerge`/`msginit`) every time the `messages.pot` has changed and
translations need to be updated. If you don't feel like translating the
messages for this program into German yourself, you can cheat and use the
file `dev-resources/de.po.premade` which I included for convenience:

    > cp dev-resources/de.po.premade locales/de.po

We are now at the point where a translator has given you updated
translations, and we want to try out our program in different languages:

    > make i18n
    > lein run
    > LANG=de_DE lein run

If this weren't an example program, but a 'real' project, you would check
the following files into git:

* `Makefile` and `dev-resources/Makefile.i18n`. You can freely edit
  `Makefile`, but `dev-resources/Makefile.i18n` will get clobbered every
  time you run `lein i18n init`, e.g., when you update to a newer version
  of the i18n library
* `locales/messages.pot` and `locales/*.po` as they contain the raw
  messages and the translations
