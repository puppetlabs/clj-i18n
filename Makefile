# The package name of this project
PROJECT=puppetlabs.i18n

# Update locales/messages.pot
# This will unconditionally update the pot file, even if no update is needed
update-pot:
	@find src/ -name \*.clj \
	    | xgettext --from-code=UTF-8 --language=lisp \
					-ktr:1 -ki18n/tr:1 \
					-o locales/messages.pot -f -

# Run msgfmt over all .po files to generate Java resource bundles
msgfmt: resources/Message_*.class
	@ls locales/*.po | sed -r -e 's@locales/([a-z_]+)\.po@\1@' > resources/locales.txt

resources/Message_%.class: locales/%.po
	msgfmt --java2 -d resources -r $(PROJECT).Messages -l $$(basename $< .po) $<

# Translators use this when they update translations; this copies any
# changes in the pot file into their language-specific po file
#locales/%.po: locales/messages.pot
#	touch $@
#	msgmerge -U $@ $<

# @todo lutter 2015-04-20: for projects that use libraries with their own
# translation, we need to combine all their translations into one big po
# file and then run msgfmt over that so that we only have to deal with one
# resource bundle
