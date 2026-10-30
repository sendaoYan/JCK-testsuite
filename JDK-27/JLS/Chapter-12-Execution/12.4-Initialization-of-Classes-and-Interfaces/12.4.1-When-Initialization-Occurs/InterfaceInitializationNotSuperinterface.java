/*
 * @test
 * @summary JLS 12.4.1 (Example 12.4.1-3): initializing an interface does not of
 *          itself initialize its superinterfaces, and a reference through a
 *          subinterface name initializes only the declaring interface.
 * @run main InterfaceInitializationNotSuperinterface
 */

public class InterfaceInitializationNotSuperinterface {
    static final StringBuilder log = new StringBuilder();

    interface I { int i = 1, ii = out("ii", 2); }
    interface J extends I { int j = out("j", 3), jj = out("jj", 4); }
    interface K extends J { int k = out("k", 5); }

    static int out(String s, int i) {
        log.append(s).append('=').append(i).append(' ');
        return i;
    }

    public static void main(String[] args) {
        if (J.i != 1) throw new AssertionError("J.i (constant variable)");
        if (K.j != 3) throw new AssertionError("K.j (declared in J)");
        // Only J's non-constant fields are initialized; I and K are not.
        if (!"j=3 jj=4 ".equals(log.toString()))
            throw new AssertionError("initialization log: " + log);
        System.out.println("PASS");
    }
}
