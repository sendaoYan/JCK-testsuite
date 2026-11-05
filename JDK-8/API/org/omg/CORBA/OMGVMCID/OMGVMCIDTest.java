/*
 * @test
 * @summary org.omg.CORBA.OMGVMCID conformance: constant holder interface.
 * @run main OMGVMCIDTest
 */

public class OMGVMCIDTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.OMGVMCID");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.OMGVMCID.value == 0x4f4d0000, "OMGVMCID.value == 0x4f4d0000");
        java.lang.reflect.Field vf = org.omg.CORBA.OMGVMCID.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "OMGVMCID.value is final");
        check(vf.getType() == int.class, "OMGVMCID.value field type");
        check(org.omg.CORBA.OMGVMCID.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.OMGVMCID.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "OMGVMCID interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
