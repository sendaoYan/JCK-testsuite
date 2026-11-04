/*
 * @test
 * @summary org.omg.CORBA.VM_TRUNCATABLE conformance: constant holder interface.
 * @run main VM_TRUNCATABLETest
 */

public class VM_TRUNCATABLETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.VM_TRUNCATABLE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
