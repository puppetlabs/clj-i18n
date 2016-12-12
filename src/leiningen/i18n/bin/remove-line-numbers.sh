#!/bin/bash

# This script's only function is to strip the line numbers from the location
# comments in a PO or POT file. It does this using a sed one-liner at the
# bottom. The preceding lines are largely about checking for error conditions.

set -eo pipefail

gettext_file="$1"

set -u # there should be no more undefined variables

if [[ -z "$gettext_file" ]]; then
  echo "ERROR: no PO or POT file given" 1>&2
  echo "usage: $0 <PO-or-POT-file>" 1>&2
  exit 1
fi

if [[ ! -e "$gettext_file" ]]; then
  echo "ERROR: the file '$gettext_file' does not exist" 1>&2
  exit 1
fi

if [[ ! -r "$gettext_file" ]]; then
  echo "ERROR: don't have permission to open '$gettext_file'" 1>&2
  exit 1
fi

sed -i.bak -e '/^#:.*[0-9]/ {' -e 's/:[0-9][0-9]*//g' -e '}' "$gettext_file"
rm "$gettext_file.bak"
