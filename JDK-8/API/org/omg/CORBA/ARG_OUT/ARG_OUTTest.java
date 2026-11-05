/*
 * @test
 * @summary org.omg.CORBA.ARG_OUT conformance: constant holder interface.
 * @run main ARG_OUTTest
 */

public class ARG_OUTTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ARG_OUT");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.ARG_OUT.value == 2, "ARG_OUT.value == 2");
        java.lang.reflect.Field vf = org.omg.CORBA.ARG_OUT.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "ARG_OUT.value is final");
        check(vf.getType() == int.class, "ARG_OUT.value field type");
        check(org.omg.CORBA.ARG_OUT.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.ARG_OUT.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "ARG_OUT interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
