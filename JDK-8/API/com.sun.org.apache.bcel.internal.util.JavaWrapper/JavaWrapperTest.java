/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.JavaWrapper conformance: reflection.
 * @run main JavaWrapperTest
 */

public class JavaWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.util.JavaWrapper", false, JavaWrapperTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("JavaWrapper"), "simple name JavaWrapper");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "runMain"), "method runMain");
        check(has(c, "_main"), "method _main");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
