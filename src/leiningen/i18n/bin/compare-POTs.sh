#!/bin/bash

# This script's only function is to determine whether there are any differences
# in the strings of the two gettext PO or POT files it's given as arguments. If
# there are no differences, a message to that effect is printed and the script
# exits zero; if there are, a different message is printed and the exit status
# is nonzero (specifically, 1),
#
# Differences are defined as only the addition or removal of entire strings
# and changes to the content of any one string; any differences in the
# translations available between the two files does not count.
#
# Most of this scritp is just checking error conditions; the real work happens
# in the last 10 lines.

set -eo pipefail

usage="usage: $0 old.pot new.pot"

old="$1"
new="$2"

set -u # there should be no more undefined variables

if [[ -z "$old" ]]; then
  echo "ERROR: no 'old.pot' file given" 1>&2
  echo "$usage" 1>&2
  exit 1
fi

if [[ -z "$new" ]]; then
  echo "ERROR: no 'new.pot' file given" 1>&2
  echo "$usage" 1>&2
  exit 1
fi

for f in "$old" "$new"; do
  if [[ ! -e "$f" ]]; then
    echo "ERROR: the file '$f' does not exist" 1>&2;
    exit 1
  fi

  if [[ ! -r "$f" ]]; then
    echo "ERROR: don't have permission to open $f" 1>&2;
    exit 1
  fi
done

set +e # msgcmp will return 1 if there are any differences
msgcmp_warnings=$(msgcmp --use-untranslated "$old" "$new" 2>&1)
msgcmp_status=$?
set -e

set -x

if [[ "$msgcmp_status" -ne 0 \
      || "$msgcmp_warnings" =~ "warning: this message is not used" ]]; then
  echo "Found differences between the POTs"
  exit 1
else
  echo "The POTs contain identical strings"
  exit 0
fi
