/*
 * @test
 * @summary org.omg.CosNaming._BindingIteratorImplBase conformance: abstract class shape.
 * @run main _BindingIteratorImplBaseTest
 */

public class _BindingIteratorImplBaseTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming._BindingIteratorImplBase");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.DynamicImplementation.class.isAssignableFrom(c), "extends org.omg.CORBA.DynamicImplementation");
        check(org.omg.CosNaming.BindingIterator.class.isAssignableFrom(c), "implements org.omg.CosNaming.BindingIterator");
        // ---- deepened (batch C, jdk8u372 probed): abstract skeleton + concrete _ids ----
        check(java.lang.reflect.Modifier.isAbstract(org.omg.CosNaming._BindingIteratorImplBase.class.getModifiers()), "class is abstract");
        check(org.omg.CORBA.DynamicImplementation.class.isAssignableFrom(org.omg.CosNaming._BindingIteratorImplBase.class),
              "extends org.omg.CORBA.DynamicImplementation");
        check(org.omg.CosNaming.BindingIterator.class.isAssignableFrom(org.omg.CosNaming._BindingIteratorImplBase.class), "implements BindingIterator");
        org.omg.CosNaming._BindingIteratorImplBase impl = new org.omg.CosNaming._BindingIteratorImplBase() {
            public boolean next_one(org.omg.CosNaming.BindingHolder b) { return false; }
            public boolean next_n(int h, org.omg.CosNaming.BindingListHolder bl) { return false; }
            public void destroy() {}
        };
        check(impl != null, "concrete anonymous subclass constructs offline");
        String[] ids = impl._ids();
        check(ids.length == 1 && ids[0].equals("IDL:omg.org/CosNaming/BindingIterator:1.0"),
              "skeleton _ids() single exact repository id");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
