/*
 * @test
 * @summary sun.misc.Signal conformance: signal.
 * @run main SignalTest
 */

public class SignalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        sun.misc.Signal s = new sun.misc.Signal("HUP");
        check(s != null, "ctor");
        check("HUP".equals(s.getName()), "getName");
        check(s.getNumber() > 0, "getNumber");
        check(s.equals(new sun.misc.Signal("HUP")), "equals");
        check(s.hashCode() == new sun.misc.Signal("HUP").hashCode(), "hashCode");
        check(s.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
