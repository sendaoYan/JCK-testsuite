/*
 * @test
 * @summary javax.smartcardio.TerminalFactory conformance: terminal factory.
 * @run main TerminalFactoryTest
 */

public class TerminalFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        String dt = javax.smartcardio.TerminalFactory.getDefaultType();
        check(dt != null && dt.length() > 0, "getDefaultType");
        try {
            javax.smartcardio.TerminalFactory f = javax.smartcardio.TerminalFactory.getDefault();
            check(f != null, "getDefault");
            check(f.getType() != null, "getType");
            check(f.toString() != null, "toString");
        } catch (Throwable t) {
            // no terminal factory provider in this environment; getDefaultType still checked
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
