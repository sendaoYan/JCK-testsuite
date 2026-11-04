/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.XMLChar conformance: class hierarchy and members.
 * @run main XMLCharTest
 */
public class XMLCharTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.XMLChar", false, XMLCharTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isSupplemental"), "method isSupplemental");
        check(mn.contains("supplemental"), "method supplemental");
        check(mn.contains("highSurrogate"), "method highSurrogate");
        check(mn.contains("lowSurrogate"), "method lowSurrogate");
        check(mn.contains("isHighSurrogate"), "method isHighSurrogate");
        check(mn.contains("isLowSurrogate"), "method isLowSurrogate");
        check(mn.contains("isValid"), "method isValid");
        check(mn.contains("isInvalid"), "method isInvalid");
        check(mn.contains("isContent"), "method isContent");
        check(mn.contains("isMarkup"), "method isMarkup");
        check(mn.contains("isSpace"), "method isSpace");
        check(mn.contains("isNameStart"), "method isNameStart");
        check(mn.contains("isName"), "method isName");
        check(mn.contains("isNCNameStart"), "method isNCNameStart");
        check(mn.contains("isNCName"), "method isNCName");
        check(mn.contains("isPubid"), "method isPubid");
        check(mn.contains("isValidName"), "method isValidName");
        check(mn.contains("isValidNCName"), "method isValidNCName");
        check(mn.contains("isValidNmtoken"), "method isValidNmtoken");
        check(mn.contains("isValidIANAEncoding"), "method isValidIANAEncoding");
        check(mn.contains("isValidJavaEncoding"), "method isValidJavaEncoding");
        check(mn.contains("trim"), "method trim");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
