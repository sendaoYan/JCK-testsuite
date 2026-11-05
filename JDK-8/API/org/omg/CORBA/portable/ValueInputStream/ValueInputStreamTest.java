/*
 * @test
 * @summary org.omg.CORBA.portable.ValueInputStream conformance: interface shape.
 * @run main ValueInputStreamTest
 */

public class ValueInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.ValueInputStream");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact 2-method surface ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("start_value", "end_value"))),
              "ValueInputStream declares start_value/end_value: " + names);
        check(c.getMethod("start_value").getParameterTypes().length == 0, "start_value takes no args");
        check(c.getMethod("end_value").getParameterTypes().length == 0, "end_value takes no args");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
