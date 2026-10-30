/*
 * @test
 * @summary java.lang.Cloneable API conformance: Object.clone() throws
 *          CloneNotSupportedException when the class does not implement Cloneable, and a
 *          shallow copy (distinct instance, equal fields, same class) when it does.
 * @run main CloneableTest
 */

public class CloneableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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
        testNotCloneable();
        testCloneable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
