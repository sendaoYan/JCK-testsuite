/*
 * @test
 * @summary org.omg.CORBA.portable.ValueBase conformance: interface shape.
 * @run main ValueBaseTest
 */

public class ValueBaseTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.ValueBase");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");

        // ---- deepened: _truncatable_ids surface and implementors ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("_truncatable_ids"))),
              "ValueBase declares only _truncatable_ids: " + names);
        check(c.getMethod("_truncatable_ids").getReturnType() == String[].class,
              "_truncatable_ids returns String[]");
        check(c.isAssignableFrom(org.omg.CORBA.DataInputStream.class), "DataInputStream extends ValueBase");
        check(c.isAssignableFrom(org.omg.CORBA.DataOutputStream.class), "DataOutputStream extends ValueBase");
        check(c.isAssignableFrom(org.omg.CORBA.portable.StreamableValue.class), "StreamableValue extends ValueBase");
        check(c.isAssignableFrom(org.omg.CORBA.portable.CustomValue.class), "CustomValue extends ValueBase");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
