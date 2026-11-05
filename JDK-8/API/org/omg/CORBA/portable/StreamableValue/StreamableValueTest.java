/*
 * @test
 * @summary org.omg.CORBA.portable.StreamableValue conformance: interface shape.
 * @run main StreamableValueTest
 */

public class StreamableValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.StreamableValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.Streamable");
        check(org.omg.CORBA.portable.ValueBase.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ValueBase");

        // ---- deepened: marker combining Streamable + ValueBase ----
        check(c.getDeclaredMethods().length == 0, "adds no methods of its own");
        java.util.Set<String> inherited = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) inherited.add(m.getName());
        for (String mname : new String[]{"_read", "_write", "_type", "_truncatable_ids"})
            check(inherited.contains(mname), "inherits " + mname);
        check(inherited.size() == 4, "exactly the 4 inherited ops: " + inherited);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
