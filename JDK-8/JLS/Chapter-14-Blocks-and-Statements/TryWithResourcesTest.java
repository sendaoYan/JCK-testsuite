/*
 * @test
 * @summary JLS 14.20.3 (Java SE 8): the try-with-resources statement closes each
 *          resource in reverse order of declaration, and close() runs even if the
 *          body throws.
 * @run main TryWithResourcesTest
 */

public class TryWithResourcesTest {
    static class Res implements AutoCloseable {
        final String name;
        final StringBuilder log;
        Res(String n, StringBuilder l) { name = n; log = l; }
        public void close() { log.append(name); }
    }

    public static void main(String[] args) {
        StringBuilder log = new StringBuilder();
        try (Res a = new Res("a", log); Res b = new Res("b", log)) {
            // body does nothing
        }
        if (!log.toString().equals("ba")) throw new AssertionError("reverse close order: " + log);

        StringBuilder log2 = new StringBuilder();
        boolean caught = false;
        try (Res r = new Res("r", log2)) {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            caught = true;
        }
        if (!caught || !log2.toString().equals("r")) throw new AssertionError("close on exception");

        System.out.println("PASS");
    }
}
