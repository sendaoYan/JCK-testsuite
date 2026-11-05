/*
 * @test
 * @summary jdk.xml.internal.JdkXmlConfig conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main JdkXmlConfigTest
 */
public class JdkXmlConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlConfig", false, JdkXmlConfigTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkXmlConfig"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
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
        check(fd.size() == 1, "field-count");
        check(fd.contains("JDKCATALOG_FILE"), "field-JDKCATALOG_FILE");
        check(md.size() == 6, "method-count");
        check(md.contains("getInstance"), "method-getInstance");
        check(md.contains("getJaxpConfig"), "method-getJaxpConfig");
        check(md.contains("getJdkCatalog"), "method-getJdkCatalog");
        check(md.contains("getXMLFeatures"), "method-getXMLFeatures");
        check(md.contains("getXMLSecurityManager"), "method-getXMLSecurityManager");
        check(md.contains("getXMLSecurityPropertyManager"), "method-getXMLSecurityPropertyManager");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object inv(Object t, String n, Object... a) throws Exception {
        for (Class<?> k = t instanceof Class ? (Class<?>) t : t.getClass(); k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (m.getName().equals(n) && m.getParameterCount() == a.length) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }
    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlConfig", false, JdkXmlConfigTest.class.getClassLoader());
        // constant surface
        java.lang.reflect.Field jf = c.getDeclaredField("JDKCATALOG_FILE");
        jf.setAccessible(true);
        check(jf.get(null).equals("/jdk/xml/internal/jdkcatalog/JDKCatalog.xml"), "v-JDKCATALOG_FILE");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "class-not-final");
        try { c.getDeclaredConstructor().newInstance(); check(false, "private-ctor"); }
        catch (NoSuchMethodException e1) { check(true, "private-ctor-blocked"); }
        catch (java.lang.reflect.InvocationTargetException | IllegalAccessException e2) { check(true, "private-ctor-blocked"); }

        // real config-file linkage: point java.xml.config.file at a temp properties file
        // BEFORE the singleton is created (this test's first JAXP touch)
        java.nio.file.Path cfgFile = java.nio.file.Files.createTempFile("dxml27-jaxp", ".properties");
        java.io.File cfg = cfgFile.toFile();
        boolean cfgExisted = false;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(java.nio.file.Files.newBufferedWriter(cfgFile))) {
            pw.println("jdk.xml.maxOccurLimit=4321");
            pw.println("javax.xml.accessExternalDTD=file");
        }
        System.setProperty("java.xml.config.file", cfg.getAbsolutePath());
        try {
            java.lang.reflect.Method gi = c.getMethod("getInstance", boolean.class);
            Object inst = gi.invoke(null, Boolean.FALSE);
            check(gi.invoke(null, Boolean.TRUE) == inst, "singleton-identity");
            // the config file entries are visible through the JAXP config Properties
            java.util.Properties props = (java.util.Properties) inv(inst, "getJaxpConfig");
            check(props.getProperty("jdk.xml.maxOccurLimit").equals("4321"), "config-maxOccurLimit");
            check(props.getProperty("javax.xml.accessExternalDTD").equals("file"), "config-accessExternalDTD");
            check(props.getProperty("dxml27.nope") == null, "config-miss-null");
            // cloneAndUpdate reads the config into the security manager copy
            Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager", false, JdkXmlConfigTest.class.getClassLoader());
            Class<?> limitC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, JdkXmlConfigTest.class.getClassLoader());
            Class<?> stC = Class.forName("jdk.xml.internal.JdkProperty$State", false, JdkXmlConfigTest.class.getClassLoader());
            Object mgrUpd = inv(inst, "getXMLSecurityManager", Boolean.TRUE);
            check(inv(mgrUpd, "getLimit", e(limitC, "MAX_OCCUR_NODE_LIMIT")).equals(Integer.valueOf(4321)), "manager-update-maxOccur");
            check(inv(mgrUpd, "getState", e(limitC, "MAX_OCCUR_NODE_LIMIT")).toString().equals("JAXPDOTPROPERTIES"), "manager-update-state");
            // non-updating copy keeps base defaults
            Object mgrBase = inv(inst, "getXMLSecurityManager", Boolean.FALSE);
            check(inv(mgrBase, "getLimit", e(limitC, "MAX_OCCUR_NODE_LIMIT")).equals(Integer.valueOf(5000)), "manager-base-default");
            check(inv(mgrBase, "getState", e(limitC, "MAX_OCCUR_NODE_LIMIT")).toString().equals("FSP"), "manager-base-fsp-state");
            check(inv(mgrBase, "isSecureProcessing").equals(Boolean.TRUE), "manager-base-secure");
            check(inv(inst, "getXMLSecurityManager", Boolean.FALSE) != mgrBase, "manager-copies-distinct");
            // property manager: config file feeds ACCESS_EXTERNAL_DTD through cloneAndUpdate
            Class<?> pmC = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager", false, JdkXmlConfigTest.class.getClassLoader());
            Class<?> propC = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager$Property", false, JdkXmlConfigTest.class.getClassLoader());
            Object pmUpd = inv(inst, "getXMLSecurityPropertyManager", Boolean.TRUE);
            check(inv(pmUpd, "getValue", e(propC, "ACCESS_EXTERNAL_DTD")).equals("file"), "propmgr-update-dtd");
            check(inv(pmUpd, "getValue", e(propC, "ACCESS_EXTERNAL_SCHEMA")).equals("all"), "propmgr-schema-default-all");
            // features copy: enforced defaults under secure processing
            Class<?> featC = Class.forName("jdk.xml.internal.JdkXmlFeatures", false, JdkXmlConfigTest.class.getClassLoader());
            Class<?> xfC = Class.forName("jdk.xml.internal.JdkXmlFeatures$XmlFeature", false, JdkXmlConfigTest.class.getClassLoader());
            Object feats = inv(inst, "getXMLFeatures", Boolean.FALSE);
            check(inv(feats, "getFeature", e(xfC, "ENABLE_EXTENSION_FUNCTION")).equals(Boolean.FALSE), "features-EEF-false");
            check(inv(feats, "getFeature", e(xfC, "USE_CATALOG")).equals(Boolean.TRUE), "features-USE_CATALOG-true");
            check(inv(feats, "clone") != feats, "features-clone-distinct");
            // JDK built-in catalog
            Object cat = inv(inst, "getJdkCatalog");
            check(cat != null, "jdkcatalog-nonnull");
            check(inv(inst, "getJdkCatalog") == cat, "jdkcatalog-cached");
            check(cat instanceof javax.xml.catalog.Catalog, "jdkcatalog-is-Catalog");
        } finally {
            System.clearProperty("java.xml.config.file");
            java.nio.file.Files.deleteIfExists(cfgFile);
            cfgExisted = cfg.exists();
        }
        check(!cfgExisted, "temp-config-deleted");
        // real-XML linkage: the config-driven manager value is live in a factory build
        // (factory built after clearing java.xml.config.file -> default 5000 behavior retained)
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute("jdk.xml.maxOccurLimit", "77");
        check(dbf.getAttribute("jdk.xml.maxOccurLimit").equals("77"), "factory-maxOccur-roundtrip");
    }
}
