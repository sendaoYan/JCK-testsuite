/*
 * @test
 * @summary jdk.xml.internal.JdkProperty.ImplPropMap conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main ImplPropMapTest
 */
public class ImplPropMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkProperty$ImplPropMap", false, ImplPropMapTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkProperty$ImplPropMap"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
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
        check(ec.size() == 8, "enum-constant-count");
        check(ec.contains("ISSTANDALONE"), "enum-ISSTANDALONE");
        check(ec.contains("XSLTCISSTANDALONE"), "enum-XSLTCISSTANDALONE");
        check(ec.contains("CDATACHUNKSIZE"), "enum-CDATACHUNKSIZE");
        check(ec.contains("EXTCLSLOADER"), "enum-EXTCLSLOADER");
        check(ec.contains("ENABLEEXTFUNC"), "enum-ENABLEEXTFUNC");
        check(ec.contains("OVERRIDEPARSER"), "enum-OVERRIDEPARSER");
        check(ec.contains("RESETSYMBOLTABLE"), "enum-RESETSYMBOLTABLE");
        check(ec.contains("ENTITYCOUNT"), "enum-ENTITYCOUNT");
        check(fd.size() == 0, "field-count");
        check(md.size() == 9, "method-count");
        check(md.contains("getState"), "method-getState");
        check(md.contains("is"), "method-is");
        check(md.contains("isNameDiffer"), "method-isNameDiffer");
        check(md.contains("qName"), "method-qName");
        check(md.contains("qNameOld"), "method-qNameOld");
        check(md.contains("systemProperty"), "method-systemProperty");
        check(md.contains("systemPropertyOld"), "method-systemPropertyOld");
        check(md.contains("valueOf"), "method-valueOf");
        check(md.contains("values"), "method-values");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }
    static Object inv(Object t, String n, Object... a) throws Exception {
        for (Class<?> k = t instanceof Class ? (Class<?>) t : t.getClass(); k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (m.getName().equals(n) && m.getParameterCount() == a.length) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkProperty$ImplPropMap", false, ImplPropMapTest.class.getClassLoader());
        // declaration order
        String[] order = {"ISSTANDALONE", "XSLTCISSTANDALONE", "CDATACHUNKSIZE", "EXTCLSLOADER", "ENABLEEXTFUNC", "OVERRIDEPARSER", "RESETSYMBOLTABLE", "ENTITYCOUNT"};
        Object[] vals = c.getEnumConstants();
        check(vals.length == 8, "values-length-8");
        for (int i = 0; i < order.length; i++) check(vals[i].toString().equals(order[i]), "order-" + order[i]);
        String P = "http://www.oracle.com/xml/jaxp/properties/";
        // exact name table: qName / systemProperty / legacy qName / legacy systemProperty / differ
        check(inv(e(c, "ISSTANDALONE"), "qName").equals(P + "isStandalone"), "q-ISSTANDALONE");
        check(inv(e(c, "ISSTANDALONE"), "systemProperty").equals("jdk.xml.isStandalone"), "sp-ISSTANDALONE");
        check(inv(e(c, "ISSTANDALONE"), "qNameOld") == null, "qOld-ISSTANDALONE");
        check(inv(e(c, "ISSTANDALONE"), "systemPropertyOld") == null, "spOld-ISSTANDALONE");
        check((Boolean) inv(e(c, "ISSTANDALONE"), "isNameDiffer"), "differ-ISSTANDALONE");
        check(inv(e(c, "XSLTCISSTANDALONE"), "qName").equals(P + "xsltcIsStandalone"), "q-XSLTCISSTANDALONE");
        check(inv(e(c, "XSLTCISSTANDALONE"), "systemProperty").equals("jdk.xml.xsltcIsStandalone"), "sp-XSLTCISSTANDALONE");
        check(inv(e(c, "XSLTCISSTANDALONE"), "qNameOld").equals("http://www.oracle.com/xml/is-standalone"), "qOld-XSLTCISSTANDALONE");
        check(inv(e(c, "XSLTCISSTANDALONE"), "systemPropertyOld") == null, "spOld-XSLTCISSTANDALONE");
        check((Boolean) inv(e(c, "XSLTCISSTANDALONE"), "isNameDiffer"), "differ-XSLTCISSTANDALONE");
        check(inv(e(c, "CDATACHUNKSIZE"), "qName").equals("jdk.xml.cdataChunkSize"), "q-CDATACHUNKSIZE");
        check(inv(e(c, "CDATACHUNKSIZE"), "systemProperty").equals("jdk.xml.cdataChunkSize"), "sp-CDATACHUNKSIZE");
        check(!(Boolean) inv(e(c, "CDATACHUNKSIZE"), "isNameDiffer"), "differ-CDATACHUNKSIZE");
        check(inv(e(c, "EXTCLSLOADER"), "qName").equals("jdk.xml.extensionClassLoader"), "q-EXTCLSLOADER");
        check(inv(e(c, "EXTCLSLOADER"), "systemProperty") == null, "sp-EXTCLSLOADER-null");
        check(inv(e(c, "EXTCLSLOADER"), "qNameOld").equals("jdk.xml.transform.extensionClassLoader"), "qOld-EXTCLSLOADER");
        check((Boolean) inv(e(c, "EXTCLSLOADER"), "isNameDiffer"), "differ-EXTCLSLOADER");
        check(inv(e(c, "ENABLEEXTFUNC"), "qName").equals(P + "enableExtensionFunctions"), "q-ENABLEEXTFUNC");
        check(inv(e(c, "ENABLEEXTFUNC"), "systemProperty").equals("jdk.xml.enableExtensionFunctions"), "sp-ENABLEEXTFUNC");
        check(inv(e(c, "ENABLEEXTFUNC"), "qNameOld") == null, "qOld-ENABLEEXTFUNC");
        check(inv(e(c, "ENABLEEXTFUNC"), "systemPropertyOld").equals("javax.xml.enableExtensionFunctions"), "spOld-ENABLEEXTFUNC");
        check((Boolean) inv(e(c, "ENABLEEXTFUNC"), "isNameDiffer"), "differ-ENABLEEXTFUNC");
        check(inv(e(c, "OVERRIDEPARSER"), "qName").equals("jdk.xml.overrideDefaultParser"), "q-OVERRIDEPARSER");
        check(inv(e(c, "OVERRIDEPARSER"), "systemProperty").equals("jdk.xml.overrideDefaultParser"), "sp-OVERRIDEPARSER");
        check(inv(e(c, "OVERRIDEPARSER"), "qNameOld").equals("http://www.oracle.com/feature/use-service-mechanism"), "qOld-OVERRIDEPARSER");
        check(inv(e(c, "OVERRIDEPARSER"), "systemPropertyOld").equals("http://www.oracle.com/feature/use-service-mechanism"), "spOld-OVERRIDEPARSER");
        check(!(Boolean) inv(e(c, "OVERRIDEPARSER"), "isNameDiffer"), "differ-OVERRIDEPARSER");
        check(inv(e(c, "RESETSYMBOLTABLE"), "qName").equals("jdk.xml.resetSymbolTable"), "q-RESETSYMBOLTABLE");
        check(inv(e(c, "RESETSYMBOLTABLE"), "systemProperty").equals("jdk.xml.resetSymbolTable"), "sp-RESETSYMBOLTABLE");
        check(inv(e(c, "RESETSYMBOLTABLE"), "qNameOld") == null, "qOld-RESETSYMBOLTABLE");
        check(!(Boolean) inv(e(c, "RESETSYMBOLTABLE"), "isNameDiffer"), "differ-RESETSYMBOLTABLE");
        check(inv(e(c, "ENTITYCOUNT"), "qName").equals("jdk.xml.getEntityCountInfo"), "q-ENTITYCOUNT");
        check(inv(e(c, "ENTITYCOUNT"), "systemProperty") == null, "sp-ENTITYCOUNT-null");
        check(inv(e(c, "ENTITYCOUNT"), "qNameOld").equals(P + "getEntityCountInfo"), "qOld-ENTITYCOUNT");
        check((Boolean) inv(e(c, "ENTITYCOUNT"), "isNameDiffer"), "differ-ENTITYCOUNT");
        // is(String) matrix: spName, qName (when differ), legacy qName; spOld is NOT matched by is()
        Object eef = e(c, "ENABLEEXTFUNC");
        check((Boolean) inv(eef, "is", "jdk.xml.enableExtensionFunctions"), "is-EEF-sp");
        check((Boolean) inv(eef, "is", P + "enableExtensionFunctions"), "is-EEF-q");
        check(!(Boolean) inv(eef, "is", "javax.xml.enableExtensionFunctions"), "is-EEF-spOld-false");
        check(!(Boolean) inv(eef, "is", "bogus"), "is-EEF-bogus");
        Object cl = e(c, "EXTCLSLOADER");
        check((Boolean) inv(cl, "is", "jdk.xml.extensionClassLoader"), "is-CL-q");
        check((Boolean) inv(cl, "is", "jdk.xml.transform.extensionClassLoader"), "is-CL-qOld");
        Object od = e(c, "OVERRIDEPARSER");
        check((Boolean) inv(od, "is", "jdk.xml.overrideDefaultParser"), "is-OD-sp");
        check((Boolean) inv(od, "is", "http://www.oracle.com/feature/use-service-mechanism"), "is-OD-qOld");
        Object cd = e(c, "CDATACHUNKSIZE");
        check((Boolean) inv(cd, "is", "jdk.xml.cdataChunkSize"), "is-CD-sp");
        check(!(Boolean) inv(cd, "is", "entityExpansionLimit"), "is-CD-cross");
        // getState(String): APIPROPERTY / LEGACY_APIPROPERTY / null
        check(inv(eef, "getState", "jdk.xml.enableExtensionFunctions").toString().equals("APIPROPERTY"), "getState-EEF-sp");
        check(inv(eef, "getState", P + "enableExtensionFunctions").toString().equals("LEGACY_APIPROPERTY"), "getState-EEF-q");
        check(inv(eef, "getState", "javax.xml.enableExtensionFunctions") == null, "getState-EEF-spOld-null");
        check(inv(eef, "getState", "bogus") == null, "getState-EEF-bogus");
        check(inv(cl, "getState", "jdk.xml.extensionClassLoader").toString().equals("APIPROPERTY"), "getState-CL-q");
        check(inv(cl, "getState", "jdk.xml.transform.extensionClassLoader").toString().equals("LEGACY_APIPROPERTY"), "getState-CL-qOld");
        Object ec = e(c, "ENTITYCOUNT");
        check(inv(ec, "getState", "jdk.xml.getEntityCountInfo").toString().equals("APIPROPERTY"), "getState-EC-q");
        check(inv(ec, "getState", P + "getEntityCountInfo").toString().equals("LEGACY_APIPROPERTY"), "getState-EC-qOld");
        check(inv(cd, "getState", "jdk.xml.cdataChunkSize").toString().equals("APIPROPERTY"), "getState-CD");
        check(inv(e(c, "ISSTANDALONE"), "getState", P + "isStandalone").toString().equals("LEGACY_APIPROPERTY"), "getState-IS-q");
        // valueOf round trip
        for (Object v : vals) check(java.lang.Enum.valueOf((Class<Enum>) (Class) c, v.toString()) == v, "valueOf-" + v);
        // real-XML linkage: the CDATACHUNKSIZE system property is a live StAX knob
        javax.xml.stream.XMLInputFactory xif = javax.xml.stream.XMLInputFactory.newInstance();
        xif.setProperty((String) inv(cd, "qName"), Integer.valueOf(18));
        check(xif.getProperty((String) inv(cd, "qName")).equals(Integer.valueOf(18)), "stax-cdata-chunk-size");
        // real-XML linkage: OVERRIDEPARSER feature name is accepted by factories
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setFeature((String) inv(od, "qName"), true);
        check(spf.getFeature((String) inv(od, "qName")), "sax-override-parser-feature");
    }
}
