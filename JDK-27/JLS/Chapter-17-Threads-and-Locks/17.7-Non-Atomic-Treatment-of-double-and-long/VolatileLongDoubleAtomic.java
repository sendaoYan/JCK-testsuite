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

    static final long LONG_A = 0x00000000FFFFFFFFL;
    static final long LONG_B = 0xFFFFFFFF00000000L;
    // Two non-NaN doubles whose bit patterns differ in their high 32 bits.
    static final double DOUBLE_A = Double.longBitsToDouble(0x00000000FFFFFFFFL);
    static final double DOUBLE_B = Double.longBitsToDouble(0x80000000FFFFFFFFL);
    static final long DOUBLE_A_BITS = 0x00000000FFFFFFFFL;
    static final long DOUBLE_B_BITS = 0x80000000FFFFFFFFL;
    static final int ITERS = 1_000_000;

    public static void main(String[] args) throws Exception {
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
