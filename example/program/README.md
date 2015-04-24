# sample-user

A sample user of the i18n library, demonstrating how to set things up.

## Usage

1. `lein i18n init`
2. `lein run`

Create a translation:

    make locales/de.po
    # edit locales/de.po
    make i18n
    LANG=de_DE lein run
