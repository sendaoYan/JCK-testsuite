/*
 * @test
 * @summary org.omg.CORBA.portable.ValueOutputStream conformance: interface shape.
 * @run main ValueOutputStreamTest
 */

public class ValueOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.ValueOutputStream");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact 2-method surface ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("start_value", "end_value"))),
              "ValueOutputStream declares start_value/end_value: " + names);
        check(c.getMethod("start_value", String.class) != null, "start_value takes String repository id");
        check(c.getMethod("end_value").getParameterTypes().length == 0, "end_value takes no args");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
