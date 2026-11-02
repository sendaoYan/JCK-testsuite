/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.XMLSerializer conformance: class hierarchy and members.
 * @run main XMLSerializerTest
 */

public class XMLSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.XMLSerializer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.XMLSerializer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.Coordinator".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.Coordinator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addInscopeBinding"), "has method addInscopeBinding");
        check(mn.contains("attWildcardAsAttributes"), "has method attWildcardAsAttributes");
        check(mn.contains("attWildcardAsURIs"), "has method attWildcardAsURIs");
        check(mn.contains("attribute"), "has method attribute");
        check(mn.contains("childAsRoot"), "has method childAsRoot");
        check(mn.contains("childAsSoleContent"), "has method childAsSoleContent");
        check(mn.contains("childAsXsiType"), "has method childAsXsiType");
        check(mn.contains("clearCurrentProperty"), "has method clearCurrentProperty");
        check(mn.contains("close"), "has method close");
        check(mn.contains("endAttributes"), "has method endAttributes");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endNamespaceDecls"), "has method endNamespaceDecls");
        check(mn.contains("errorMissingId"), "has method errorMissingId");
        check(mn.contains("fireAfterMarshalEvents"), "has method fireAfterMarshalEvents");
        check(mn.contains("fireBeforeMarshalEvents"), "has method fireBeforeMarshalEvents");
        check(mn.contains("fireMarshalEvent"), "has method fireMarshalEvent");
        check(mn.contains("getCachedBase64DataInstance"), "has method getCachedBase64DataInstance");
        check(mn.contains("getCurrentLocation"), "has method getCurrentLocation");
        check(mn.contains("getCurrentProperty"), "has method getCurrentProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
