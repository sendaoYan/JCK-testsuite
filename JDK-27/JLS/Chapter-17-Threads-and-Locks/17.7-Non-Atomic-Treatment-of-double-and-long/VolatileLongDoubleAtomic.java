/*
 * @test
 * @summary JLS 17.7: reads and writes of volatile long and double values are
 *          always atomic, so a reader never observes a value made of mixed
 *          32-bit halves.
 * @run main VolatileLongDoubleAtomic
 */

public class VolatileLongDoubleAtomic {
    static volatile long lv;
    static volatile double dv;
    static volatile boolean failed = false;
    static volatile boolean stop = false;

    // All four combinations of the two 32-bit halves are distinct, and none of
    // them equals the field's default 0.  With the old pair (0x00000000FFFFFFFF
    // / 0xFFFFFFFF00000000) one mixed value was 0x0 -- exactly the default -- so
    // a reader that ran before the writer's first store looked like a tear.
    static final long LONG_A = 0x1111111122222222L;
    static final long LONG_B = 0x3333333344444444L;
    // Two non-NaN doubles whose bit patterns differ in BOTH their high and low
    // 32 bits, so every mixed-halves read is distinguishable from A and B.
    static final double DOUBLE_A = Double.longBitsToDouble(0x3FF00000FFFFFFFFL);
    static final double DOUBLE_B = Double.longBitsToDouble(0x4000000000000000L);
    static final long DOUBLE_A_BITS = 0x3FF00000FFFFFFFFL;
    static final long DOUBLE_B_BITS = 0x4000000000000000L;
    static final int ITERS = 1_000_000;

    public static void main(String[] args) throws Exception {
        // Seed valid values before starting the reader: the reader flags any
        // value other than LONG_A/LONG_B (or DOUBLE_A/DOUBLE_B) as "torn", and
        // the field's initial value 0 is neither, so a reader that happens to
        // run before the writer's first store would falsely report a tear.
        lv = LONG_A;
        dv = DOUBLE_A;
        Thread writer = new Thread(() -> {
            for (int i = 0; i < ITERS && !failed; i++) {
                if ((i & 1) == 0) {
                    lv = LONG_A;
                    dv = DOUBLE_A;
                } else {
                    lv = LONG_B;
                    dv = DOUBLE_B;
                }
            }
            stop = true;
        });

        Thread reader = new Thread(() -> {
            while (!stop) {
                long v = lv;
                if (v != LONG_A && v != LONG_B) {
                    failed = true;
                    return;
                }
                long db = Double.doubleToRawLongBits(dv);
                if (db != DOUBLE_A_BITS && db != DOUBLE_B_BITS) {
                    failed = true;
                    return;
                }
            }
        });

        writer.start();
        reader.start();
        writer.join();
        reader.join(5000);

        if (reader.isAlive()) throw new AssertionError("reader did not terminate");
        if (failed) throw new AssertionError("observed a torn volatile long/double");

        System.out.println("PASS");
    }
}
