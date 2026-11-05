/*
 * @test
 * @summary jdk.xml.internal.JdkXmlUtils conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main JdkXmlUtilsTest
 */
public class JdkXmlUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlUtils", false, JdkXmlUtilsTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkXmlUtils"), "name");
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
        check(fd.size() == 14, "field-count");
        check(fd.contains("IS_WINDOWS"), "field-IS_WINDOWS");
        check(fd.contains("JAVA_HOME"), "field-JAVA_HOME");
        check(fd.contains("NAMESPACES_FEATURE"), "field-NAMESPACES_FEATURE");
        check(fd.contains("NAMESPACE_PREFIXES_FEATURE"), "field-NAMESPACE_PREFIXES_FEATURE");
        check(fd.contains("USE_CATALOG"), "field-USE_CATALOG");
        check(fd.contains("SP_USE_CATALOG"), "field-SP_USE_CATALOG");
        check(fd.contains("CATALOG_FILES"), "field-CATALOG_FILES");
        check(fd.contains("CATALOG_DEFER"), "field-CATALOG_DEFER");
        check(fd.contains("CATALOG_PREFER"), "field-CATALOG_PREFER");
        check(fd.contains("CATALOG_RESOLVE"), "field-CATALOG_RESOLVE");
        check(fd.contains("RESOLVE_STRICT"), "field-RESOLVE_STRICT");
        check(fd.contains("RESOLVE_CONTINUE"), "field-RESOLVE_CONTINUE");
        check(fd.contains("RESOLVE_IGNORE"), "field-RESOLVE_IGNORE");
        check(fd.contains("USE_CATALOG_DEFAULT"), "field-USE_CATALOG_DEFAULT");
        check(md.size() == 17, "method-count");
        check(md.contains("catalogFeaturesConfig2Config"), "method-catalogFeaturesConfig2Config");
        check(md.contains("catalogFeaturesConfig2Reader"), "method-catalogFeaturesConfig2Reader");
        check(md.contains("getCatalogFeature"), "method-getCatalogFeature");
        check(md.contains("getCatalogFeatures"), "method-getCatalogFeatures");
        check(md.contains("getDOMDocument"), "method-getDOMDocument");
        check(md.contains("getDOMFactory"), "method-getDOMFactory");
        check(md.contains("getDTDExternalDecl"), "method-getDTDExternalDecl");
        check(md.contains("getProperty"), "method-getProperty");
        check(md.contains("getSAXFactory"), "method-getSAXFactory");
        check(md.contains("getSAXTransformFactory"), "method-getSAXTransformFactory");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("getXMLReader"), "method-getXMLReader");
        check(md.contains("initCatalogFeatures"), "method-initCatalogFeatures");
        check(md.contains("isResolveContinue"), "method-isResolveContinue");
        check(md.contains("setProperty"), "method-setProperty");
        check(md.contains("setReaderProperty"), "method-setReaderProperty");
        check(md.contains("setXMLReaderPropertyIfSupport"), "method-setXMLReaderPropertyIfSupport");
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
    static String fld(Class<?> c, String n) throws Exception { java.lang.reflect.Field f = c.getDeclaredField(n); f.setAccessible(true); return (String) f.get(null); }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlUtils", false, JdkXmlUtilsTest.class.getClassLoader());
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager", false, JdkXmlUtilsTest.class.getClassLoader());
        Class<?> pmC = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager", false, JdkXmlUtilsTest.class.getClassLoader());
        Object xsm = smC.getDeclaredConstructor().newInstance();
        Object xspm = pmC.getDeclaredConstructor().newInstance();
        java.lang.reflect.Method setP = c.getMethod("setProperty",
                Class.forName("jdk.xml.internal.XMLSecurityManager", false, JdkXmlUtilsTest.class.getClassLoader()),
                Class.forName("jdk.xml.internal.XMLSecurityPropertyManager", false, JdkXmlUtilsTest.class.getClassLoader()),
                String.class, Object.class);
        java.lang.reflect.Method getP = c.getMethod("getProperty",
                Class.forName("jdk.xml.internal.XMLSecurityManager", false, JdkXmlUtilsTest.class.getClassLoader()),
                Class.forName("jdk.xml.internal.XMLSecurityPropertyManager", false, JdkXmlUtilsTest.class.getClassLoader()),
                String.class);
        // setProperty/getProperty bridge: security-manager side
        check((Boolean) setP.invoke(null, xsm, xspm, "jdk.xml.maxElementDepth", "7"), "bridge-set-med");
        check(getP.invoke(null, xsm, xspm, "jdk.xml.maxElementDepth").equals("7"), "bridge-get-med");
        check((Boolean) setP.invoke(null, xsm, xspm, "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", "3"), "bridge-set-legacy-ee");
        check(getP.invoke(null, xsm, xspm, "jdk.xml.entityExpansionLimit").equals("3"), "bridge-get-ee");
        check(getP.invoke(null, xsm, xspm, "jdk.xml.dtd.support").equals("allow"), "bridge-get-dtd-default");
        // property-manager side
        check((Boolean) setP.invoke(null, xsm, xspm, javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "file"), "bridge-set-acc-dtd");
        check(getP.invoke(null, xsm, xspm, javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals("file"), "bridge-get-acc-dtd");
        // unmanaged name: neither manager claims it
        check(!(Boolean) setP.invoke(null, xsm, xspm, "bogus.prop", "x"), "bridge-set-bogus");
        check(getP.invoke(null, xsm, xspm, "bogus.prop") == null, "bridge-get-bogus-null");
        check(getP.invoke(null, null, xspm, javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA).equals("all"), "bridge-get-null-xsm");
        check(getP.invoke(null, xsm, null, "jdk.xml.maxOccurLimit").equals("5000"), "bridge-get-null-xspm");
        // getDTDExternalDecl exact rendering incl. quote switching
        java.lang.reflect.Method decl = c.getMethod("getDTDExternalDecl", String.class, String.class);
        check(decl.invoke(null, null, "sys.dtd").equals(" SYSTEM \"sys.dtd\""), "dtd-sys");
        check(decl.invoke(null, "pub", "sys.dtd").equals(" PUBLIC \"pub\" \"sys.dtd\""), "dtd-public-sys");
        check(decl.invoke(null, "pub", null).equals(" PUBLIC \"pub\""), "dtd-public-only");
        check(decl.invoke(null, null, null).equals(""), "dtd-empty");
        check(decl.invoke(null, "has\"dq", "s").equals(" PUBLIC 'has\"dq' \"s\""), "dtd-public-quote-switch");
        check(decl.invoke(null, "p", "has\"dq").equals(" PUBLIC \"p\" 'has\"dq'"), "dtd-sys-quote-switch");
        // getValue(Object,int) conversion matrix
        java.lang.reflect.Method gv = c.getMethod("getValue", Object.class, int.class);
        check(gv.invoke(null, null, 9).equals(9), "gv-null-default");
        check(gv.invoke(null, Integer.valueOf(7), 9).equals(7), "gv-integer");
        check(gv.invoke(null, "42", 9).equals(42), "gv-string");
        check(gv.invoke(null, 3.5d, 9).equals(3), "gv-double-truncates");
        try { gv.invoke(null, new Object(), 9); check(false, "gv-IAE"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof IllegalArgumentException, "gv-IAE"); }
        // catalog feature surface
        javax.xml.catalog.CatalogFeatures d = (javax.xml.catalog.CatalogFeatures) inv(c, "getCatalogFeatures");
        check(d.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER).equals("public"), "cat-default-prefer");
        check(d.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER).equals("true"), "cat-default-defer");
        check(d.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE).equals("strict"), "cat-default-resolve");
        javax.xml.catalog.CatalogFeatures c4 = (javax.xml.catalog.CatalogFeatures)
                inv(c, "getCatalogFeatures", "false", "file:///tmp/dxml27-cat.xml", "system", "continue");
        check(c4.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER).equals("false"), "cat-4-defer");
        check(c4.get(javax.xml.catalog.CatalogFeatures.Feature.FILES).equals("file:///tmp/dxml27-cat.xml"), "cat-4-files");
        check(c4.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER).equals("system"), "cat-4-prefer");
        check(c4.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE).equals("continue"), "cat-4-resolve");
        javax.xml.catalog.CatalogFeatures cn = (javax.xml.catalog.CatalogFeatures)
                inv(c, "getCatalogFeatures", null, null, null, null);
        check(cn.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE).equals("strict"), "cat-null-args-defaults");
        java.lang.reflect.Method irc = c.getMethod("isResolveContinue", javax.xml.catalog.CatalogFeatures.class);
        check((Boolean) irc.invoke(null, (Object) null), "isResolveContinue-null-true");
        check(!(Boolean) irc.invoke(null, d), "isResolveContinue-strict-false");
        check((Boolean) irc.invoke(null, c4), "isResolveContinue-continue");
        java.lang.reflect.Method gcf = c.getMethod("getCatalogFeature", javax.xml.catalog.CatalogFeatures.class, String.class);
        check(gcf.invoke(null, c4, "javax.xml.catalog.resolve").equals("continue"), "getCatalogFeature-resolve");
        check(gcf.invoke(null, d, "javax.xml.catalog.prefer").equals("public"), "getCatalogFeature-prefer");
        check(gcf.invoke(null, d, "bogus.name") == null, "getCatalogFeature-miss");
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        inv(c, "initCatalogFeatures", m);
        check(m.size() == 4, "initCatalogFeatures-size-4");
        check(m.containsKey("javax.xml.catalog.files"), "initCatalog-keys-files");
        check(m.containsKey("javax.xml.catalog.defer"), "initCatalog-keys-defer");
        check(m.containsKey("javax.xml.catalog.prefer"), "initCatalog-keys-prefer");
        check(m.containsKey("javax.xml.catalog.resolve"), "initCatalog-keys-resolve");
        // factory plumbing: system-default implementations, namespace aware, non-validating
        javax.xml.parsers.DocumentBuilderFactory dbf = (javax.xml.parsers.DocumentBuilderFactory) inv(c, "getDOMFactory", Boolean.FALSE);
        check(dbf.getClass().getName().equals("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"), "dom-impl-class");
        check(dbf.isNamespaceAware(), "dom-ns-aware");
        check(!dbf.isValidating(), "dom-not-validating");
        org.w3c.dom.Document doc = (org.w3c.dom.Document) inv(c, "getDOMDocument");
        check(doc != null && doc.getChildNodes() != null, "dom-document-new");
        javax.xml.parsers.SAXParserFactory spf = (javax.xml.parsers.SAXParserFactory) inv(c, "getSAXFactory", Boolean.FALSE);
        check(spf.getClass().getName().equals("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"), "sax-impl-class");
        check(spf.isNamespaceAware(), "sax-ns-aware");
        javax.xml.transform.sax.SAXTransformerFactory stf =
                (javax.xml.transform.sax.SAXTransformerFactory) inv(c, "getSAXTransformFactory", xsm, Boolean.FALSE);
        check(stf.getClass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"), "tf-impl-class");
        // getXMLReader with a security manager installed: real parse through the reader
        org.xml.sax.XMLReader reader = (org.xml.sax.XMLReader) inv(c, "getXMLReader", xsm, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, d);
        check(reader != null, "reader-nonnull");
        final int[] elems = {0};
        reader.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String qn, org.xml.sax.Attributes a) { elems[0]++; }
        });
        reader.parse(new org.xml.sax.InputSource(new java.io.StringReader("<a><b x='1'/><c/></a>")));
        check(elems[0] == 3, "reader-parse-3-elements");
        // setXMLReaderPropertyIfSupport swallows unknown properties without warning
        inv(c, "setXMLReaderPropertyIfSupport", reader, "http://dxml27/bogus", "v", Boolean.FALSE);
        check(true, "setXMLReaderPropertyIfSupport-no-throw");
        // constants
        check(fld(c, "USE_CATALOG").equals(javax.xml.XMLConstants.USE_CATALOG), "v-USE_CATALOG");
        check(fld(c, "SP_USE_CATALOG").equals("javax.xml.useCatalog"), "v-SP_USE_CATALOG");
        check(fld(c, "RESOLVE_STRICT").equals("strict"), "v-RESOLVE_STRICT");
        check(fld(c, "RESOLVE_CONTINUE").equals("continue"), "v-RESOLVE_CONTINUE");
        check(fld(c, "RESOLVE_IGNORE").equals("ignore"), "v-RESOLVE_IGNORE");
        check(fld(c, "CATALOG_FILES").equals("javax.xml.catalog.files"), "v-CATALOG_FILES");
        check(fld(c, "CATALOG_DEFER").equals("javax.xml.catalog.defer"), "v-CATALOG_DEFER");
        check(fld(c, "CATALOG_PREFER").equals("javax.xml.catalog.prefer"), "v-CATALOG_PREFER");
        check(fld(c, "CATALOG_RESOLVE").equals("javax.xml.catalog.resolve"), "v-CATALOG_RESOLVE");
        check(fld(c, "NAMESPACES_FEATURE").equals("http://xml.org/sax/features/namespaces"), "v-NAMESPACES_FEATURE");
        check(fld(c, "NAMESPACE_PREFIXES_FEATURE").equals("http://xml.org/sax/features/namespace-prefixes"), "v-NAMESPACE_PREFIXES_FEATURE");
        java.lang.reflect.Field iw = c.getDeclaredField("IS_WINDOWS"); iw.setAccessible(true);
        check(iw.get(null).equals(System.getProperty("os.name").contains("Windows")), "v-IS_WINDOWS-live");
        java.lang.reflect.Field jh = c.getDeclaredField("JAVA_HOME"); jh.setAccessible(true);
        check(jh.get(null).equals(System.getProperty("java.home")), "v-JAVA_HOME-live");
        java.lang.reflect.Field ucd = c.getDeclaredField("USE_CATALOG_DEFAULT"); ucd.setAccessible(true);
        check(ucd.get(null).equals(Boolean.TRUE), "v-USE_CATALOG_DEFAULT-true");
        // real-XML linkage: system-default DOM factory parses in-memory XML end to end
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document pd = db.parse(new java.io.ByteArrayInputStream(
                "<?xml version='1.0'?><root a='1'>text</root>".getBytes("UTF-8")));
        check(pd.getDocumentElement().getTagName().equals("root"), "parse-docel");
        check(pd.getDocumentElement().getAttribute("a").equals("1"), "parse-attr");
        check(pd.getDocumentElement().getTextContent().equals("text"), "parse-text");
    }
}
