/*
 * TornVolatileProbe — self-contained reproducer / diagnostic for the
 * "volatile long/double torn read on aarch64 -Xcomp" report (JLS 17.7).
 *
 * Usage:  java [JVM opts] -cp <dir> TornVolatileProbe <mode> [durationMs] [writerDelayMs]
 *
 * Modes:
 *   strict  (default)  seeded + rigorous constants.  A TEAR observed here is a
 *                      genuine JLS 17.7 violation.  Reading the field's default
 *                      value 0 is impossible (the field is seeded before the
 *                      threads start), and 0 is not one of the two possible
 *                      mixed-half values either.
 *   no-seed            rigorous constants but NO seed: a reader that runs before
 *                      the writer's first store sees the default 0.  Such reads
 *                      are counted separately as PRE-STORE (not as tears), which
 *                      demonstrates that the race window exists.
 *   selftest           negative control: seeds, then the writer deliberately
 *                      publishes a mixed-half value for 50 ms.  The probe MUST
 *                      report TEAR-DETECTED; this proves the detector is not
 *                      vacuous.
 *   legacy             byte-for-byte behaviour of the archived test: no seed and
 *                      LONG_A=0x00000000FFFFFFFF / LONG_B=0xFFFFFFFF00000000.
 *                      There, a torn mix of the two 32-bit halves is
 *                      0x0000000000000000 or 0xFFFFFFFFFFFFFFFF, and 0x0 is also
 *                      the field's default — so a pre-store read is reported as a
 *                      "tear".  This mode reproduces the historical failure and
 *                      prints the exact value that was observed.
 *
 * Exit codes: 0 = no tear, 2 = tear observed, 3 = bad usage.
 *
 * Compiles with javac 8 and later (no lambdas needed for the hot loop).
 */

public class TornVolatileProbe {

    static volatile long lv;
    static volatile double dv;
    static volatile boolean stop;
    static volatile boolean firstStoreDone;
    static volatile long storeCount;
    static volatile Report report;

    /* selected constants, set by main() */
    static long lA, lB, dA, dB;
    static boolean legacy;

    /* rigorous constants: A, B and both mixed-half values are all distinct,
     * non-zero, and none of them can be confused with the default 0. */
    static final long RIG_A  = 0x1111111122222222L;
    static final long RIG_B  = 0x3333333344444444L;
    static final long RIG_T1 = 0x1111111144444444L;  /* A.high + B.low  */
    static final long RIG_T2 = 0x3333333322222222L;  /* B.high + A.low  */

    static final long RIG_DA  = 0x3FF0000055556666L; /* ~1.0000000005, non-NaN */
    static final long RIG_DB  = 0x4000000077778888L; /* ~2.0000000009, non-NaN */
    static final long RIG_DT1 = 0x3FF0000077778888L;
    static final long RIG_DT2 = 0x4000000055556666L;

    /* legacy constants, exactly as in the archived test */
    static final long LEG_A  = 0x00000000FFFFFFFFL;
    static final long LEG_B  = 0xFFFFFFFF00000000L;
    static final long LEG_DA = 0x00000000FFFFFFFFL;
    static final long LEG_DB = 0x80000000FFFFFFFFL;

    static final class Report {
        long reads;
        long nA, nB, nTorn, nOther, nPreStore;
        int  firstTornSet;      /* 0 = none, 1 = recorded */
        long firstTorn;
        boolean firstTornBeforeFirstStore; /* writer had not completed any store yet */
        int  firstOtherSet;
        long firstOther;

        long dReads;
        long dA, dB, dTorn, dOther, dPreStore;
        int  firstDTornSet;
        long firstDTorn;
        boolean firstDTornBeforeFirstStore;
        int  firstDOtherSet;
        long firstDOther;
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "strict";
        long durationMs = args.length > 1 ? Long.parseLong(args[1]) : 3000L;
        long writerDelayMs = args.length > 2 ? Long.parseLong(args[2]) : 0L;

        boolean selftest = mode.equals("selftest");
        if (mode.equals("legacy")) {
            legacy = true;
            lA = LEG_A; lB = LEG_B; dA = LEG_DA; dB = LEG_DB;
        } else if (mode.equals("strict") || mode.equals("no-seed") || selftest) {
            legacy = false;
            lA = RIG_A; lB = RIG_B; dA = RIG_DA; dB = RIG_DB;
        } else {
            System.err.println("unknown mode: " + mode);
            System.err.println("usage: TornVolatileProbe {strict|no-seed|legacy|selftest} [durationMs] [writerDelayMs]");
            System.exit(3);
        }
        boolean seed = mode.equals("strict") || selftest;

        if (seed) {
            /* Seed before any thread exists.  Thread.start() gives the
             * happens-before edge, so both threads observe these values. */
            lv = lA;
            dv = Double.longBitsToDouble(dA);
            firstStoreDone = true;   /* the seed *is* a valid value */
        }

        Thread writer = new Thread(new Runnable() {
            public void run() {
                if (writerDelayMs > 0) {
                    try { Thread.sleep(writerDelayMs); } catch (InterruptedException ignore) { }
                }
                if (selftest) {
                    /* Negative control: deliberately publish a mixed-half value
                     * for 50 ms.  A working detector MUST report a tear. */
                    lv = RIG_T1;
                    dv = Double.longBitsToDouble(RIG_DT1);
                    try { Thread.sleep(50); } catch (InterruptedException ignore) { }
                }
                for (long i = 0; !stop; i++) {
                    if ((i & 1) == 0) {
                        lv = lA;
                        dv = Double.longBitsToDouble(dA);
                    } else {
                        lv = lB;
                        dv = Double.longBitsToDouble(dB);
                    }
                    storeCount = i + 1;
                    if (i == 0) firstStoreDone = true;
                }
            }
        });

