/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.BindingContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main BindingContextTest
 */

import java.lang.reflect.Modifier;


public class BindingContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.BindingContext", false, BindingContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.BindingContext"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBridge"), "method createBridge");
        check(mn.contains("createFragmentBridge"), "method createFragmentBridge");
        check(mn.contains("createMarshaller"), "method createMarshaller");
        check(mn.contains("createUnmarshaller"), "method createUnmarshaller");
        check(mn.contains("generateSchema"), "method generateSchema");
        check(mn.contains("getBuildId"), "method getBuildId");
        check(mn.contains("getElementName"), "method getElementName");
        check(mn.contains("getElementPropertyAccessor"), "method getElementPropertyAccessor");
        check(mn.contains("getJAXBContext"), "method getJAXBContext");
        check(mn.contains("getKnownNamespaceURIs"), "method getKnownNamespaceURIs");
        check(mn.contains("getTypeName"), "method getTypeName");
        check(mn.contains("hasSwaRef"), "method hasSwaRef");
        check(mn.contains("newWrapperInstace"), "method newWrapperInstace");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("CANONICALIZATION_SUPPORT"), "field CANONICALIZATION_SUPPORT");
        check(fn.contains("DEFAULT_NAMESPACE_REMAP"), "field DEFAULT_NAMESPACE_REMAP");
        check(fn.contains("ENABLE_XOP"), "field ENABLE_XOP");
        check(fn.contains("RETAIN_REFERENCE_TO_INFO"), "field RETAIN_REFERENCE_TO_INFO");
        check(fn.contains("SUBCLASS_REPLACEMENTS"), "field SUBCLASS_REPLACEMENTS");
        check(fn.contains("TREAT_EVERYTHING_NILLABLE"), "field TREAT_EVERYTHING_NILLABLE");
        check(fn.contains("TYPE_REFERENCES"), "field TYPE_REFERENCES");
        check(fn.contains("XMLACCESSORFACTORY_SUPPORT"), "field XMLACCESSORFACTORY_SUPPORT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
