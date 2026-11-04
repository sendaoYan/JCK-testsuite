/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.JSR_W conformance: reflection.
 * @run main JSR_WTest
 */

public class JSR_WTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.JSR_W", false, JSR_WTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("JSR_W"), "simple name JSR_W");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.JsrInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.JsrInstruction");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "dump"), "method dump");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
