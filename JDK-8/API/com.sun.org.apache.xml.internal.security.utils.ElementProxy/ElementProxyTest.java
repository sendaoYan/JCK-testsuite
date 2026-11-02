/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.ElementProxy conformance: abstract class hierarchy and members.
 * @run main ElementProxyTest
 */
public class ElementProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.ElementProxy", false, ElementProxyTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBaseNamespace"), "method getBaseNamespace");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        check(mn.contains("createElementForFamilyLocal"), "method createElementForFamilyLocal");
        check(mn.contains("createElementForFamily"), "method createElementForFamily");
        check(mn.contains("setElement"), "method setElement");
        check(mn.contains("getElement"), "method getElement");
        check(mn.contains("getElementPlusReturns"), "method getElementPlusReturns");
        check(mn.contains("createText"), "method createText");
        check(mn.contains("getDocument"), "method getDocument");
        check(mn.contains("getBaseURI"), "method getBaseURI");
        check(mn.contains("guaranteeThatElementInCorrectSpace"), "method guaranteeThatElementInCorrectSpace");
        check(mn.contains("addBigIntegerElement"), "method addBigIntegerElement");
        check(mn.contains("addReturnToSelf"), "method addReturnToSelf");
        check(mn.contains("addBase64Element"), "method addBase64Element");
        check(mn.contains("addTextElement"), "method addTextElement");
        check(mn.contains("addBase64Text"), "method addBase64Text");
        check(mn.contains("appendSelf"), "method appendSelf");
        check(mn.contains("appendOther"), "method appendOther");
        check(mn.contains("addText"), "method addText");
        check(mn.contains("getBigIntegerFromChildElement"), "method getBigIntegerFromChildElement");
        check(mn.contains("getTextFromChildElement"), "method getTextFromChildElement");
        check(mn.contains("getBytesFromTextChild"), "method getBytesFromTextChild");
        check(mn.contains("getTextFromTextChild"), "method getTextFromTextChild");
        check(mn.contains("length"), "method length");
        check(mn.contains("setXPathNamespaceContext"), "method setXPathNamespaceContext");
        check(mn.contains("setDefaultPrefix"), "method setDefaultPrefix");
        check(mn.contains("registerDefaultPrefixes"), "method registerDefaultPrefixes");
        check(mn.contains("getDefaultPrefix"), "method getDefaultPrefix");
        check(mn.contains("setDocument"), "method setDocument");
        check(mn.contains("getLocalAttribute"), "method getLocalAttribute");
        check(mn.contains("setLocalAttribute"), "method setLocalAttribute");
        check(mn.contains("setLocalIdAttribute"), "method setLocalIdAttribute");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
