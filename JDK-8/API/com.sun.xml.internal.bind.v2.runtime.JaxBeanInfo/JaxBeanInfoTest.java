/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.JaxBeanInfo conformance: abstract class hierarchy and members.
 * @run main JaxBeanInfoTest
 */

public class JaxBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.JaxBeanInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.JaxBeanInfo"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cacheLifecycleMethod"), "has method cacheLifecycleMethod");
        check(mn.contains("createInstance"), "has method createInstance");
        check(mn.contains("getElementLocalName"), "has method getElementLocalName");
        check(mn.contains("getElementNamespaceURI"), "has method getElementNamespaceURI");
        check(mn.contains("getId"), "has method getId");
        check(mn.contains("getLifecycleMethods"), "has method getLifecycleMethods");
        check(mn.contains("getLoader"), "has method getLoader");
        check(mn.contains("getTransducer"), "has method getTransducer");
        check(mn.contains("getTypeName"), "has method getTypeName");
        check(mn.contains("getTypeNames"), "has method getTypeNames");
        check(mn.contains("hasAfterMarshalMethod"), "has method hasAfterMarshalMethod");
        check(mn.contains("hasAfterUnmarshalMethod"), "has method hasAfterUnmarshalMethod");
        check(mn.contains("hasBeforeMarshalMethod"), "has method hasBeforeMarshalMethod");
        check(mn.contains("hasBeforeUnmarshalMethod"), "has method hasBeforeUnmarshalMethod");
        check(mn.contains("hasElementOnlyContentModel"), "has method hasElementOnlyContentModel");
        check(mn.contains("invokeAfterUnmarshalMethod"), "has method invokeAfterUnmarshalMethod");
        check(mn.contains("invokeBeforeUnmarshalMethod"), "has method invokeBeforeUnmarshalMethod");
        check(mn.contains("invokeUnmarshallCallback"), "has method invokeUnmarshallCallback");
        check(mn.contains("isElement"), "has method isElement");
        check(mn.contains("isImmutable"), "has method isImmutable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
