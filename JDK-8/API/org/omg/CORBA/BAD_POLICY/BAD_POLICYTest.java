/*
 * @test
 * @summary org.omg.CORBA.BAD_POLICY conformance: constant holder interface.
 * @run main BAD_POLICYTest
 */

public class BAD_POLICYTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.BAD_POLICY");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.BAD_POLICY.value == (short) 0, "BAD_POLICY.value == (short) 0");
        java.lang.reflect.Field vf = org.omg.CORBA.BAD_POLICY.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "BAD_POLICY.value is final");
        check(vf.getType() == short.class, "BAD_POLICY.value field type");
        check(org.omg.CORBA.BAD_POLICY.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.BAD_POLICY.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "BAD_POLICY interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
