/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.cmdline.Transform conformance: reflection.
 * @run main TransformTest
 */

public class TransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.cmdline.Transform", false, TransformTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Transform"), "simple name Transform");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getFileName"), "method getFileName");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "setParameters"), "method setParameters");
        check(has(c, "printUsage"), "method printUsage");
        check(has(c, "main"), "method main");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
