/*
 * @test
 * @summary org.omg.CORBA.portable.BoxedValueHelper conformance: interface shape.
 * @run main BoxedValueHelperTest
 */

public class BoxedValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.BoxedValueHelper");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact 3-method surface ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("get_id", "read_value", "write_value"))),
              "BoxedValueHelper declares get_id/read_value/write_value: " + names);
        check(c.getMethod("get_id").getReturnType() == String.class, "get_id returns String");
        check(c.getMethod("read_value", org.omg.CORBA.portable.InputStream.class).getReturnType()
                == java.io.Serializable.class, "read_value returns Serializable");
        check(c.getMethod("write_value", org.omg.CORBA.portable.OutputStream.class, java.io.Serializable.class) != null,
              "write_value takes (OutputStream, Serializable)");
        check(c.getMethods().length == 3, "exactly 3 methods on the interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
