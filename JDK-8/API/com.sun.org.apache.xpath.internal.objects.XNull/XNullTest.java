/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XNull conformance: reflection.
 * @run main XNullTest
 */

public class XNullTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XNull", false, XNullTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XNull"), "simple name XNull");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.objects.XNodeSet"), "superclass com.sun.org.apache.xpath.internal.objects.XNodeSet");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getType"), "method getType");
        check(has(c, "getTypeString"), "method getTypeString");
        check(has(c, "num"), "method num");
        check(has(c, "bool"), "method bool");
        check(has(c, "str"), "method str");
        check(has(c, "rtf"), "method rtf");
        check(has(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
