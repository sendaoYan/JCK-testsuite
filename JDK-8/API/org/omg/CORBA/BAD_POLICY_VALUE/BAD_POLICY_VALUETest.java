/*
 * @test
 * @summary org.omg.CORBA.BAD_POLICY_VALUE conformance: constant holder interface.
 * @run main BAD_POLICY_VALUETest
 */

public class BAD_POLICY_VALUETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.BAD_POLICY_VALUE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.BAD_POLICY_VALUE.value == (short) 3, "BAD_POLICY_VALUE.value == (short) 3");
        java.lang.reflect.Field vf = org.omg.CORBA.BAD_POLICY_VALUE.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "BAD_POLICY_VALUE.value is final");
        check(vf.getType() == short.class, "BAD_POLICY_VALUE.value field type");
        check(org.omg.CORBA.BAD_POLICY_VALUE.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.BAD_POLICY_VALUE.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "BAD_POLICY_VALUE interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
