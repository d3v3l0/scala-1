##!/bin/bash
# A wrapper script to report context of sloc.py -v using grep -C${N:-0}
scriptdir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
(( $# < 1 )) && echo "Usage: [N=3] $0 PERL_REGEX [sloc.py_FLAGS]" >&2 && exit 1
re="$1"; shift
"$scriptdir/sloc.py" -v "$@" 2>/dev/null | grep -v '^$' | grep -P "$re" | \
    sed -e 's:\([][]\|[*.]\):\\\1:g' | grep -C${N:-0} -f - -nR src/library/scala
