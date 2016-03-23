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

PAT_UNSAFE_SUFFIX = r".*// ?(XXX|FIXME)\(leo\).*"
RE_UNSAFE_SUFFIX = re.compile(PAT_UNSAFE_SUFFIX)
RE_TRY_UNSAFE = re.compile(r".*ESC.TRY" + PAT_UNSAFE_SUFFIX)
RE_PAR_UNSAFE = re.compile(r".*(\.mct)" + PAT_UNSAFE_SUFFIX)

RE_CANTHROW_PARAM = re.compile(r".*(\(@local cc: CanThrow.*|\bCanThrow ->).*")
RE_CAP_PARAM = re.compile(r".*[^(]@local \w+: (Maybe)?Can(not)?Throw.*")

def is_comment(line, pat=re.compile(r"^[[:space:]]*(\*|\*\*|[/\\]\*)")):
    return pat.matches(line)

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
        "-c", "--cap-param", action='store_true',
        help="print lines matching regex: " + RE_CAP_PARAM.pattern)
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

        relevant = False
        for b in block_freq.keys():
            if b in line:
                block_freq[b] += 1
                relevant |= not ARGS.no_blocks

        relevant |= ARGS.canthrow_param and bool(RE_CANTHROW_PARAM.match(line))
        relevant |= ARGS.cap_param and bool(RE_CAP_PARAM.match(line))

        # Unsafe / unfinished
        relevant |= ARGS.try_unsafe and bool(RE_TRY_UNSAFE.match(line))
        relevant |= ARGS.par_unsafe and bool(RE_PAR_UNSAFE.match(line))

        # Soft checks
        relevant |= ARGS.check_nonlocal_throw and bool(
            RE_NONLOCAL_THROW.match(line))
        relevant |= ARGS.check_noncc_canthrow and bool(
            RE_NONCC_CANTHROW.match(l))

        if relevant: print_relevant(line)

        # Hard checks
        if RE_UNSAFE_SUFFIX.match(line) and not any(
            cap in line for cap in ["CanThrow", "CannotThrow", "MaybeCanThrow"]
        ) and "ESC." not in line:
            raise Exception(line)

    print(block_freq, file=sys.stderr)

    return 0

def v_print(min_verbosity, *args, **kwargs):
    if ARGS.v >= min_verbosity:
        print(*args, **kwargs)

if __name__ == '__main__': sys.exit(main())
