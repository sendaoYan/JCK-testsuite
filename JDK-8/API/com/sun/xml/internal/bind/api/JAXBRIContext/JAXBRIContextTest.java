/*
 * @test
 * @summary com.sun.xml.internal.bind.api.JAXBRIContext conformance: abstract class hierarchy and members.
 * @run main JAXBRIContextTest
 */

public class JAXBRIContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.api.JAXBRIContext", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.api.JAXBRIContext"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("javax.xml.bind.JAXBContext".equals(c.getSuperclass().getName()), "extends javax.xml.bind.JAXBContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBridge"), "has method createBridge");
        check(mn.contains("createBridgeContext"), "has method createBridgeContext");
        check(mn.contains("generateEpisode"), "has method generateEpisode");
        check(mn.contains("generateSchema"), "has method generateSchema");
        check(mn.contains("getBaseType"), "has method getBaseType");
        check(mn.contains("getBuildId"), "has method getBuildId");
        check(mn.contains("getElementName"), "has method getElementName");
        check(mn.contains("getElementPropertyAccessor"), "has method getElementPropertyAccessor");
        check(mn.contains("getKnownNamespaceURIs"), "has method getKnownNamespaceURIs");
        check(mn.contains("getRuntimeTypeInfoSet"), "has method getRuntimeTypeInfoSet");
        check(mn.contains("getTypeName"), "has method getTypeName");
        check(mn.contains("hasSwaRef"), "has method hasSwaRef");
        check(mn.contains("mangleNameToClassName"), "has method mangleNameToClassName");
        check(mn.contains("mangleNameToPropertyName"), "has method mangleNameToPropertyName");
        check(mn.contains("mangleNameToVariableName"), "has method mangleNameToVariableName");
        check(mn.contains("newInstance"), "has method newInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
