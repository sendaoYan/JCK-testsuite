/*
 * @test
 * @summary org.omg.CORBA.VM_ABSTRACT conformance: constant holder interface.
 * @run main VM_ABSTRACTTest
 */

public class VM_ABSTRACTTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.VM_ABSTRACT");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");

        // ---- deepened: precise numeric value and field contract ----
        check(org.omg.CORBA.VM_ABSTRACT.value == (short) 2, "VM_ABSTRACT.value == (short) 2");
        java.lang.reflect.Field vf = org.omg.CORBA.VM_ABSTRACT.class.getField("value");
        check(java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "VM_ABSTRACT.value is final");
        check(vf.getType() == short.class, "VM_ABSTRACT.value field type");
        check(org.omg.CORBA.VM_ABSTRACT.class.getMethods().length == 0
              || java.util.Arrays.stream(org.omg.CORBA.VM_ABSTRACT.class.getMethods())
                   .allMatch(m -> m.getDeclaringClass() == java.lang.Object.class),
              "VM_ABSTRACT interface declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
