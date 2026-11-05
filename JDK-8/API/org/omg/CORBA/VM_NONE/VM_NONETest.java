/*
 * @test
 * @summary org.omg.CORBA.VM_NONE conformance: constant holder interface.
 * @run main VM_NONETest
 */

public class VM_NONETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.VM_NONE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.VM_NONE.value == (short) 0, "VM_NONE.value == (short) 0");
        java.lang.reflect.Field vf = org.omg.CORBA.VM_NONE.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "VM_NONE.value is final");
        check(vf.getType() == short.class, "VM_NONE.value field type");
        check(org.omg.CORBA.VM_NONE.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.VM_NONE.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "VM_NONE interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
