#!/usr/bin/env python3

import argparse
from collections import namedtuple
import errno
import os, os.path
import re
import subprocess
import sys

ROOT = ":/src/library/scala/"

# Hard Sanity checks

# Soft Sanity checks
RE_NONLOCAL_THROW = re.compile(r".*(" + (
    r'.*(?<!@local)(?<!def|val) \w+: \w+Throw\b' + "|" +
    r'\w+Throw\b =>') + r").*")
RE_NONCC_CANTHROW = re.compile(r'.*@local (?!cc)[^:]+: CanThrow.*')

RE_LOCAL_FN = re.compile(
    # cc, ct, mct, mcc are used for @local vals and params related to exceptions
    # use negative lookahead to eliminate @local followed by any of these
    r".*@local (?!" +
    r"(?:(?:(?:pr(?:otected|ivate)[^ ]* )?(?:implicit )?)(?:val|def) )?"
    r"m?c[ct]).*")

PAT_UNSAFE_SUFFIX = r".*// ?XXX\(leo\).*"
RE_UNSAFE_SUFFIX = re.compile(PAT_UNSAFE_SUFFIX)
RE_TRY_UNSAFE = re.compile(r".*ESC.TRY" + PAT_UNSAFE_SUFFIX)
RE_PAR_UNSAFE = re.compile(r".*(\.mct|mcc = new CanThrow)" + PAT_UNSAFE_SUFFIX)

RE_CANTHROW_PARAM = re.compile(r".*(\(@local cc: CanThrow|\bCanThrow ->).*")
RE_CAP_PARAM = re.compile(r".*[^(]@local \w+: (Maybe)?Can(not)?Throw.*")
RE_CAP_TYPE = re.compile(r".*type MaybeCanThrow.*")

def is_comment(line, pat=re.compile(r"^\s*(\*|\*\*|[/\\]\*|//)")):
    return pat.match(line)

def main(args=sys.argv):
    parser = argparse.ArgumentParser(
        prog=args[0],
        description="""Generates results about CHANGED code in case studies""")
    parser.add_argument(
        "-v", "--verbose", dest='v', action='count', default=0,
        help="increase output verbosity (can be repeated)")
    parser.add_argument(
        "-B", "--no-blocks", action='store_true',
        help="do not print lines containing ESC. only")
    parser.add_argument(
        "-w", "--canthrow-param", action='store_true',
        help="print lines matching regex: " + RE_CANTHROW_PARAM.pattern)
    parser.add_argument(
        "-C", "--no-cap-type", action='store_true',
        help="do not print lines defining type Cap (MaybeCanThrow)")
    parser.add_argument(
        "-c", "--cap-param", action='store_true',
        help="print lines matching regex: " + RE_CAP_PARAM.pattern)
    parser.add_argument(
        "-f", "--local-fn", action='store_true',
        help="print lines matching regex: " + RE_LOCAL_FN.pattern)
    parser.add_argument(
        "-y", "--try-unsafe", action='store_true',
        help="print lines matching regex: " + RE_TRY_UNSAFE.pattern)
    parser.add_argument(
        "-r", "--par-unsafe", action='store_true',
        help="print lines matching regex: " + RE_PAR_UNSAFE.pattern)
    parser.add_argument(
        "--check-nonlocal-throw", action='store_true',
        help="print lines matching regex: " + RE_NONLOCAL_THROW.pattern)
    parser.add_argument(
        "--check-noncc-canthrow", action='store_true',
        help="print lines matching regex: " + RE_NONCC_CANTHROW.pattern)

    # Result-sensitive flags (not just for verbose printing)
    parser.add_argument(
        "dirs", nargs='*',
        default=[""],
        help="directories relative to git root")
    parser.add_argument(
        "--base", default="1fbce4612c21a4d0c553ea489b4765494828c09f",
        help="base git commit")

    args = parser.parse_args(args[1:])
    args.dirs = [os.path.normpath(ROOT + "/" + s) for s in args.dirs]

    global ARGS; ARGS = args
    v_print(3, "ARGS:", args)

    block_freq = dict(("ESC." + b, 0) for b in ["TRY", "THROW", "NO"])
    canthrow_param_freq = 0
    cap_param_freq = 0
    cap_type_freq = 0
    local_fn_freq = 0
    cap_unsafe_freq = 0
    chunk_relevant = False
    def print_relevant(*args, **kwargs):
        nonlocal chunk_relevant
        chunk_relevant = True
        v_print(1, *args, **kwargs)

    proc = subprocess.Popen(
        ["git", "diff", "-U0", "-b", ARGS.base, "HEAD", "--"] + ARGS.dirs,
        stdout=subprocess.PIPE)
    for line in map(lambda bs: bs.decode("utf-8").rstrip(), proc.stdout):
        if line.startswith("@@"):
            if chunk_relevant: v_print(1)
            chunk_relevant = False
            continue                          # ignore diff location
        if not line.startswith("+"): continue # ignore removals
        line = line[1:]

        if is_comment(line): continue

        relevant = False
        def count_neg(matched, show):
            nonlocal relevant
            if matched: relevant |= show
            return int(matched)
        count_pos = count_neg   # TODO:

        for b in block_freq.keys():
            if b in line:
                block_freq[b] += count_neg(1, not ARGS.no_blocks)
        cap_type_freq += count_neg(
            bool(RE_CAP_TYPE.match(line)), not ARGS.no_cap_type)

        canthrow_param_freq += count_pos(
            bool(RE_CANTHROW_PARAM.match(line)), ARGS.canthrow_param)
        cap_param_freq += count_pos(
            bool(RE_CAP_PARAM.match(line)), ARGS.cap_param)
        local_fn_freq +=  count_pos(
            bool(RE_LOCAL_FN.match(line)), ARGS.local_fn)

        # Unsafe / unfinished
        cap_unsafe_freq += count_pos(
            bool(RE_TRY_UNSAFE.match(line)), ARGS.try_unsafe
        ) | count_pos(
            bool(RE_PAR_UNSAFE.match(line)), ARGS.par_unsafe
        )

        # Soft checks
        count_pos(
            bool(RE_NONLOCAL_THROW.match(line)), ARGS.check_nonlocal_throw)
        count_pos(
            bool(RE_NONCC_CANTHROW.match(line)), ARGS.check_noncc_canthrow)

        if relevant: print_relevant(line)

        # Hard checks
        if RE_UNSAFE_SUFFIX.match(line) and not any(
            cap in line for cap in ["CanThrow", "CannotThrow", "MaybeCanThrow"]
        ) and "ESC." not in line:
            raise Exception(line)

    def report(*args, **kwargs):
        kwargs.setdefault('file', sys.stderr)
        print(*args, **kwargs)
    report(block_freq)
    report("CanThrow param:", canthrow_param_freq)
    report("Cap param:", cap_param_freq)
    report("Cap type:", cap_type_freq)
    report("Cap unsafe:", cap_unsafe_freq)

    return 0

def v_print(min_verbosity, *args, **kwargs):
    if ARGS.v >= min_verbosity:
        print(*args, **kwargs)

if __name__ == '__main__': sys.exit(main())
