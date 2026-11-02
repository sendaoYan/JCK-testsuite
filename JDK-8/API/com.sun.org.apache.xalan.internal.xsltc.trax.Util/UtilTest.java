/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.Util conformance: reflection.
 * @run main UtilTest
 */

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.Util", false, UtilTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Util"), "simple name Util");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "baseName"), "method baseName");
        check(has(c, "noExtName"), "method noExtName");
        check(has(c, "toJavaName"), "method toJavaName");
        check(has(c, "getInputSource"), "method getInputSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
