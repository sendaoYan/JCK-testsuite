/*
 * @test
 * @summary JLS 12.4.1 (Example 12.4.1-2): a reference to a static field causes
 *          initialization only of the class that actually declares it.
 * @run main OnlyDeclaringClassInitialized
 */

public class OnlyDeclaringClassInitialized {
    static final StringBuilder log = new StringBuilder();

    static class Super { static int taxi = 1729; }
    static class Sub extends Super { static { log.append("Sub "); } }

    public static void main(String[] args) {
        int v = Sub.taxi;
        if (v != 1729) throw new AssertionError("taxi value");
        if (!"".equals(log.toString()))
            throw new AssertionError("Sub should not be initialized: " + log);
        System.out.println("PASS");
    }
}
