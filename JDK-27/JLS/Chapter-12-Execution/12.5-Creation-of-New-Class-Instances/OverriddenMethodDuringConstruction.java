/*
 * @test
 * @summary JLS 12.5: method dispatch during instance creation uses overridden
 *          methods even before the new object is completely initialized.
 * @run main OverriddenMethodDuringConstruction
 */

public class OverriddenMethodDuringConstruction {
    static final StringBuilder log = new StringBuilder();

    static class Base {
        Base() { m(); }
        void m() { log.append("Base.m "); }
    }

    static class Sub extends Base {
        int value = 5;
        void m() { log.append("Sub.m value=").append(value).append(' '); }
    }

    public static void main(String[] args) {
        new Sub();
        // Sub.m runs during Base's constructor; value still has default 0.
        if (!"Sub.m value=0 ".equals(log.toString()))
            throw new AssertionError("dispatch log: " + log);
        System.out.println("PASS");
    }
}
