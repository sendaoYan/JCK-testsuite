/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XStringForFSB conformance: reflection.
 * @run main XStringForFSBTest
 */

public class XStringForFSBTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XStringForFSB", false, XStringForFSBTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XStringForFSB"), "simple name XStringForFSB");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.objects.XString"), "superclass com.sun.org.apache.xpath.internal.objects.XString");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "fsb"), "method fsb");
        check(has(c, "appendToFsb"), "method appendToFsb");
        check(has(c, "hasString"), "method hasString");
        check(has(c, "object"), "method object");
        check(has(c, "str"), "method str");
        check(has(c, "dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(has(c, "dispatchAsComment"), "method dispatchAsComment");
        check(has(c, "length"), "method length");
        check(has(c, "charAt"), "method charAt");
        check(has(c, "getChars"), "method getChars");
        check(has(c, "equals"), "method equals");
        check(has(c, "equalsIgnoreCase"), "method equalsIgnoreCase");
        check(has(c, "compareTo"), "method compareTo");
        check(has(c, "compareToIgnoreCase"), "method compareToIgnoreCase");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "startsWith"), "method startsWith");
        check(has(c, "indexOf"), "method indexOf");
        check(has(c, "substring"), "method substring");
        check(has(c, "concat"), "method concat");
        check(has(c, "trim"), "method trim");
        check(has(c, "fixWhiteSpace"), "method fixWhiteSpace");
        check(has(c, "toDouble"), "method toDouble");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
