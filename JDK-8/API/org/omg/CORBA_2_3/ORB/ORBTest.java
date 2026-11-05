/*
 * @test
 * @summary org.omg.CORBA_2_3.ORB conformance: abstract class shape.
 * @run main ORBTest
 */

public class ORBTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA_2_3.ORB");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.ORB.class.isAssignableFrom(c), "extends org.omg.CORBA.ORB");
        // ---- deepened (jdk8u372-b07 probed): 2.3 ORB extension method face ----
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                  "register_value_factory", "unregister_value_factory",
                  "lookup_value_factory", "get_value_def", "set_delegate"))),
              "exactly the 5 Java-to-IDL 2.3 methods");
        java.lang.reflect.Method rvf = c.getDeclaredMethod("register_value_factory",
            String.class, org.omg.CORBA.portable.ValueFactory.class);
        check(rvf.getReturnType() == org.omg.CORBA.portable.ValueFactory.class,
              "register_value_factory(String, ValueFactory) returns ValueFactory");
        java.lang.reflect.Method gvd = c.getDeclaredMethod("get_value_def", String.class);
        check(gvd.getReturnType() == org.omg.CORBA.Object.class, "get_value_def(String) returns CORBA Object");
        check(gvd.getExceptionTypes().length == 1 && gvd.getExceptionTypes()[0] == org.omg.CORBA.BAD_PARAM.class,
              "get_value_def declares BAD_PARAM");
        check(c.getConstructors().length == 1, "single public no-arg constructor");
        // never instantiated: abstract superclass chain is org.omg.CORBA.ORB
        check(org.omg.CORBA.ORB.class.isAssignableFrom(c) && c != org.omg.CORBA.ORB.class,
              "proper 2.3 subclass of org.omg.CORBA.ORB");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
