/*
 * @test
 * @summary org.omg.CORBA.CTX_RESTRICT_SCOPE conformance: constant holder interface.
 * @run main CTX_RESTRICT_SCOPETest
 */

public class CTX_RESTRICT_SCOPETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.CTX_RESTRICT_SCOPE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.CTX_RESTRICT_SCOPE.value == 15, "CTX_RESTRICT_SCOPE.value == 15");
        java.lang.reflect.Field vf = org.omg.CORBA.CTX_RESTRICT_SCOPE.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "CTX_RESTRICT_SCOPE.value is final");
        check(vf.getType() == int.class, "CTX_RESTRICT_SCOPE.value field type");
        check(org.omg.CORBA.CTX_RESTRICT_SCOPE.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.CTX_RESTRICT_SCOPE.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "CTX_RESTRICT_SCOPE interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
