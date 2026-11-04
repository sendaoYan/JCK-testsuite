/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.org.apache.xerces.util.XMLChar conformance: class hierarchy and members.
 * @run main XMLCharTest
 */

public class XMLCharTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.org.apache.xerces.util.XMLChar", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.org.apache.xerces.util.XMLChar"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("highSurrogate"), "has method highSurrogate");
        check(mn.contains("isContent"), "has method isContent");
        check(mn.contains("isHighSurrogate"), "has method isHighSurrogate");
        check(mn.contains("isInvalid"), "has method isInvalid");
        check(mn.contains("isLowSurrogate"), "has method isLowSurrogate");
        check(mn.contains("isMarkup"), "has method isMarkup");
        check(mn.contains("isNCName"), "has method isNCName");
        check(mn.contains("isNCNameStart"), "has method isNCNameStart");
        check(mn.contains("isName"), "has method isName");
        check(mn.contains("isNameStart"), "has method isNameStart");
        check(mn.contains("isPubid"), "has method isPubid");
        check(mn.contains("isSpace"), "has method isSpace");
        check(mn.contains("isSupplemental"), "has method isSupplemental");
        check(mn.contains("isValid"), "has method isValid");
        check(mn.contains("isValidIANAEncoding"), "has method isValidIANAEncoding");
        check(mn.contains("isValidJavaEncoding"), "has method isValidJavaEncoding");
        check(mn.contains("isValidNCName"), "has method isValidNCName");
        check(mn.contains("isValidName"), "has method isValidName");
        check(mn.contains("isValidNmtoken"), "has method isValidNmtoken");
        check(mn.contains("lowSurrogate"), "has method lowSurrogate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
