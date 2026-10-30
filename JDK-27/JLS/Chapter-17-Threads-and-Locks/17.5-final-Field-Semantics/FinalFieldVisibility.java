/*
 * @test
 * @summary JLS 17.5: a thread that sees a safely published reference to a fully
 *          constructed object sees the correctly initialized values of its final
 *          fields.
 * @run main FinalFieldVisibility
 */

public class FinalFieldVisibility {
    static final class Holder {
        final int value;
        Holder(int v) { value = v; }
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
        holder = new Holder(42);
        reader.join(5000);

        if (reader.isAlive()) throw new AssertionError("reader did not observe holder");
        if (seen[0].value != 42)
            throw new AssertionError("final field value == " + seen[0].value + ", expected 42");

        System.out.println("PASS");
    }
}
