/*
 * @test
 * @summary org.omg.IOP.TAG_MULTIPLE_COMPONENTS conformance: constant holder interface.
 * @run main TAG_MULTIPLE_COMPONENTSTest
 */

public class TAG_MULTIPLE_COMPONENTSTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_MULTIPLE_COMPONENTS");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
