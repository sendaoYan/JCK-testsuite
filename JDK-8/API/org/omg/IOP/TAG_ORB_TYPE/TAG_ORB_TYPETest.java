/*
 * @test
 * @summary org.omg.IOP.TAG_ORB_TYPE conformance: constant holder interface.
 * @run main TAG_ORB_TYPETest
 */

public class TAG_ORB_TYPETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_ORB_TYPE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
