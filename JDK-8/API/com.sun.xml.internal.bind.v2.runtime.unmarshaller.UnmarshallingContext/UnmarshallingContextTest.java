/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext conformance: class hierarchy and members.
 * @run main UnmarshallingContextTest
 */

public class UnmarshallingContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.Coordinator".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.Coordinator");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 5, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor"), "has interface com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor$TextPredictor"), "has interface com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor$TextPredictor");
        check(ifs.contains("javax.xml.bind.ValidationEventHandler"), "has interface javax.xml.bind.ValidationEventHandler");
        check(ifs.contains("javax.xml.namespace.NamespaceContext"), "has interface javax.xml.namespace.NamespaceContext");
        check(ifs.contains("org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_startElement"), "has method _startElement");
        check(mn.contains("addFactory"), "has method addFactory");
        check(mn.contains("addPatcher"), "has method addPatcher");
        check(mn.contains("addToIdTable"), "has method addToIdTable");
        check(mn.contains("clearResult"), "has method clearResult");
        check(mn.contains("clearStates"), "has method clearStates");
        check(mn.contains("createInstance"), "has method createInstance");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endPrefixMapping"), "has method endPrefixMapping");
        check(mn.contains("endScope"), "has method endScope");
        check(mn.contains("errorUnresolvedIDREF"), "has method errorUnresolvedIDREF");
        check(mn.contains("expectText"), "has method expectText");
        check(mn.contains("getAllDeclaredPrefixes"), "has method getAllDeclaredPrefixes");
        check(mn.contains("getAllPrefixesInList"), "has method getAllPrefixesInList");
        check(mn.contains("getContext"), "has method getContext");
        check(mn.contains("getCurrentExpectedAttributes"), "has method getCurrentExpectedAttributes");
        check(mn.contains("getCurrentExpectedElements"), "has method getCurrentExpectedElements");
        check(mn.contains("getCurrentState"), "has method getCurrentState");
        check(mn.contains("getInnerPeer"), "has method getInnerPeer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
