/*
 * @test
 * @summary com.sun.org.omg.SendingContext.CodeBaseOperations conformance: reflection.
 * @run main CodeBaseOperationsTest
 */

public class CodeBaseOperationsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.SendingContext.CodeBaseOperations", false, CodeBaseOperationsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CodeBaseOperations"), "simple name CodeBaseOperations");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.SendingContext.RunTimeOperations"), "interface org.omg.SendingContext.RunTimeOperations");
        check(has(c, "get_ir"), "method get_ir");
        check(has(c, "implementation"), "method implementation");
        check(has(c, "implementations"), "method implementations");
        check(has(c, "meta"), "method meta");
        check(has(c, "metas"), "method metas");
        check(has(c, "bases"), "method bases");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
