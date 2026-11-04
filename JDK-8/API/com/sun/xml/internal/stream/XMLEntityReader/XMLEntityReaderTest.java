/*
 * @test
 * @summary com.sun.xml.internal.stream.XMLEntityReader conformance: abstract class hierarchy and members.
 * @run main XMLEntityReaderTest
 */

public class XMLEntityReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.XMLEntityReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.XMLEntityReader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.org.apache.xerces.internal.xni.XMLLocator"), "has interface com.sun.org.apache.xerces.internal.xni.XMLLocator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCharacterOffset"), "has method getCharacterOffset");
        check(mn.contains("getEncoding"), "has method getEncoding");
        check(mn.contains("getVersion"), "has method getVersion");
        check(mn.contains("isExternal"), "has method isExternal");
        check(mn.contains("peekChar"), "has method peekChar");
        check(mn.contains("registerListener"), "has method registerListener");
        check(mn.contains("scanChar"), "has method scanChar");
        check(mn.contains("scanContent"), "has method scanContent");
        check(mn.contains("scanData"), "has method scanData");
        check(mn.contains("scanLiteral"), "has method scanLiteral");
        check(mn.contains("scanName"), "has method scanName");
        check(mn.contains("scanNmtoken"), "has method scanNmtoken");
        check(mn.contains("scanQName"), "has method scanQName");
        check(mn.contains("setEncoding"), "has method setEncoding");
        check(mn.contains("setVersion"), "has method setVersion");
        check(mn.contains("skipChar"), "has method skipChar");
        check(mn.contains("skipSpaces"), "has method skipSpaces");
        check(mn.contains("skipString"), "has method skipString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
