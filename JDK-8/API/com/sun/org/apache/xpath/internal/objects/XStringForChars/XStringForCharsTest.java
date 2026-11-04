/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XStringForChars conformance: reflection.
 * @run main XStringForCharsTest
 */

public class XStringForCharsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XStringForChars", false, XStringForCharsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XStringForChars"), "simple name XStringForChars");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.objects.XString"), "superclass com.sun.org.apache.xpath.internal.objects.XString");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "fsb"), "method fsb");
        check(has(c, "appendToFsb"), "method appendToFsb");
        check(has(c, "hasString"), "method hasString");
        check(has(c, "str"), "method str");
        check(has(c, "object"), "method object");
        check(has(c, "dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(has(c, "dispatchAsComment"), "method dispatchAsComment");
        check(has(c, "length"), "method length");
        check(has(c, "charAt"), "method charAt");
        check(has(c, "getChars"), "method getChars");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
