/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XString conformance: reflection.
 * @run main XStringTest
 */

public class XStringTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XString", false, XStringTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XString"), "simple name XString");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.objects.XObject"), "superclass com.sun.org.apache.xpath.internal.objects.XObject");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.utils.XMLString"), "interface com.sun.org.apache.xml.internal.utils.XMLString");
        check(has(c, "getType"), "method getType");
        check(has(c, "getTypeString"), "method getTypeString");
        check(has(c, "hasString"), "method hasString");
        check(has(c, "num"), "method num");
        check(has(c, "toDouble"), "method toDouble");
        check(has(c, "bool"), "method bool");
        check(has(c, "xstr"), "method xstr");
        check(has(c, "str"), "method str");
        check(has(c, "rtf"), "method rtf");
        check(has(c, "dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(has(c, "dispatchAsComment"), "method dispatchAsComment");
        check(has(c, "length"), "method length");
        check(has(c, "charAt"), "method charAt");
        check(has(c, "getChars"), "method getChars");
        check(has(c, "equals"), "method equals");
        check(has(c, "equalsIgnoreCase"), "method equalsIgnoreCase");
        check(has(c, "compareTo"), "method compareTo");
        check(has(c, "compareToIgnoreCase"), "method compareToIgnoreCase");
        check(has(c, "startsWith"), "method startsWith");
        check(has(c, "endsWith"), "method endsWith");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "indexOf"), "method indexOf");
        check(has(c, "lastIndexOf"), "method lastIndexOf");
        check(has(c, "substring"), "method substring");
        check(has(c, "concat"), "method concat");
        check(has(c, "toLowerCase"), "method toLowerCase");
        check(has(c, "toUpperCase"), "method toUpperCase");
        check(has(c, "trim"), "method trim");
        check(has(c, "fixWhiteSpace"), "method fixWhiteSpace");
        check(has(c, "callVisitors"), "method callVisitors");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
