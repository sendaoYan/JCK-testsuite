/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.ElementBeanInfoImpl conformance: class hierarchy and members.
 * @run main ElementBeanInfoImplTest
 */

public class ElementBeanInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.ElementBeanInfoImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.ElementBeanInfoImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.JaxBeanInfo".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.JaxBeanInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createInstance"), "has method createInstance");
        check(mn.contains("createInstanceFromValue"), "has method createInstanceFromValue");
        check(mn.contains("getElementLocalName"), "has method getElementLocalName");
        check(mn.contains("getElementNamespaceURI"), "has method getElementNamespaceURI");
        check(mn.contains("getId"), "has method getId");
        check(mn.contains("getLoader"), "has method getLoader");
        check(mn.contains("getTransducer"), "has method getTransducer");
        check(mn.contains("link"), "has method link");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("serializeAttributes"), "has method serializeAttributes");
        check(mn.contains("serializeBody"), "has method serializeBody");
        check(mn.contains("serializeRoot"), "has method serializeRoot");
        check(mn.contains("serializeURIs"), "has method serializeURIs");
        check(mn.contains("wrapUp"), "has method wrapUp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
