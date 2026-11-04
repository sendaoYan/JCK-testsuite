/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DOMNormalizer.XMLAttributesProxy conformance: class hierarchy and members.
 * @run main XMLAttributesProxyTest
 */
public class XMLAttributesProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DOMNormalizer$XMLAttributesProxy", false, XMLAttributesProxyTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLAttributes"), "has interface com.sun.org.apache.xerces.internal.xni.XMLAttributes");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setAttributes"), "method setAttributes");
        check(mn.contains("addAttribute"), "method addAttribute");
        check(mn.contains("removeAllAttributes"), "method removeAllAttributes");
        check(mn.contains("removeAttributeAt"), "method removeAttributeAt");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("setName"), "method setName");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getQName"), "method getQName");
        check(mn.contains("getQualifiedName"), "method getQualifiedName");
        check(mn.contains("setType"), "method setType");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("setValue"), "method setValue");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("setNonNormalizedValue"), "method setNonNormalizedValue");
        check(mn.contains("getNonNormalizedValue"), "method getNonNormalizedValue");
        check(mn.contains("setSpecified"), "method setSpecified");
        check(mn.contains("isSpecified"), "method isSpecified");
        check(mn.contains("getAugmentations"), "method getAugmentations");
        check(mn.contains("setAugmentations"), "method setAugmentations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