        Thread reader = new Thread(new Runnable() {
            public void run() {
                Report r = new Report();
                while (!stop) {
                    long v = lv;
                    long d = Double.doubleToRawLongBits(dv);
                    r.reads++;
                    r.dReads++;
                    classifyLong(r, v);
                    classifyDouble(r, d);
                }
                report = r;
            }
        });

        long t0 = System.nanoTime();
        writer.start();
        reader.start();
        long deadline = t0 + durationMs * 1_000_000L;
        while (System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        stop = true;
        writer.join();
        reader.join();

        Report r = report;
        if (r == null) {
            System.err.println("reader did not report");
            System.exit(3);
        }

        System.out.println("mode=" + mode
                + " arch=" + System.getProperty("os.arch")
                + " jvm=" + System.getProperty("java.vm.name") + " " + System.getProperty("java.version")
                + " durationMs=" + durationMs
                + " writerDelayMs=" + writerDelayMs
                + " stores=" + storeCount);

        System.out.println("  LONG   reads=" + r.reads
                + " A=" + r.nA + " B=" + r.nB
                + " TORN=" + r.nTorn
                + " PRE_STORE=" + r.nPreStore
                + " OTHER=" + r.nOther
                + (r.firstTornSet != 0
                   ? " firstTorn=0x" + hex(r.firstTorn) + " beforeFirstStore=" + r.firstTornBeforeFirstStore
                   : "")
                + (r.firstOtherSet != 0 ? " firstOther=0x" + hex(r.firstOther) : ""));

        System.out.println("  DOUBLE reads=" + r.dReads
                + " A=" + r.dA + " B=" + r.dB
                + " TORN=" + r.dTorn
                + " PRE_STORE=" + r.dPreStore
                + " OTHER=" + r.dOther
                + (r.firstDTornSet != 0
                   ? " firstTorn=0x" + hex(r.firstDTorn) + " beforeFirstStore=" + r.firstDTornBeforeFirstStore
                   : "")
                + (r.firstDOtherSet != 0 ? " firstOther=0x" + hex(r.firstDOther) : ""));

        long tears = r.nTorn + r.dTorn;

        /* In legacy mode the observation is ambiguous: 0x0 is both a possible
         * mixed-half value and the field's default.  Say so explicitly. */
        if (legacy && tears > 0) {
            boolean zeroTear = (r.firstTornSet != 0 && r.firstTorn == 0L)
                    || (r.firstDTornSet != 0 && r.firstDTorn == 0L);
            boolean beforeFirstStore = (r.firstTornSet != 0 && r.firstTornBeforeFirstStore)
                    || (r.firstDTornSet != 0 && r.firstDTornBeforeFirstStore);
            if (beforeFirstStore) {
                System.out.println("RESULT: LEGACY-FAIL observed an unexpected value *before the writer "
                        + "completed any store* — at that point the field can only hold its default "
                        + "(0x0000000000000000), so this is the pre-store artifact, NOT proof of tearing");
            } else if (zeroTear) {
                System.out.println("RESULT: LEGACY-FAIL observed value 0x0000000000000000 == default; "
                        + "this is the pre-store artifact, NOT proof of tearing");
            } else {
                System.out.println("RESULT: LEGACY-FAIL observed a non-zero unexpected value");
            }
            System.exit(2);
        }

        if (tears > 0) {
            System.out.println("RESULT: TEAR-DETECTED — genuine JLS 17.7 violation");
            System.exit(2);
        }

        System.out.println("RESULT: OK — no tear observed"
                + (r.nPreStore + r.dPreStore > 0
                   ? " (but " + (r.nPreStore + r.dPreStore) + " PRE-STORE default reads)"
                   : ""));
        System.exit(0);
    }

    static void classifyLong(Report r, long v) {
        if (v == lA) { r.nA++; return; }
        if (v == lB) { r.nB++; return; }
        if (legacy) {
            recordTearLong(r, v, !firstStoreDone);
            return;
        }
        if (v == RIG_T1 || v == RIG_T2) {
            recordTearLong(r, v, false);
            return;
        }
        if (v == 0L && !firstStoreDone) {
            r.nPreStore++;
            return;
        }
        if (r.firstOtherSet == 0) { r.firstOtherSet = 1; r.firstOther = v; }
        r.nOther++;
    }

    static void classifyDouble(Report r, long d) {
        if (d == dA) { r.dA++; return; }
        if (d == dB) { r.dB++; return; }
        if (legacy) {
            recordTearDouble(r, d, !firstStoreDone);
            return;
        }
        if (d == RIG_DT1 || d == RIG_DT2) {
            recordTearDouble(r, d, false);
            return;
        }
        if (d == 0L && !firstStoreDone) {
            r.dPreStore++;
            return;
        }
        if (r.firstDOtherSet == 0) { r.firstDOtherSet = 1; r.firstDOther = d; }
        r.dOther++;
    }

    static void recordTearLong(Report r, long v, boolean beforeFirstStore) {
        if (r.firstTornSet == 0) {
            r.firstTornSet = 1;
            r.firstTorn = v;
            r.firstTornBeforeFirstStore = beforeFirstStore;
        }
        r.nTorn++;
    }

    static void recordTearDouble(Report r, long v, boolean beforeFirstStore) {
        if (r.firstDTornSet == 0) {
            r.firstDTornSet = 1;
            r.firstDTorn = v;
            r.firstDTornBeforeFirstStore = beforeFirstStore;
        }
        r.dTorn++;
    }

    static String hex(long v) {
        String s = Long.toHexString(v).toUpperCase();
        while (s.length() < 16) s = "0" + s;
        return s;
    }
}
