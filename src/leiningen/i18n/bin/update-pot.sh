#!/bin/bash

set -eo pipefail

repo_dir="$(realpath "$(dirname "$0")/../../..")"
pot_file="$repo_dir/locales/messages.pot"
compare_pots="$repo_dir/dev-resources/i18n/bin/compare-POTs.sh"

git_branch="$1";

set -u

if [[ -z "$git_branch" ]]; then
  echo "ERROR: no git branch argument was supplied, so the updated POT can't be pushed" 1>&2
  echo "usage: $0 remote_branch_name" 1>&2
  exit 1
fi

if [[ ! -e "$pot_file" ]]; then
  echo "ERROR: POT file '$pot_file' doesn't exist" 1>&2
  echo 'Have you run `make i18n update-pot`?'
  exit 1
fi

if [[ ! -r "$pot_file" ]]; then
  echo "ERROR: don't have permission to open '$pot_file'" 1>&2
  exit 1
fi

if [[ ! -e "$compare_pots" ]]; then
  echo "ERROR: the file '$compare_pots' does not exist" 1>&2
  echo 'Have you run 'lein i18n init'?' 1>&2
  exit 1
fi

if [[ ! -x "$compare_pots" ]]; then
  echo "ERROR: don't have permission to execute '$compare_pots'" 1>&2
  exit 1
fi

# move current POT out of the way
old_pot="$(mktemp "/tmp/i18n-POT-XXXXXXXX.po")"
mv "$pot_file" "$old_pot"

# regenerate the POT
make update-pot
new_pot="$pot_file"

echo ""
echo "Comparing checked-in POT file with fresh POT file"

set +e
# see if there are new strings in the new POT
if ! "$compare_pots" "$old_pot" "$new_pot"; then
  set -e

  echo ""
  echo "String changes found in fresh POT file; committing"

  ./dev-resources/i18n/bin/remove-line-numbers.sh "$new_pot"
  ./dev-resources/i18n/bin/add-gitref.sh "$new_pot"

  git add "$new_pot"
  git commit -m "(i18n) Update strings in messages.pot file"

  echo ""
  echo "Pushing updated POT file to GitHub"

  git push origin "HEAD:$git_branch"

  rm "$old_pot"
else
  echo ""
  echo "No string changes found; restoring old POT file"
  mv "$old_pot" "$pot_file"
fi


