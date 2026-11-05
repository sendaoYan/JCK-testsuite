/*
 * @test
 * @summary org.omg.CosNaming._NamingContextImplBase conformance: abstract class shape.
 * @run main _NamingContextImplBaseTest
 */

public class _NamingContextImplBaseTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming._NamingContextImplBase");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.DynamicImplementation.class.isAssignableFrom(c), "extends org.omg.CORBA.DynamicImplementation");
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContext");
        // ---- deepened (batch C, jdk8u372 probed): abstract skeleton + concrete _ids ----
        check(java.lang.reflect.Modifier.isAbstract(org.omg.CosNaming._NamingContextImplBase.class.getModifiers()), "class is abstract");
        check(org.omg.CORBA.DynamicImplementation.class.isAssignableFrom(org.omg.CosNaming._NamingContextImplBase.class),
              "extends org.omg.CORBA.DynamicImplementation (stream-based skeleton)");
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(org.omg.CosNaming._NamingContextImplBase.class), "implements NamingContext");
        org.omg.CosNaming._NamingContextImplBase impl = new org.omg.CosNaming._NamingContextImplBase() {
            public void bind(org.omg.CosNaming.NameComponent[] n, org.omg.CORBA.Object o) {}
            public void bind_context(org.omg.CosNaming.NameComponent[] n, org.omg.CosNaming.NamingContext c) {}
            public void rebind(org.omg.CosNaming.NameComponent[] n, org.omg.CORBA.Object o) {}
            public void rebind_context(org.omg.CosNaming.NameComponent[] n, org.omg.CosNaming.NamingContext c) {}
            public org.omg.CORBA.Object resolve(org.omg.CosNaming.NameComponent[] n) { return null; }
            public void unbind(org.omg.CosNaming.NameComponent[] n) {}
            public void list(int h, org.omg.CosNaming.BindingListHolder bl, org.omg.CosNaming.BindingIteratorHolder bi) {}
            public org.omg.CosNaming.NamingContext new_context() { return null; }
            public org.omg.CosNaming.NamingContext bind_new_context(org.omg.CosNaming.NameComponent[] n) { return null; }
            public void destroy() {}
        };
        check(impl != null, "concrete anonymous subclass constructs offline");
        String[] ids = impl._ids();
        check(ids.length == 1 && ids[0].equals("IDL:omg.org/CosNaming/NamingContext:1.0"),
              "skeleton _ids() single exact repository id");
        check(impl instanceof org.omg.CORBA.Object, "skeleton instance is a CORBA Object");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
