/*
 * @test
 * @summary java.nio.file.WatchEvent.Modifier conformance: name() method.
 * @run main ModifierTest
 */

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testModifier() throws Exception {
        check(java.nio.file.WatchEvent.Modifier.class.isInterface(), "is interface");
        java.lang.reflect.Method m = java.nio.file.WatchEvent.Modifier.class.getMethod("name");
        check(m.getReturnType() == String.class, "name() returns String");
    }

    public static void main(String[] args) throws Exception {
        testModifier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
