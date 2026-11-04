/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XNumber conformance: reflection.
 * @run main XNumberTest
 */

public class XNumberTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XNumber", false, XNumberTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XNumber"), "simple name XNumber");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.objects.XObject"), "superclass com.sun.org.apache.xpath.internal.objects.XObject");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getType"), "method getType");
        check(has(c, "getTypeString"), "method getTypeString");
        check(has(c, "num"), "method num");
        check(has(c, "bool"), "method bool");
        check(has(c, "str"), "method str");
        check(has(c, "object"), "method object");
        check(has(c, "equals"), "method equals");
        check(has(c, "isStableNumber"), "method isStableNumber");
        check(has(c, "callVisitors"), "method callVisitors");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
