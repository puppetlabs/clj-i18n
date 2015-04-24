# The package name of this project
PROJECT=puppetlabs.i18n
LOCALES=$(basename $(notdir $(wildcard locales/*.po)))
PROJECT_DIR=$(subst .,/,$(PROJECT))
BUNDLE_FILES=$(patsubst %,resources/$(PROJECT_DIR)/Messages_%.class,$(LOCALES))
SRC_FILES=$(shell find src/ -name \*.clj)

i18n: update-pot msgfmt

# Update locales/messages.pot
update-pot: locales/messages.pot

locales/messages.pot: $(SRC_FILES)
	@find src/ -name \*.clj \
	    | xgettext --from-code=UTF-8 --language=lisp \
					-ktr:1 -ki18n/tr:1 \
					-o locales/messages.pot -f -

# Run msgfmt over all .po files to generate Java resource bundles
msgfmt: $(BUNDLE_FILES)
	@echo $(LOCALES) | tr ' ' '\n' > resources/locales.txt

resources/$(PROJECT_DIR)/Messages_%.class: locales/%.po
	msgfmt --java2 -d resources -r $(PROJECT).Messages -l $$(basename $< .po) $<

# Translators use this when they update translations; this copies any
# changes in the pot file into their language-specific po file
locales/%.po: locales/messages.pot
	msgmerge -U $@ $< && touch $@

# @todo lutter 2015-04-20: for projects that use libraries with their own
# translation, we need to combine all their translations into one big po
# file and then run msgfmt over that so that we only have to deal with one
# resource bundle
