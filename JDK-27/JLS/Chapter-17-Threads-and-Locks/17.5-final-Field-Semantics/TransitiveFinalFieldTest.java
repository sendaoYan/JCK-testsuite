/*
 * @test
 * @summary JLS 17.5.1: when a final reference field is frozen, the final fields
 *          of the object it references are transitively frozen too, so a thread
 *          that sees a safely published object also sees those final fields.
 * @run main TransitiveFinalFieldTest
 */

public class TransitiveFinalFieldTest {
    static final class Inner {
        final int x;
        final long y;
        Inner(int x, long y) { this.x = x; this.y = y; }
    }

    static final class Holder {
        final Inner inner;
        final String name;
        Holder(int x, long y) {
            inner = new Inner(x, y);
            name = "done";
        }
    }

    static volatile Holder holder;   // safe publication

    public static void main(String[] args) throws Exception {
        final Holder[] seen = new Holder[1];

        Thread reader = new Thread(() -> {
            Holder h;
            while ((h = holder) == null) {
                Thread.yield();
            }
            seen[0] = h;
        });

        reader.start();
        holder = new Holder(7, 0x123456789L);
        reader.join(5000);

        if (reader.isAlive()) throw new AssertionError("reader did not observe holder");
        if (seen[0].inner.x != 7) throw new AssertionError("inner.x = " + seen[0].inner.x);
        if (seen[0].inner.y != 0x123456789L) throw new AssertionError("inner.y");
        if (!"done".equals(seen[0].name)) throw new AssertionError("name");

        System.out.println("PASS");
    }
}
