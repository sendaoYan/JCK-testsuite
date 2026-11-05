/*
 * @test
 * @summary org.omg.CORBA.ContextList conformance: abstract class shape.
 * @run main ContextListTest
 */

public class ContextListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ContextList");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");

        // real instance from the offline ORB factory
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.ContextList cl = orb.create_context_list();
        check(cl != null, "create_context_list works offline");

        // ---- empty list ----
        check(cl.count() == 0, "fresh ContextList count 0");

        // ---- add / item / count ----
        cl.add("alpha");
        check(cl.count() == 1, "count after first add");
        cl.add("beta");
        cl.add("gamma");
        check(cl.count() == 3, "count after three adds");
        check("alpha".equals(cl.item(0)), "item(0) readback");
        check("beta".equals(cl.item(1)), "item(1) readback");
        check("gamma".equals(cl.item(2)), "item(2) readback");

        // ---- remove decrements and shifts the rest ----
        cl.remove(1);
        check(cl.count() == 2, "count after remove");
        check("alpha".equals(cl.item(0)), "item(0) unchanged after middle remove");
        check("gamma".equals(cl.item(1)), "old item(2) shifted to item(1)");
        cl.remove(0);
        check("gamma".equals(cl.item(0)), "tail shifted to head after head remove");
        cl.remove(0);
        check(cl.count() == 0, "list drains to zero");

        // ---- out-of-bounds -> org.omg.CORBA.Bounds ----
        try {
            cl.item(0);
            throw new AssertionError("item(0) on empty list should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "empty item(0) -> org.omg.CORBA.Bounds");
        }
        cl.add("x");
        try {
            cl.item(1);
            throw new AssertionError("item(1) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "item(count) -> org.omg.CORBA.Bounds");
        }
        try {
            cl.item(-1);
            throw new AssertionError("item(-1) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "item(-1) -> org.omg.CORBA.Bounds");
        }
        try {
            cl.remove(5);
            throw new AssertionError("remove(5) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "remove(5) -> org.omg.CORBA.Bounds");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
