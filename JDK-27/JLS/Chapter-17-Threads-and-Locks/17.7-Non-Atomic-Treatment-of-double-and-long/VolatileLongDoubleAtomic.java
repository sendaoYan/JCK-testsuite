/*
 * @test
 * @summary JLS 17.7: writes and reads of volatile long and double values are
 *          always atomic.  A reader must never observe a value composed of the
 *          high 32 bits of one write and the low 32 bits of another.
 * @run main VolatileLongDoubleAtomic
 */

public class VolatileLongDoubleAtomic {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static volatile long lv;
    static volatile double dv;
    static volatile boolean failed;
    static volatile boolean stop;
    static volatile long observed;   // the mixed value a reader actually saw, for diagnostics

    // Several bit-pattern pairs per type.  For every pair the two mixed-halves
    // values (A.high|B.low and B.high|A.low) must differ from both A and B,
    // otherwise a torn read would collapse back onto a legal value and go
    // undetected (this was the original defect on the double side).
    //
    // The seed below (lv = A before the threads start) is what makes a pair such
    // as {0x00000000FFFFFFFF, 0xFFFFFFFF00000000} safe: one of its mixed values
    // is 0x0, which is also the field default, but the field is seeded to A so a
    // reader can never see the default.  A torn read of 0x0 is then unambiguously
    // 0x0 != A != B.
    static final long[][] LONG_PAIRS = {
        { 0x1111111122222222L, 0x3333333344444444L },   // halves pairwise distinct, none 0
        { 0x00000000FFFFFFFFL, 0xFFFFFFFF00000000L },   // mixed values are 0x0 / 0xFFFF..F
        { 0x8000000000000000L, 0x7FFFFFFFFFFFFFFFL },   // Long.MIN_VALUE / Long.MAX_VALUE
        { 0xAAAAAAAA55555555L, 0x55555555AAAAAAAAL },   // alternating bits
    };

    // Four finite-value double bit patterns whose high AND low 32 bits both
    // differ, so every mixed-halves read is distinguishable from A and B.
    static final long[][] DOUBLE_PAIRS = {
        { 0x3FF00000FFFFFFFFL, 0x4000000000000000L },   // ~1.0 vs 2.0
        { 0x3FF0000055556666L, 0x4000000077778888L },   // two arbitrary non-NaN values
        { 0x3FE0000000000000L, 0x3FF0000000000001L },   // 0.5 vs 1.0+ulp
        { 0x400921FB54442D18L, 0xC013333333333333L },   // PI vs -4.8
    };

    static final int ITERS = 500_000;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < LONG_PAIRS.length; i++) {
            long a = LONG_PAIRS[i][0], b = LONG_PAIRS[i][1];
            check(probeLong(a, b), "volatile long reads were atomic (pair " + i
                    + ", observed 0x" + Long.toHexString(observed) + ")");
        }
        for (int i = 0; i < DOUBLE_PAIRS.length; i++) {
            long a = DOUBLE_PAIRS[i][0], b = DOUBLE_PAIRS[i][1];
            check(probeDouble(a, b), "volatile double reads were atomic (pair " + i
                    + ", observed 0x" + Long.toHexString(observed) + ")");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Single writer alternating a/b, single reader rejecting anything but a/b.
    // Returns true iff the reader never saw a torn value.
    static boolean probeLong(long a, long b) throws Exception {
        lv = a;                     // seed: the field can never hold the default 0 now
        failed = false;
        stop = false;
        observed = 0;
        Thread writer = new Thread(() -> {
            for (int i = 0; i < ITERS && !failed; i++)
                lv = ((i & 1) == 0) ? a : b;
            stop = true;
        });
        Thread reader = new Thread(() -> {
            while (!stop) {
                long v = lv;
                if (v != a && v != b) { failed = true; observed = v; return; }
            }
        });
        writer.start();
        reader.start();
        writer.join();
        reader.join(5000);
        if (reader.isAlive()) throw new AssertionError("reader did not terminate");
        return !failed;
    }

    static boolean probeDouble(long aBits, long bBits) throws Exception {
        double a = Double.longBitsToDouble(aBits);
        double b = Double.longBitsToDouble(bBits);
        dv = a;
        failed = false;
        stop = false;
        observed = 0;
        Thread writer = new Thread(() -> {
            for (int i = 0; i < ITERS && !failed; i++)
                dv = ((i & 1) == 0) ? a : b;
            stop = true;
        });
        Thread reader = new Thread(() -> {
            while (!stop) {
                long db = Double.doubleToRawLongBits(dv);
                if (db != aBits && db != bBits) { failed = true; observed = db; return; }
            }
        });
        writer.start();
        reader.start();
        writer.join();
        reader.join(5000);
        if (reader.isAlive()) throw new AssertionError("reader did not terminate");
        return !failed;
    }
}
