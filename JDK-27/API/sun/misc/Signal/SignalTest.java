/*
 * @test
 * @summary sun.misc.Signal conformance: signal.
 * @library /lib
 * @run main SignalTest
 */

public class SignalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** The first signal name this platform knows (Windows has no HUP). */
    static String knownSignal(String... candidates) {
        for (String n : candidates) {
            try {
                new sun.misc.Signal(n);
                return n;
            } catch (IllegalArgumentException notHere) {
                // try the next candidate
            }
        }
        throw new jtreg.SkippedException("none of " + java.util.Arrays.toString(candidates)
                                         + " is a signal this platform knows");
    }

    public static void main(String[] args) throws Exception {
        String name = knownSignal("HUP", "INT", "TERM");
        sun.misc.Signal s = new sun.misc.Signal(name);
        check(name.equals(s.getName()), "getName");
        check(s.getNumber() > 0, "getNumber");
        check(s.equals(new sun.misc.Signal(name)), "equals");
        check(s.hashCode() == new sun.misc.Signal(name).hashCode(), "hashCode");
        check(s.toString() != null, "toString");
        check(new sun.misc.Signal(name).getNumber() == s.getNumber(),
              "the same name maps to the same number");
        try {
            new sun.misc.Signal("NO_SUCH_SIGNAL_XYZ");
            check(false, "an unknown signal name must be rejected");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
