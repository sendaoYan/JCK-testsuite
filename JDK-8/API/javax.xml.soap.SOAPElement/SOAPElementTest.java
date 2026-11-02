/*
 * @test
 * @summary javax.xml.soap.SOAPElement conformance: interface hierarchy and methods.
 * @run main SOAPElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.soap.SOAPElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.Node"), "implements javax.xml.soap.Node");
        check(has(ifs, "org.w3c.dom.Element"), "implements org.w3c.dom.Element");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addChildElement"), "has method addChildElement");
        check(names.contains("removeContents"), "has method removeContents");
        check(names.contains("addTextNode"), "has method addTextNode");
        check(names.contains("addAttribute"), "has method addAttribute");
        check(names.contains("addNamespaceDeclaration"), "has method addNamespaceDeclaration");
        check(names.contains("getAttributeValue"), "has method getAttributeValue");
        check(names.contains("getAllAttributes"), "has method getAllAttributes");
        check(names.contains("getAllAttributesAsQNames"), "has method getAllAttributesAsQNames");
        check(names.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(names.contains("getNamespacePrefixes"), "has method getNamespacePrefixes");
        check(names.contains("getVisibleNamespacePrefixes"), "has method getVisibleNamespacePrefixes");
        check(names.contains("createQName"), "has method createQName");
        check(names.contains("getElementName"), "has method getElementName");
        check(names.contains("getElementQName"), "has method getElementQName");
        check(names.contains("setElementQName"), "has method setElementQName");
        check(names.contains("removeAttribute"), "has method removeAttribute");
        check(names.contains("removeNamespaceDeclaration"), "has method removeNamespaceDeclaration");
        check(names.contains("getChildElements"), "has method getChildElements");
        check(names.contains("setEncodingStyle"), "has method setEncodingStyle");
        check(names.contains("getEncodingStyle"), "has method getEncodingStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
