/*
 * @test
 * @summary org.omg.CORBA.portable.CustomValue conformance: interface shape.
 * @run main CustomValueTest
 */

public class CustomValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.CustomValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");
        check(org.omg.CORBA.CustomMarshal.class.isAssignableFrom(c), "extends org.omg.CORBA.CustomMarshal");

        // ---- deepened: marker combining ValueBase + CustomMarshal ----
        check(c.getDeclaredMethods().length == 0, "adds no methods of its own");
        java.util.Set<String> inherited = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) inherited.add(m.getName());
        check(inherited.contains("marshal") && inherited.contains("unmarshal"),
              "inherits CustomMarshal marshal/unmarshal");
        check(inherited.contains("_truncatable_ids"), "inherits ValueBase _truncatable_ids");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
