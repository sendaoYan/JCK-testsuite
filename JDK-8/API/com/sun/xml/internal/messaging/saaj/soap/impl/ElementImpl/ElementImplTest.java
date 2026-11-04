/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl conformance: class hierarchy and members.
 * @run main ElementImplTest
 */

public class ElementImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.org.apache.xerces.internal.dom.ElementNSImpl".equals(c.getSuperclass().getName()), "extends com.sun.org.apache.xerces.internal.dom.ElementNSImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("javax.xml.soap.SOAPBodyElement"), "has interface javax.xml.soap.SOAPBodyElement");
        check(ifs.contains("javax.xml.soap.SOAPElement"), "has interface javax.xml.soap.SOAPElement");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAttribute"), "has method addAttribute");
        check(mn.contains("addAttributeBare"), "has method addAttributeBare");
        check(mn.contains("addCDATA"), "has method addCDATA");
        check(mn.contains("addChildElement"), "has method addChildElement");
        check(mn.contains("addElement"), "has method addElement");
        check(mn.contains("addNamespaceDeclaration"), "has method addNamespaceDeclaration");
        check(mn.contains("addNode"), "has method addNode");
        check(mn.contains("addText"), "has method addText");
        check(mn.contains("addTextNode"), "has method addTextNode");
        check(mn.contains("convertToSoapElement"), "has method convertToSoapElement");
        check(mn.contains("createElement"), "has method createElement");
        check(mn.contains("createQName"), "has method createQName");
        check(mn.contains("detachNode"), "has method detachNode");
        check(mn.contains("doGetNamespacePrefixes"), "has method doGetNamespacePrefixes");
        check(mn.contains("ensureNamespaceIsDeclared"), "has method ensureNamespaceIsDeclared");
        check(mn.contains("findChild"), "has method findChild");
        check(mn.contains("findEncodingStyleAttributeName"), "has method findEncodingStyleAttributeName");
        check(mn.contains("getAllAttributes"), "has method getAllAttributes");
        check(mn.contains("getAllAttributesAsQNames"), "has method getAllAttributesAsQNames");
        check(mn.contains("getAllAttributesFrom"), "has method getAllAttributesFrom");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
