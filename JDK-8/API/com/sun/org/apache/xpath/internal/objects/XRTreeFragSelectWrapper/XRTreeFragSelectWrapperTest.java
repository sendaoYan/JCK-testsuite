/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XRTreeFragSelectWrapper conformance: reflection.
 * @run main XRTreeFragSelectWrapperTest
 */

public class XRTreeFragSelectWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XRTreeFragSelectWrapper", false, XRTreeFragSelectWrapperTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XRTreeFragSelectWrapper"), "simple name XRTreeFragSelectWrapper");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.objects.XRTreeFrag"), "superclass com.sun.org.apache.xpath.internal.objects.XRTreeFrag");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "execute"), "method execute");
        check(has(c, "detach"), "method detach");
        check(has(c, "num"), "method num");
        check(has(c, "xstr"), "method xstr");
        check(has(c, "str"), "method str");
        check(has(c, "getType"), "method getType");
        check(has(c, "rtf"), "method rtf");
        check(has(c, "asNodeIterator"), "method asNodeIterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
