/*
 * @test
 * @summary org.omg.CORBA.PRIVATE_MEMBER conformance: constant holder interface.
 * @run main PRIVATE_MEMBERTest
 */

public class PRIVATE_MEMBERTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.PRIVATE_MEMBER");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.PRIVATE_MEMBER.value == (short) 0, "PRIVATE_MEMBER.value == (short) 0");
        java.lang.reflect.Field vf = org.omg.CORBA.PRIVATE_MEMBER.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "PRIVATE_MEMBER.value is final");
        check(vf.getType() == short.class, "PRIVATE_MEMBER.value field type");
        check(org.omg.CORBA.PRIVATE_MEMBER.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.PRIVATE_MEMBER.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "PRIVATE_MEMBER interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
