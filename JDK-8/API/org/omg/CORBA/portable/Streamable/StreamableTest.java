/*
 * @test
 * @summary org.omg.CORBA.portable.Streamable conformance: interface shape.
 * @run main StreamableTest
 */

public class StreamableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.Streamable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact 3-method surface ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("_read", "_write", "_type"));
        check(names.equals(expected), "Streamable declares exactly _read/_write/_type: " + names);
        check(c.getMethod("_read", org.omg.CORBA.portable.InputStream.class) != null, "_read takes InputStream");
        check(c.getMethod("_write", org.omg.CORBA.portable.OutputStream.class) != null, "_write takes OutputStream");
        check(c.getMethod("_type").getReturnType() == org.omg.CORBA.TypeCode.class, "_type returns TypeCode");
        // Any.insert_Streamable/extract_Streamable are the integration points
        check(java.util.Arrays.stream(org.omg.CORBA.Any.class.getMethods())
                .anyMatch(m -> m.getName().equals("insert_Streamable")), "Any.insert_Streamable exists");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
