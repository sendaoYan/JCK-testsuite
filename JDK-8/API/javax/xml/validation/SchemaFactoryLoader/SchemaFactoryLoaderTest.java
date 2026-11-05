/*
 * @test
 * @summary javax.xml.validation.SchemaFactoryLoader conformance: abstract class hierarchy and methods.
 * @run main SchemaFactoryLoaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SchemaFactoryLoaderTest {
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
        Class<?> c = Class.forName("javax.xml.validation.SchemaFactoryLoader");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("newFactory"), "has method newFactory");
        testDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    // The platform ships no discoverable SchemaFactoryLoader (factory lookup goes
    // through SchemaFactory.newInstance); the class is the user-extension point,
    // so a concrete subclass must dispatch newFactory by schema language.
    static void testDispatch() throws Exception {
        check(Class.forName("javax.xml.validation.SchemaFactoryLoader").getConstructors().length == 0,
              "no public constructors (protected ctor on abstract class)");
        final javax.xml.validation.SchemaFactory delegate =
            javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        javax.xml.validation.SchemaFactoryLoader loader = new javax.xml.validation.SchemaFactoryLoader() {
            public javax.xml.validation.SchemaFactory newFactory(String schemaLanguage) {
                return javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(schemaLanguage) ? delegate : null;
            }
        };
        check(loader.newFactory(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI) == delegate,
              "newFactory(W3C) dispatches to delegate");
        check(loader.newFactory("http://example.invalid/lang") == null,
              "newFactory(unsupported language) returns null");
        check(loader.newFactory(null) == null, "newFactory(null) returns null (subclass contract)");

        // platform discovery: no SchemaFactoryLoader provider ships with the JDK
        int found = 0;
        for (javax.xml.validation.SchemaFactoryLoader l
                : java.util.ServiceLoader.load(javax.xml.validation.SchemaFactoryLoader.class)) {
            found++;
        }
        check(found == 0, "no platform SchemaFactoryLoader providers discovered (recorded actual)");
    }
}
