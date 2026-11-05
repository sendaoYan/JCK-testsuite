/*
 * @test
 * @summary jdk.xml.internal.JdkXmlFeatures conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main JdkXmlFeaturesTest
 */
public class JdkXmlFeaturesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlFeatures", false, JdkXmlFeaturesTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkXmlFeatures"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 4, "field-count");
        check(fd.contains("ORACLE_JAXP_PROPERTY_PREFIX"), "field-ORACLE_JAXP_PROPERTY_PREFIX");
        check(fd.contains("XML_FEATURE_MANAGER"), "field-XML_FEATURE_MANAGER");
        check(fd.contains("CATALOG_FEATURES"), "field-CATALOG_FEATURES");
        check(fd.contains("PROPERTY_USE_CATALOG"), "field-PROPERTY_USE_CATALOG");
        check(md.size() == 7, "method-count");
        check(md.contains("clone"), "method-clone");
        check(md.contains("cloneAndUpdate"), "method-cloneAndUpdate");
        check(md.contains("findByName"), "method-findByName");
        check(md.contains("getFeature"), "method-getFeature");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("setFeature"), "method-setFeature");
        check(md.contains("update"), "method-update");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object inv(Object t, String n, Object... a) throws Exception {
        Class<?> start = t instanceof Class ? (Class<?>) t : t.getClass();
        for (Class<?> k = start; k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (!m.getName().equals(n) || m.getParameterCount() != a.length) continue;
                boolean ok = true;
                for (int i = 0; i < a.length; i++) {
                    Class<?> p = m.getParameterTypes()[i];
                    if (a[i] == null) { if (p.isPrimitive()) { ok = false; break; } continue; }
                    Class<?> ac = a[i].getClass();
                    if (p.isPrimitive()) { if (!(ac == Integer.class || ac == Boolean.class)) { ok = false; break; } }
                    else if (!p.isAssignableFrom(ac)) { ok = false; break; }
                }
                if (ok) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }
    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlFeatures", false, JdkXmlFeaturesTest.class.getClassLoader());
        Class<?> xfC = Class.forName("jdk.xml.internal.JdkXmlFeatures$XmlFeature", false, JdkXmlFeaturesTest.class.getClassLoader());
        Class<?> stC = Class.forName("jdk.xml.internal.JdkProperty$State", false, JdkXmlFeaturesTest.class.getClassLoader());
        Object api = e(stC, "APIPROPERTY"), sys = e(stC, "SYSTEMPROPERTY"), dflt = e(stC, "DEFAULT");
        java.lang.reflect.Constructor<?> ctor = c.getConstructor(boolean.class);
        Object EEF = e(xfC, "ENABLE_EXTENSION_FUNCTION"), UC = e(xfC, "USE_CATALOG"),
               RST = e(xfC, "RESET_SYMBOL_TABLE_FEATURE"), ODP = e(xfC, "JDK_OVERRIDE_PARSER"),
               XSP = e(xfC, "JDK_XPATH_SYSTEM_PROPERTY"), TPL = e(xfC, "ENABLE_TEMPLATESIMPL_DESERIALIZATION");
        // constructor with secureProcessing=false seeds the declared defaults
        Object f0 = ctor.newInstance(Boolean.FALSE);
        check(inv(f0, "getFeature", EEF).equals(Boolean.FALSE), "f0-EEF-false");
        check(inv(f0, "getFeature", UC).equals(Boolean.TRUE), "f0-UC-true");
        check(inv(f0, "getFeature", RST).equals(Boolean.FALSE), "f0-RST-false");
        check(inv(f0, "getFeature", ODP).equals(Boolean.FALSE), "f0-ODP-false");
        check(inv(f0, "getFeature", XSP).equals(Boolean.FALSE), "f0-XSP-false");
        check(inv(f0, "getFeature", TPL).equals(Boolean.FALSE), "f0-TPL-false");
        // constructor with secureProcessing=true: enforced features get the enforced value
        Object f1 = ctor.newInstance(Boolean.TRUE);
        check(inv(f1, "getFeature", EEF).equals(Boolean.FALSE), "f1-EEF-enforced-false");
        check(inv(f1, "getFeature", UC).equals(Boolean.TRUE), "f1-UC-not-enforced-true");
        check(inv(f1, "getFeature", XSP).equals(Boolean.FALSE), "f1-XSP-enforced-false");
        check(inv(f1, "getFeature", TPL).equals(Boolean.FALSE), "f1-TPL-enforced-false");
        // getFeature(int) index view
        check(inv(f0, "getFeature", Integer.valueOf(1)).equals(Boolean.TRUE), "getFeature-int-UC");
        // setFeature(XmlFeature, State, boolean) + override precedence on index variant
        inv(f0, "setFeature", ODP, api, Boolean.TRUE);
        check(inv(f0, "getFeature", ODP).equals(Boolean.TRUE), "setFeature-enum-true");
        inv(f0, "setFeature", Integer.valueOf(3), dflt, Boolean.FALSE);
        check(inv(f0, "getFeature", Integer.valueOf(3)).equals(Boolean.TRUE), "lower-state-blocked");
        inv(f0, "setFeature", Integer.valueOf(3), api, Boolean.FALSE);
        check(inv(f0, "getFeature", Integer.valueOf(3)).equals(Boolean.FALSE), "same-state-overrides");
        // setFeature(int, State, Object) parses strings through Boolean.parseBoolean
        inv(f0, "setFeature", Integer.valueOf(2), api, "true");
        check(inv(f0, "getFeature", Integer.valueOf(2)).equals(Boolean.TRUE), "setFeature-string-parse");
        inv(f0, "setFeature", Integer.valueOf(2), api, "not-a-bool");
        check(inv(f0, "getFeature", Integer.valueOf(2)).equals(Boolean.FALSE), "parseBoolean-lenient");
        // setFeature(String, State, Object): managed names, legacy names, unmanaged
        check((Boolean) inv(f0, "setFeature", "jdk.xml.enableExtensionFunctions", api, Boolean.TRUE), "setFeature-name-sp");
        check(inv(f0, "getFeature", EEF).equals(Boolean.TRUE), "setFeature-name-sp-applied");
        check((Boolean) inv(f0, "setFeature", "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions", api, "true"), "setFeature-name-legacy");
        check((Boolean) inv(f0, "setFeature", "bogus.feature", api, Boolean.TRUE) == false, "setFeature-name-bogus");
        check((Boolean) inv(f0, "setFeature", javax.xml.XMLConstants.USE_CATALOG, api, Boolean.FALSE), "setFeature-name-usecatalog");
        check(inv(f0, "getFeature", UC).equals(Boolean.FALSE), "setFeature-name-usecatalog-applied");
        // findByName / getIndex
        check(inv(f0, "findByName", "jdk.xml.overrideDefaultParser") == ODP, "findByName-ODP");
        check(inv(f0, "findByName", "http://www.oracle.com/feature/use-service-mechanism") == ODP, "findByName-ODP-legacy");
        check(inv(f0, "findByName", "bogus") == null, "findByName-null");
        check((Integer) inv(f0, "getIndex", "jdk.xml.overrideDefaultParser") == 3, "getIndex-ODP-3");
        check((Integer) inv(f0, "getIndex", javax.xml.XMLConstants.USE_CATALOG) == 1, "getIndex-UC-1");
        check((Integer) inv(f0, "getIndex", "bogus") == -1, "getIndex-miss");
        // clone is a deep copy of the value/state arrays
        Object f0c = inv(f0, "clone");
        check(f0c != f0, "clone-distinct");
        inv(f0, "setFeature", UC, api, Boolean.TRUE);
        check(inv(f0c, "getFeature", UC).equals(Boolean.FALSE), "clone-independent");
        // update()/cloneAndUpdate() pick up system properties set afterwards
        System.setProperty("javax.xml.useCatalog", "false");
        Object f2 = ctor.newInstance(Boolean.FALSE);
        check(inv(f2, "getFeature", UC).equals(Boolean.TRUE), "ctor-does-not-read-sysprop");
        inv(f2, "update");
        check(inv(f2, "getFeature", UC).equals(Boolean.FALSE), "update-reads-sysprop");
        Object f3 = inv(ctor.newInstance(Boolean.FALSE), "cloneAndUpdate");
        check(inv(f3, "getFeature", UC).equals(Boolean.FALSE), "cloneAndUpdate-reads-sysprop");
        System.clearProperty("javax.xml.useCatalog");
        // enforced feature under FSP can still be relaxed by a system property
        Object f4 = ctor.newInstance(Boolean.TRUE);
        System.setProperty("jdk.xml.enableExtensionFunctions", "true");
        inv(f4, "update");
        check(inv(f4, "getFeature", EEF).equals(Boolean.TRUE), "sysprop-overrides-fsp");
        System.clearProperty("jdk.xml.enableExtensionFunctions");
        // constants
        java.lang.reflect.Field cf;
        cf = c.getDeclaredField("ORACLE_JAXP_PROPERTY_PREFIX");
        cf.setAccessible(true); check(cf.get(null).equals("http://www.oracle.com/xml/jaxp/properties/"), "v-ORACLE_PREFIX");
        cf = c.getDeclaredField("XML_FEATURE_MANAGER");
        cf.setAccessible(true); check(cf.get(null).equals("http://www.oracle.com/xml/jaxp/properties/XmlFeatureManager"), "v-XML_FEATURE_MANAGER");
        cf = c.getDeclaredField("CATALOG_FEATURES");
        cf.setAccessible(true); check(cf.get(null).equals("javax.xml.catalog.catalogFeatures"), "v-CATALOG_FEATURES");
        cf = c.getDeclaredField("PROPERTY_USE_CATALOG");
        cf.setAccessible(true); check(cf.get(null).equals(javax.xml.XMLConstants.USE_CATALOG), "v-PROPERTY_USE_CATALOG");
        // real-XML linkage: javax.xml.useCatalog set before the FIRST factory creation in this JVM
        // (this test's deep() runs before any factory) flips the factory-visible USE_CATALOG feature;
        // the captured default is fixed at JdkXmlUtils class-init, so the factory keeps reporting false
        System.setProperty("javax.xml.useCatalog", "false");
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        check(dbf.getFeature(javax.xml.XMLConstants.USE_CATALOG) == false, "factory-usecatalog-false-from-sysprop");
        System.clearProperty("javax.xml.useCatalog");
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        check(!dbf2.getFeature(javax.xml.XMLConstants.USE_CATALOG), "default-frozen-at-class-init");
        dbf2.setFeature(javax.xml.XMLConstants.USE_CATALOG, true);
        check(dbf2.getFeature(javax.xml.XMLConstants.USE_CATALOG), "factory-usecatalog-setFeature-true");
        dbf2.setFeature(javax.xml.XMLConstants.USE_CATALOG, false);
        check(!dbf2.getFeature(javax.xml.XMLConstants.USE_CATALOG), "factory-usecatalog-setFeature-false");
    }
}
