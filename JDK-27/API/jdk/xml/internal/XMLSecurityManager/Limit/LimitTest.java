/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.Limit conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main LimitTest
 */
public class LimitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：JAXP 限额错误消息随默认 Locale 本地化（如 zh_CN 为中文），断言的是英文消息
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, LimitTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$Limit"), "name");
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
        check(ec.size() == 16, "enum-constant-count");
        check(ec.contains("ENTITY_EXPANSION_LIMIT"), "enum-ENTITY_EXPANSION_LIMIT");
        check(ec.contains("MAX_OCCUR_NODE_LIMIT"), "enum-MAX_OCCUR_NODE_LIMIT");
        check(ec.contains("ELEMENT_ATTRIBUTE_LIMIT"), "enum-ELEMENT_ATTRIBUTE_LIMIT");
        check(ec.contains("TOTAL_ENTITY_SIZE_LIMIT"), "enum-TOTAL_ENTITY_SIZE_LIMIT");
        check(ec.contains("GENERAL_ENTITY_SIZE_LIMIT"), "enum-GENERAL_ENTITY_SIZE_LIMIT");
        check(ec.contains("PARAMETER_ENTITY_SIZE_LIMIT"), "enum-PARAMETER_ENTITY_SIZE_LIMIT");
        check(ec.contains("MAX_ELEMENT_DEPTH_LIMIT"), "enum-MAX_ELEMENT_DEPTH_LIMIT");
        check(ec.contains("MAX_NAME_LIMIT"), "enum-MAX_NAME_LIMIT");
        check(ec.contains("ENTITY_REPLACEMENT_LIMIT"), "enum-ENTITY_REPLACEMENT_LIMIT");
        check(ec.contains("XPATH_GROUP_LIMIT"), "enum-XPATH_GROUP_LIMIT");
        check(ec.contains("XPATH_OP_LIMIT"), "enum-XPATH_OP_LIMIT");
        check(ec.contains("XPATH_TOTALOP_LIMIT"), "enum-XPATH_TOTALOP_LIMIT");
        check(ec.contains("DTD"), "enum-DTD");
        check(ec.contains("XERCES_DISALLOW_DTD"), "enum-XERCES_DISALLOW_DTD");
        check(ec.contains("STAX_SUPPORT_DTD"), "enum-STAX_SUPPORT_DTD");
        check(ec.contains("JDKCATALOG_RESOLVE"), "enum-JDKCATALOG_RESOLVE");
        check(fd.size() == 0, "field-count");
        check(md.size() == 11, "method-count");
        check(md.contains("apiProperty"), "method-apiProperty");
        check(md.contains("defaultValue"), "method-defaultValue");
        check(md.contains("getState"), "method-getState");
        check(md.contains("is"), "method-is");
        check(md.contains("isSupported"), "method-isSupported");
        check(md.contains("key"), "method-key");
        check(md.contains("mapper"), "method-mapper");
        check(md.contains("spOld"), "method-spOld");
        check(md.contains("systemProperty"), "method-systemProperty");
        check(md.contains("valueOf"), "method-valueOf");
        check(md.contains("values"), "method-values");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object inv(Object t, String n, Object... a) throws Exception {
        for (Class<?> k = t instanceof Class ? (Class<?>) t : t.getClass(); k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (!m.getName().equals(n) || m.getParameterCount() != a.length) continue;
                boolean ok = true;
                for (int i = 0; i < a.length; i++) {
                    if (a[i] == null) { if (m.getParameterTypes()[i].isPrimitive()) { ok = false; break; } continue; }
                    Class<?> p = m.getParameterTypes()[i];
                    if (p.isPrimitive()) { if (a[i].getClass() != Integer.class && a[i].getClass() != Boolean.class) { ok = false; break; } }
                    else if (!p.isAssignableFrom(a[i].getClass())) { ok = false; break; }
                }
                if (ok) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }
    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, LimitTest.class.getClassLoader());
        Class<?> procC = Class.forName("jdk.xml.internal.XMLSecurityManager$Processor", false, LimitTest.class.getClassLoader());
        Object PARSER = e(procC, "PARSER"), XPATH = e(procC, "XPATH"), ANY = e(procC, "ANY");
        String P = "http://www.oracle.com/xml/jaxp/properties/";
        // full declaration order and per-constant attribute tables, exactly as declared in the source
        String[][] table = {
            {"ENTITY_EXPANSION_LIMIT",   "EntityExpansionLimit",         P + "entityExpansionLimit",        "jdk.xml.entityExpansionLimit",         "entityExpansionLimit",    "2500",  "2500",  "PARSER",  "IntegerMapper"},
            {"MAX_OCCUR_NODE_LIMIT",     "MaxOccurLimit",                P + "maxOccurLimit",               "jdk.xml.maxOccurLimit",                "maxOccurLimit",           "5000",  "5000",  "PARSER",  "IntegerMapper"},
            {"ELEMENT_ATTRIBUTE_LIMIT",  "ElementAttributeLimit",        P + "elementAttributeLimit",       "jdk.xml.elementAttributeLimit",        "elementAttributeLimit",   "200",   "200",   "PARSER",  "IntegerMapper"},
            {"TOTAL_ENTITY_SIZE_LIMIT",  "TotalEntitySizeLimit",         P + "totalEntitySizeLimit",        "jdk.xml.totalEntitySizeLimit",         null,                      "100000","100000","PARSER",  "IntegerMapper"},
            {"GENERAL_ENTITY_SIZE_LIMIT","MaxEntitySizeLimit",           P + "maxGeneralEntitySizeLimit",   "jdk.xml.maxGeneralEntitySizeLimit",    null,                      "100000","100000","PARSER",  "IntegerMapper"},
            {"PARAMETER_ENTITY_SIZE_LIMIT","MaxParameterEntitySizeLimit",P + "maxParameterEntitySizeLimit", "jdk.xml.maxParameterEntitySizeLimit",  null,                      "15000", "15000", "PARSER",  "IntegerMapper"},
            {"MAX_ELEMENT_DEPTH_LIMIT",  "MaxElementDepthLimit",         P + "maxElementDepth",             "jdk.xml.maxElementDepth",              null,                      "100",   "100",   "PARSER",  "IntegerMapper"},
            {"MAX_NAME_LIMIT",           "MaxXMLNameLimit",              P + "maxXMLNameLimit",             "jdk.xml.maxXMLNameLimit",              null,                      "1000",  "1000",  "PARSER",  "IntegerMapper"},
            {"ENTITY_REPLACEMENT_LIMIT", "EntityReplacementLimit",       P + "entityReplacementLimit",      "jdk.xml.entityReplacementLimit",       null,                      "100000","100000","PARSER",  "IntegerMapper"},
            {"XPATH_GROUP_LIMIT",        "XPathGroupLimit",              "jdk.xml.xpathExprGrpLimit",       "jdk.xml.xpathExprGrpLimit",            null,                      "10",    "10",    "XPATH",   "IntegerMapper"},
            {"XPATH_OP_LIMIT",           "XPathExprOpLimit",             "jdk.xml.xpathExprOpLimit",        "jdk.xml.xpathExprOpLimit",             null,                      "100",   "100",   "XPATH",   "IntegerMapper"},
            {"XPATH_TOTALOP_LIMIT",      "XPathTotalOpLimit",            "jdk.xml.xpathTotalOpLimit",       "jdk.xml.xpathTotalOpLimit",            null,                      "10000", "10000", "XPATH",   "IntegerMapper"},
            {"DTD",                      "DTDProperty",                  "jdk.xml.dtd.support",             "jdk.xml.dtd.support",                  null,                      "0",     "0",     "PARSER",  "StringMapper"},
            {"XERCES_DISALLOW_DTD",      "disallowDTD",                  "http://apache.org/xml/features/disallow-doctype-decl", null, null,              "0",     "0",     "PARSER",  "BooleanMapper"},
            {"STAX_SUPPORT_DTD",         "supportDTD",                   "javax.xml.stream.supportDTD",     null,                                   null,                      "1",     "1",     "PARSER",  "BooleanMapper"},
            {"JDKCATALOG_RESOLVE",       "JDKCatalogResolve",            "jdk.xml.jdkcatalog.resolve",      "jdk.xml.jdkcatalog.resolve",           null,                      "0",     "0",     "PARSER",  "StringMapper"},
        };
        Object[] vals = c.getEnumConstants();
        check(vals.length == 16, "values-length-16");
        for (int i = 0; i < table.length; i++) {
            Object l = e(c, table[i][0]);
            check(vals[i] == l, "order-" + table[i][0]);
            check(((Enum<?>) l).ordinal() == i, "ordinal-" + table[i][0]);
            check(inv(l, "key").equals(table[i][1]), "key-" + table[i][0]);
            check(inv(l, "apiProperty").equals(table[i][2]), "api-" + table[i][0]);
            check(java.util.Objects.equals(inv(l, "systemProperty"), table[i][3]), "sp-" + table[i][0]);
            check(java.util.Objects.equals(inv(l, "spOld"), table[i][4]), "spOld-" + table[i][0]);
            check(inv(l, "defaultValue").equals(Integer.valueOf(table[i][5])), "def-" + table[i][0]);
            check(inv(l, "secureValue").equals(Integer.valueOf(table[i][6])), "sec-" + table[i][0]);
            check(inv(l, "mapper").getClass().getSimpleName().equals(table[i][8]), "mapper-" + table[i][0]);
            // isSupported: exactly the declared processor, never ANY
            check((Boolean) inv(l, "isSupported", e(procC, table[i][7])), "supp-" + table[i][7] + "-" + table[i][0]);
            check(!(Boolean) inv(l, "isSupported", ANY), "supp-ANY-false-" + table[i][0]);
            check(!(Boolean) inv(l, "isSupported", table[i][7].equals("PARSER") ? XPATH : PARSER), "supp-other-false-" + table[i][0]);
            // valueOf round trip
            check(java.lang.Enum.valueOf((Class<Enum>) (Class) c, table[i][0]) == l, "valueOf-" + table[i][0]);
        }
        // is(name): system property name and legacy URI name match; the pre-JDK-17 plain old name never does
        Object ee = e(c, "ENTITY_EXPANSION_LIMIT");
        check((Boolean) inv(ee, "is", "jdk.xml.entityExpansionLimit"), "is-sp");
        check((Boolean) inv(ee, "is", P + "entityExpansionLimit"), "is-uri-legacy");
        check(!(Boolean) inv(ee, "is", "entityExpansionLimit"), "is-spOld-plain-false");
        check(!(Boolean) inv(ee, "is", "bogus"), "is-bogus-false");
        Object xer = e(c, "XERCES_DISALLOW_DTD"), stax = e(c, "STAX_SUPPORT_DTD");
        check((Boolean) inv(xer, "is", "http://apache.org/xml/features/disallow-doctype-decl"), "is-xerces-uri");
        check(!(Boolean) inv(xer, "is", "disallowDTD"), "is-xerces-key-false");
        check((Boolean) inv(stax, "is", "javax.xml.stream.supportDTD"), "is-stax-api");
        // getState(name): sp -> APIPROPERTY, legacy URI -> LEGACY_APIPROPERTY, spOld/plain -> null
        check(inv(ee, "getState", "jdk.xml.entityExpansionLimit").toString().equals("APIPROPERTY"), "getState-sp-api");
        check(inv(ee, "getState", P + "entityExpansionLimit").toString().equals("LEGACY_APIPROPERTY"), "getState-uri-legacy");
        check(inv(ee, "getState", "entityExpansionLimit") == null, "getState-spOld-null");
        check(inv(ee, "getState", "bogus") == null, "getState-bogus-null");
        Object dtd = e(c, "DTD");
        check(inv(dtd, "getState", "jdk.xml.dtd.support").toString().equals("APIPROPERTY"), "getState-dtd-api");
        check(inv(dtd, "getState", "bogus") == null, "getState-dtd-bogus-null");
        // DTD and JDKCATALOG_RESOLVE share apiProperty and systemProperty (same name both ways)
        Object jcat = e(c, "JDKCATALOG_RESOLVE");
        check(inv(dtd, "apiProperty").equals(inv(dtd, "systemProperty")), "dtd-api-eq-sp");
        check(inv(jcat, "apiProperty").equals(inv(jcat, "systemProperty")), "jcat-api-eq-sp");
        // XPATH limits use bare jdk.xml.* names for both roles
        Object xg = e(c, "XPATH_GROUP_LIMIT");
        check(inv(xg, "apiProperty").equals("jdk.xml.xpathExprGrpLimit"), "xg-api");
        check(inv(xg, "apiProperty").equals(inv(xg, "systemProperty")), "xg-api-eq-sp");
        check(inv(xg, "spOld") == null, "xg-spOld-null");
        try { java.lang.Enum.valueOf((Class<Enum>) (Class) c, "NO_SUCH_LIMIT"); check(false, "valueOf-bogus"); }
        catch (IllegalArgumentException iae) { check(true, "valueOf-bogus-IAE"); }
        // real linkage 1: a PARSER-processor limit is a live factory attribute (value conversion + readback)
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute("jdk.xml.maxElementDepth", 2);
        check(dbf.getAttribute("jdk.xml.maxElementDepth").equals("2"), "factory-attr-readback");
        // real linkage 2: the same limit actually stops a too-deep document with its exact error code
        try {
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream("<a><b><c/></b></a>".getBytes("UTF-8")));
            check(false, "depth-limit-violates");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().equals("JAXP00010006: The element \"c\" has a depth of \"3\" that exceeds the limit \"2\" set by \"jdk.xml.maxElementDepth\"."),
                    "depth-limit-message");
        }
        // real linkage 3: within the limit the parse succeeds, and the attribute is per-factory
        dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream("<a><b/></a>".getBytes("UTF-8")));
        check(true, "depth-within-limit-ok");
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        check(dbf2.getAttribute("jdk.xml.maxElementDepth").equals("100"), "factory2-default-100");
        // real linkage 4: the plain legacy name is not an API name anywhere - factories reject it
        try {
            dbf2.setAttribute("entityExpansionLimit", "3");
            check(false, "legacy-plain-attr");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage().equals("Property 'entityExpansionLimit' is not recognized."), "legacy-plain-attr-IAE");
        }
        // real linkage 5: the StAX limit name is the live StAX property id
        check(inv(stax, "apiProperty").equals(javax.xml.stream.XMLInputFactory.SUPPORT_DTD), "stax-limit-name-is-StAX-const");
    }
}
