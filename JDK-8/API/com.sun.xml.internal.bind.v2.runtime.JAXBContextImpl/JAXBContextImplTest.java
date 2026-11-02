/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl conformance: class hierarchy and members.
 * @run main JAXBContextImplTest
 */

public class JAXBContextImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.api.JAXBRIContext".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.api.JAXBRIContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createAugmented"), "has method createAugmented");
        check(mn.contains("createBinder"), "has method createBinder");
        check(mn.contains("createBridge"), "has method createBridge");
        check(mn.contains("createBridgeContext"), "has method createBridgeContext");
        check(mn.contains("createDom"), "has method createDom");
        check(mn.contains("createJAXBIntrospector"), "has method createJAXBIntrospector");
        check(mn.contains("createMarshaller"), "has method createMarshaller");
        check(mn.contains("createSchemaGenerator"), "has method createSchemaGenerator");
        check(mn.contains("createTransformer"), "has method createTransformer");
        check(mn.contains("createTransformerHandler"), "has method createTransformerHandler");
        check(mn.contains("createUnmarshaller"), "has method createUnmarshaller");
        check(mn.contains("createValidator"), "has method createValidator");
        check(mn.contains("generateEpisode"), "has method generateEpisode");
        check(mn.contains("generateSchema"), "has method generateSchema");
        check(mn.contains("getBeanInfo"), "has method getBeanInfo");
        check(mn.contains("getBuildId"), "has method getBuildId");
        check(mn.contains("getElement"), "has method getElement");
        check(mn.contains("getElementName"), "has method getElementName");
        check(mn.contains("getElementPropertyAccessor"), "has method getElementPropertyAccessor");
        check(mn.contains("getGlobalType"), "has method getGlobalType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
