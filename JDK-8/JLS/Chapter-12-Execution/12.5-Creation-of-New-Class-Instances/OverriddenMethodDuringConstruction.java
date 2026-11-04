/*
 * @test
 * @summary JLS 12.5 (Java SE 8): method dispatch during instance creation uses
 *          overridden methods even before the new object is completely
 *          initialized.
 * @run main OverriddenMethodDuringConstruction
 */

public class OverriddenMethodDuringConstruction {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StringBuilder log = new StringBuilder();

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
        check("Sub.m value=0 ".equals(log.toString()), "dispatch log: " + log);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
