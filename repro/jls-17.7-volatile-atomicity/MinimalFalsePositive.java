/*
 * Minimum reproduction of the VolatileLongDoubleAtomic "torn read" false positive.
 *
 *   1) legacy  (default): the archived test's constants, no seed
 *                         -> may report an unexpected value; it is always 0x0,
 *                            the field's default, read before the writer's first
 *                            store.  This is the historical "failure".
 *   2) fixed            : distinct constants + seed before starting threads
 *                         -> never reports an unexpected value.
 *
 * Build:  javac -d out MinimalFalsePositive.java
 * Run:    taskset -c 0 java -Xcomp -cp out MinimalFalsePositive legacy 5000
 *         taskset -c 0 java -Xcomp -cp out MinimalFalsePositive fixed  5000
 *
 * AArch64 (Temurin 8u502-b07 / 27.0.1.0.2), 2026-11-12:
 *   legacy: 11 of 40 runs reported an unexpected value, always 0x0000000000000000,
 *           most of them with firstStoreDone=false (i.e. before any store existed).
 *   fixed : 0 of 40 runs reported anything, same machine, same contention.
 *
 * Exit code: 0 = nothing unexpected observed, 2 = unexpected value observed.
 */
public class MinimalFalsePositive {
    static volatile long lv;
    static volatile boolean firstStoreDone;
    static volatile boolean stop;

    /* legacy pair: A.high|B.low == 0x0000000000000000 == the field's default,
       so "read the default before any store" is indistinguishable from a tear. */
    static final long LEGACY_A = 0x00000000FFFFFFFFL;
    static final long LEGACY_B = 0xFFFFFFFF00000000L;

    /* four halves-combinations pairwise distinct and none of them 0. */
    static final long FIXED_A = 0x1111111122222222L;
    static final long FIXED_B = 0x3333333344444444L;

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "legacy";
        long millis = args.length > 1 ? Long.parseLong(args[1]) : 5000L;
        boolean legacy = mode.equals("legacy");
        if (!legacy && !mode.equals("fixed"))
            throw new IllegalArgumentException("mode must be legacy or fixed");

        final long a = legacy ? LEGACY_A : FIXED_A;
        final long b = legacy ? LEGACY_B : FIXED_B;
        if (!legacy) lv = a;            /* seed: the field can never be 0 now */

        Thread writer = new Thread(() -> {
            long n = 0;
            while (!stop) {
                lv = ((n++ & 1) == 0) ? a : b;
                firstStoreDone = true;
            }
        });

        final long[] unexpected = new long[1];   /* value of the first anomaly */
        final boolean[] anomalyBeforeStore = new boolean[1];
        final long[] count = new long[1];
        Thread reader = new Thread(() -> {
            while (!stop) {
                long v = lv;
                if (v != a && v != b) {
                    if (count[0]++ == 0) {
                        unexpected[0] = v;
                        anomalyBeforeStore[0] = !firstStoreDone;
                    }
                }
            }
        });

        writer.setDaemon(true);
        reader.setDaemon(true);
        writer.start();
        reader.start();
        Thread.sleep(millis);
        stop = true;
        Thread.sleep(50);

        if (count[0] == 0) {
            System.out.println("mode=" + mode + " RESULT: OK, no unexpected value"
                    + " (reads completed without observing anything but A or B)");
            System.exit(0);
        }
        System.out.printf("mode=%s RESULT: UNEXPECTED value=0x%016X count=%d "
                        + "beforeFirstStore=%b%n",
                mode, unexpected[0], count[0], anomalyBeforeStore[0]);
        if (legacy && unexpected[0] == 0L)
            System.out.println("  -> 0x0 is the field's default; this is a read "
                    + "that happened before the writer's first store, NOT a tear.");
        System.exit(2);
    }
}
