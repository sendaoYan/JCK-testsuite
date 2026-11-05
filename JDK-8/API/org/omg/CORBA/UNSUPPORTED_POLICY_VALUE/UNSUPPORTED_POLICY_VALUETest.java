/*
 * @test
 * @summary org.omg.CORBA.UNSUPPORTED_POLICY_VALUE conformance: constant holder interface.
 * @run main UNSUPPORTED_POLICY_VALUETest
 */

public class UNSUPPORTED_POLICY_VALUETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.UNSUPPORTED_POLICY_VALUE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.UNSUPPORTED_POLICY_VALUE.value == (short) 4, "UNSUPPORTED_POLICY_VALUE.value == (short) 4");
        java.lang.reflect.Field vf = org.omg.CORBA.UNSUPPORTED_POLICY_VALUE.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "UNSUPPORTED_POLICY_VALUE.value is final");
        check(vf.getType() == short.class, "UNSUPPORTED_POLICY_VALUE.value field type");
        check(org.omg.CORBA.UNSUPPORTED_POLICY_VALUE.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.UNSUPPORTED_POLICY_VALUE.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "UNSUPPORTED_POLICY_VALUE interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
