/*
 * @test
 * @summary java.lang.Cloneable conformance: white-box structure + black-box behavior
 *          (Object.clone() throws CloneNotSupportedException without Cloneable, shallow copy
 *          with it).
 * @run main CloneableTest
 */

public class CloneableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Cloneable");
        check(c.isInterface(), "Cloneable is an interface");
        check(c.getMethods().length == 0 && c.getDeclaredMethods().length == 0,
              "Cloneable is a marker interface (no methods)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class NotCloneable {
        public Object clone() throws CloneNotSupportedException { return super.clone(); }
    }

    static class CloneableBox implements Cloneable {
        int v;
        CloneableBox(int v) { this.v = v; }
        public Object clone() throws CloneNotSupportedException { return super.clone(); }
    }

    static void testNotCloneable() throws Exception {
        NotCloneable n = new NotCloneable();
        try {
            n.clone();
            throw new AssertionError("clone() without Cloneable: expected CloneNotSupportedException");
        } catch (CloneNotSupportedException expected) {
            passed++;
        }
    }

    static void testCloneable() throws Exception {
        CloneableBox b = new CloneableBox(42);
        CloneableBox c = (CloneableBox) b.clone();
        check(c != b, "clone returns a distinct instance");
        check(c.getClass() == b.getClass(), "clone has the same class");
        check(c.v == b.v, "clone copies field values (shallow)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNotCloneable();
        testCloneable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
