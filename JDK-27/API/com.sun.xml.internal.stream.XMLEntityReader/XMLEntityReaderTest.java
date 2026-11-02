/*
 * @test
 * @summary com.sun.xml.internal.stream.XMLEntityReader conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream:+open
 * @run main XMLEntityReaderTest
 */
public class XMLEntityReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.XMLEntityReader", false, XMLEntityReaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.XMLEntityReader"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.xerces.internal.xni.XMLLocator"), "if-com.sun.org.apache.xerces.internal.xni.XMLLocator");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 18, "method-count");
        check(md.contains("getCharacterOffset"), "method-getCharacterOffset");
        check(md.contains("getEncoding"), "method-getEncoding");
        check(md.contains("getVersion"), "method-getVersion");
        check(md.contains("isExternal"), "method-isExternal");
        check(md.contains("peekChar"), "method-peekChar");
        check(md.contains("registerListener"), "method-registerListener");
        check(md.contains("scanChar"), "method-scanChar");
        check(md.contains("scanContent"), "method-scanContent");
        check(md.contains("scanData"), "method-scanData");
        check(md.contains("scanLiteral"), "method-scanLiteral");
        check(md.contains("scanName"), "method-scanName");
        check(md.contains("scanNmtoken"), "method-scanNmtoken");
        check(md.contains("scanQName"), "method-scanQName");
        check(md.contains("setEncoding"), "method-setEncoding");
        check(md.contains("setVersion"), "method-setVersion");
        check(md.contains("skipChar"), "method-skipChar");
        check(md.contains("skipSpaces"), "method-skipSpaces");
        check(md.contains("skipString"), "method-skipString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
