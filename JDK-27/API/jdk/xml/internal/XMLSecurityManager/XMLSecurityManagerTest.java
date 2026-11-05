/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main XMLSecurityManagerTest
 */
public class XMLSecurityManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager", false, XMLSecurityManagerTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(fd.size() == 8, "field-count");
        check(fd.contains("DTD_KEY"), "field-DTD_KEY");
        check(fd.contains("DISALLOW_DTD"), "field-DISALLOW_DTD");
        check(fd.contains("LOAD_EXTERNAL_DTD"), "field-LOAD_EXTERNAL_DTD");
        check(fd.contains("ZEPHYR_PROPERTY_PREFIX"), "field-ZEPHYR_PROPERTY_PREFIX");
        check(fd.contains("IGNORE_EXTERNAL_DTD"), "field-IGNORE_EXTERNAL_DTD");
        check(fd.contains("DTD_ALLOW"), "field-DTD_ALLOW");
        check(fd.contains("DTD_IGNORE"), "field-DTD_IGNORE");
        check(fd.contains("DTD_DENY"), "field-DTD_DENY");
        check(md.size() == 25, "method-count");
        check(md.contains("clone"), "method-clone");
        check(md.contains("cloneAndUpdate"), "method-cloneAndUpdate");
        check(md.contains("convert"), "method-convert");
        check(md.contains("debugPrint"), "method-debugPrint");
        check(md.contains("find"), "method-find");
        check(md.contains("getEnumValue"), "method-getEnumValue");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("getJDKCatalogResolver"), "method-getJDKCatalogResolver");
        check(md.contains("getLimit"), "method-getLimit");
        check(md.contains("getLimitAsString"), "method-getLimitAsString");
        check(md.contains("getLimitValueAsString"), "method-getLimitValueAsString");
        check(md.contains("getLimitValueByIndex"), "method-getLimitValueByIndex");
        check(md.contains("getState"), "method-getState");
        check(md.contains("getStateLiteral"), "method-getStateLiteral");
        check(md.contains("is"), "method-is");
        check(md.contains("isNoLimit"), "method-isNoLimit");
        check(md.contains("isOverLimit"), "method-isOverLimit");
        check(md.contains("isSecureProcessing"), "method-isSecureProcessing");
        check(md.contains("isSecureProcessingSet"), "method-isSecureProcessingSet");
        check(md.contains("isSet"), "method-isSet");
        check(md.contains("printEntityCountInfo"), "method-printEntityCountInfo");
        check(md.contains("printWarning"), "method-printWarning");
        check(md.contains("readSystemProperties"), "method-readSystemProperties");
        check(md.contains("setLimit"), "method-setLimit");
        check(md.contains("setSecureProcessing"), "method-setSecureProcessing");
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
    static String fld(Class<?> c, String n) throws Exception { java.lang.reflect.Field f = c.getDeclaredField(n); f.setAccessible(true); return (String) f.get(null); }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager", false, XMLSecurityManagerTest.class.getClassLoader());
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, XMLSecurityManagerTest.class.getClassLoader());
        Class<?> stC = Class.forName("jdk.xml.internal.JdkProperty$State", false, XMLSecurityManagerTest.class.getClassLoader());
        Class<?> anC = Class.forName("jdk.xml.internal.XMLLimitAnalyzer", false, XMLSecurityManagerTest.class.getClassLoader());
        Object api = e(stC, "APIPROPERTY"), sys = e(stC, "SYSTEMPROPERTY"), fsp = e(stC, "FSP"), dflt = e(stC, "DEFAULT");
        Object EE = e(limC, "ENTITY_EXPANSION_LIMIT"), MO = e(limC, "MAX_OCCUR_NODE_LIMIT"),
             EA = e(limC, "ELEMENT_ATTRIBUTE_LIMIT"), DTD = e(limC, "DTD"),
             STX = e(limC, "STAX_SUPPORT_DTD"), JC = e(limC, "JDKCATALOG_RESOLVE");
        // constants
        check(fld(c, "DTD_KEY").equals("jdk.xml.dtd.support"), "v-DTD_KEY");
        check(fld(c, "DISALLOW_DTD").equals("http://apache.org/xml/features/disallow-doctype-decl"), "v-DISALLOW_DTD");
        check(fld(c, "LOAD_EXTERNAL_DTD").equals("http://apache.org/xml/features/nonvalidating/load-external-dtd"), "v-LOAD_EXTERNAL_DTD");
        check(fld(c, "ZEPHYR_PROPERTY_PREFIX").equals("http://java.sun.com/xml/stream/properties/"), "v-ZEPHYR_PREFIX");
        check(fld(c, "IGNORE_EXTERNAL_DTD").equals("http://java.sun.com/xml/stream/properties/ignore-external-dtd"), "v-IGNORE_EXTERNAL_DTD");
        check(fld(c, "DTD_ALLOW").equals("allow") && fld(c, "DTD_IGNORE").equals("ignore") && fld(c, "DTD_DENY").equals("deny"), "v-DTD-literals");
        // package-private value maps: exact contents, immutable
        java.lang.reflect.Field dtdMapF = c.getDeclaredField("DTD_MAP"); dtdMapF.setAccessible(true);
        @SuppressWarnings("unchecked") java.util.Map<String, Integer> dtdMap = (java.util.Map<String, Integer>) dtdMapF.get(null);
        check(dtdMap.size() == 3, "DTD_MAP-size-3");
        check(dtdMap.get("allow").equals(0) && dtdMap.get("ignore").equals(1) && dtdMap.get("deny").equals(2), "DTD_MAP-values");
        try { dtdMap.put("x", 9); check(false, "DTD_MAP-immutable"); }
        catch (UnsupportedOperationException uoe) { check(true, "DTD_MAP-immutable-UOE"); }
        java.lang.reflect.Field crMapF = c.getDeclaredField("CR_MAP"); crMapF.setAccessible(true);
        @SuppressWarnings("unchecked") java.util.Map<String, Integer> crMap = (java.util.Map<String, Integer>) crMapF.get(null);
        check(crMap.get("continue").equals(0) && crMap.get("ignore").equals(1) && crMap.get("strict").equals(2), "CR_MAP-values");
        try { crMap.put("x", 9); check(false, "CR_MAP-immutable"); }
        catch (UnsupportedOperationException uoe) { check(true, "CR_MAP-immutable-UOE"); }
        // default constructor: declared defaults, DEFAULT state, secure flags off
        Object m0 = c.getDeclaredConstructor().newInstance();
        check(!(Boolean) inv(m0, "isSecureProcessing"), "ctor-default-not-secure");
        check(!(Boolean) inv(m0, "isSecureProcessingSet"), "ctor-default-not-explicitly-set");
        check(inv(m0, "getLimit", EE).equals(2500), "def-EE-2500");
        check(inv(m0, "getLimit", MO).equals(5000), "def-MO-5000");
        check(inv(m0, "getLimit", EA).equals(200), "def-EA-200");
        check(inv(m0, "getLimitValueAsString", DTD).equals("allow"), "def-DTD-allow");
        check(inv(m0, "getLimitValueAsString", STX).equals("true"), "def-STAX-true");
        check(inv(m0, "getLimitValueAsString", JC).equals("continue"), "def-JDKCAT-continue");
        check(inv(m0, "getState", EE).toString().equals("DEFAULT"), "def-state-DEFAULT");
        check(inv(m0, "getStateLiteral", EE).equals("default"), "def-stateLiteral-default");
        check(!(Boolean) inv(m0, "isSet", EE), "def-not-set");
        // secure constructor: FSP state, secureValues, secure flag on but not "set explicitly"
        Object m1 = c.getConstructor(boolean.class).newInstance(Boolean.TRUE);
        check((Boolean) inv(m1, "isSecureProcessing"), "ctor-true-secure");
        check(!(Boolean) inv(m1, "isSecureProcessingSet"), "ctor-true-not-explicitly-set");
        check(inv(m1, "getState", EE).toString().equals("FSP"), "ctor-true-state-FSP");
        check(inv(m1, "getLimit", EE).equals(2500), "ctor-true-EE-2500");
        check(inv(m1, "getLimitValueAsString", DTD).equals("allow"), "ctor-true-DTD-allow");
        check(inv(m1, "getLimitValueAsString", STX).equals("true"), "ctor-true-STAX-true");
        // setSecureProcessing: flips values and the FSP state, and records the explicit-set flag
        Object m2 = c.getDeclaredConstructor().newInstance();
        java.lang.reflect.Method ssp = c.getMethod("setSecureProcessing", boolean.class);
        ssp.invoke(m2, true);
        check((Boolean) inv(m2, "isSecureProcessing") && (Boolean) inv(m2, "isSecureProcessingSet"), "ssp-true-flags");
        check(inv(m2, "getState", EE).toString().equals("FSP"), "ssp-true-state-FSP");
        check(inv(m2, "getLimit", EE).equals(2500), "ssp-true-EE-secureValue");
        ssp.invoke(m2, false);
        check(!(Boolean) inv(m2, "isSecureProcessing") && (Boolean) inv(m2, "isSecureProcessingSet"), "ssp-false-flags");
        check(inv(m2, "getLimit", EE).equals(2500), "ssp-false-EE-default-again");
        check(inv(m2, "getState", EE).toString().equals("FSP"), "ssp-false-state-stays-FSP");
        // an APIPROPERTY-set limit survives setSecureProcessing(true) (FSP is lower precedence)
        Object m3 = c.getDeclaredConstructor().newInstance();
        java.lang.reflect.Method slObj = c.getMethod("setLimit", limC, stC, Object.class);
        slObj.invoke(m3, EE, api, "123");
        ssp.invoke(m3, true);
        check(inv(m3, "getLimit", EE).equals(123), "api-beats-fsp-value");
        check(inv(m3, "getState", EE).toString().equals("APIPROPERTY"), "api-beats-fsp-state");
        // setLimit by name: current name resolves APIPROPERTY, URI name resolves LEGACY_APIPROPERTY
        java.lang.reflect.Method slStr = c.getMethod("setLimit", String.class, stC, Object.class);
        check((Boolean) slStr.invoke(m0, "jdk.xml.maxElementDepth", api, "8"), "setname-sp-true");
        check(inv(m0, "getLimit", e(limC, "MAX_ELEMENT_DEPTH_LIMIT")).equals(8), "setname-sp-applied");
        check(inv(m0, "getState", e(limC, "MAX_ELEMENT_DEPTH_LIMIT")).toString().equals("APIPROPERTY"), "setname-sp-state");
        check((Boolean) slStr.invoke(m0, "http://www.oracle.com/xml/jaxp/properties/maxOccurLimit", api, "9"), "setname-uri-true");
        check(inv(m0, "getLimit", MO).equals(9), "setname-uri-applied");
        check(inv(m0, "getState", MO).toString().equals("LEGACY_APIPROPERTY"), "setname-uri-state-legacy");
        check(!(Boolean) slStr.invoke(m0, "bogus.limit", api, "1"), "setname-bogus-false");
        // lower-state writes are blocked (DEFAULT cannot override an APIPROPERTY setting)
        Object m4 = c.getDeclaredConstructor().newInstance();
        slObj.invoke(m4, EE, api, "5");
        slObj.invoke(m4, EE, sys, "6");
        check(inv(m4, "getLimit", EE).equals(5), "sysp-lt-api-blocked");
        check((Boolean) inv(m4, "isSet", EE), "isSet-true-after-setLimit");
        // ENTITYCOUNT special property: index 10000, value kept as a string, "yes" semantics
        check((Boolean) slStr.invoke(m4, "jdk.xml.getEntityCountInfo", api, "yes"), "ec-set-by-qname");
        check((Boolean) inv(m4, "printEntityCountInfo"), "ec-print-true");
        check(inv(m4, "getIndex", "jdk.xml.getEntityCountInfo").equals(10000), "ec-index-10000");
        check(inv(m4, "getLimitValueByIndex", 10000).equals("yes"), "ec-value-by-index");
        check(inv(m4, "getLimitAsString", "jdk.xml.getEntityCountInfo").equals("yes"), "ec-getLimitAsString");
        check((Boolean) slStr.invoke(m4, "http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo", api, "yes"), "ec-set-by-legacy-uri");
        java.lang.reflect.Method slInt = c.getMethod("setLimit", int.class, stC, int.class);
        slInt.invoke(m4, 10000, api, 5);
        check((Boolean) inv(m4, "printEntityCountInfo"), "ec-int-set-means-yes");
        // find(): canonical names for limits, qName for the special property, null otherwise
        check(inv(m4, "find", "jdk.xml.entityExpansionLimit").equals("jdk.xml.entityExpansionLimit"), "find-sp");
        check(inv(m4, "find", "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit").equals("jdk.xml.entityExpansionLimit"), "find-uri-to-sp");
        check(inv(m4, "find", "entityExpansionLimit") == null, "find-plain-old-null");
        check(inv(m4, "find", "jdk.xml.getEntityCountInfo").equals("jdk.xml.getEntityCountInfo"), "find-entitycount");
        check(inv(m4, "find", "bogus") == null, "find-bogus-null");
        // getEnumValue / getIndex / getLimitAsString by name
        check(inv(m4, "getEnumValue", "jdk.xml.dtd.support") == DTD, "getEnumValue-sp");
        check(inv(m4, "getEnumValue", "http://apache.org/xml/features/disallow-doctype-decl") == e(limC, "XERCES_DISALLOW_DTD"), "getEnumValue-uri");
        check(inv(m4, "getEnumValue", "bogus") == null, "getEnumValue-bogus-null");
        check(inv(m4, "getIndex", "jdk.xml.dtd.support").equals(12), "getIndex-dtd-12");
        check(inv(m4, "getIndex", "bogus").equals(-1), "getIndex-bogus-m1");
        check(inv(m4, "getLimitAsString", "jdk.xml.maxOccurLimit").equals("5000"), "getLimitAsString-default");
        check(inv(m4, "getLimitAsString", "bogus") == null, "getLimitAsString-bogus-null");
        // is(Limit, int) / is(Limit)
        Object m5 = c.getDeclaredConstructor().newInstance();
        check((Boolean) inv(m5, "is", EE, 2500), "is-value-default");
        check(!(Boolean) inv(m5, "is", EE, 2499), "is-value-mismatch");
        check((Boolean) inv(m5, "is", STX), "is-boolean-1-true");
        check(!(Boolean) inv(m5, "is", e(limC, "XERCES_DISALLOW_DTD")), "is-boolean-0-false");
        check((Boolean) inv(m5, "isNoLimit", 0), "isNoLimit-0");
        check(!(Boolean) inv(m5, "isNoLimit", 1), "isNoLimit-1-false");
        // isOverLimit(size variant) records into the analyzer and reports only when exceeded
        Object an = anC.getDeclaredConstructor().newInstance();
        slInt.invoke(m5, 0, api, 10); // ENTITY_EXPANSION_LIMIT = 10
        check((Boolean) inv(m5, "isOverLimit", 0, "e1", 11, an), "over-size-11-true");
        check(inv(an, "getTotalValue", EE).equals(11), "analyzer-recorded-11");
        check(!(Boolean) inv(m5, "isOverLimit", 0, "e1", 10, an), "over-size-10-false-boundary");
        check(inv(an, "getTotalValue", EE).equals(11), "analyzer-not-recorded-at-boundary");
        slInt.invoke(m5, 2, api, 0); // ELEMENT_ATTRIBUTE_LIMIT = 0 means no limit
        check(!(Boolean) inv(m5, "isOverLimit", 2, "e", 9999, an), "over-nolimit-false");
        // isOverLimit(cumulative variant) compares against analyzer totals for accumulation limits
        inv(an, "addValue", EE, "cx", 3000);
        check((Boolean) inv(c.getDeclaredConstructor().newInstance(), "isOverLimit", 0, an), "over-cumulative-true");
        Object an2 = anC.getDeclaredConstructor().newInstance();
        inv(an2, "addValue", EE, "cx", 100);
        check(!(Boolean) inv(c.getDeclaredConstructor().newInstance(), "isOverLimit", 0, an2), "over-cumulative-false");
        // clone: deep copy of values/states/isSet
        Object m6 = c.getDeclaredConstructor().newInstance();
        slObj.invoke(m6, MO, api, "42");
        Object m6c = inv(m6, "clone");
        check(m6c != m6 && m6c.getClass().equals(c), "clone-distinct-same-type");
        slObj.invoke(m6c, MO, api, "43");
        check(inv(m6, "getLimit", MO).equals(42) && inv(m6c, "getLimit", MO).equals(43), "clone-independent");
        // cloneAndUpdate reads current system properties into the copy only (restored in finally)
        System.setProperty("jdk.xml.maxOccurLimit", "77");
        try {
            Object m7 = c.getDeclaredConstructor().newInstance();
            Object m7c = inv(m7, "cloneAndUpdate");
            check(inv(m7c, "getLimit", MO).equals(77), "cloneAndUpdate-picks-up-sysprop");
            check(inv(m7c, "getState", MO).toString().equals("SYSTEMPROPERTY"), "cloneAndUpdate-state-sysprop");
            check(inv(m7, "getLimit", MO).equals(5000), "cloneAndUpdate-original-untouched");
        } finally {
            System.clearProperty("jdk.xml.maxOccurLimit");
        }
        // readSystemProperties: current name wins, legacy plain name is the fallback, garbage throws NFE
        System.setProperty("jdk.xml.entityExpansionLimit", "9");
        System.setProperty("entityExpansionLimit", "7");
        try {
            Object m8 = c.getDeclaredConstructor().newInstance();
            inv(m8, "readSystemProperties");
            check(inv(m8, "getLimit", EE).equals(9), "sysprop-new-beats-old");
            check(inv(m8, "getState", EE).toString().equals("SYSTEMPROPERTY"), "sysprop-state");
            System.clearProperty("jdk.xml.entityExpansionLimit");
            Object m9 = c.getDeclaredConstructor().newInstance();
            inv(m9, "readSystemProperties");
            check(inv(m9, "getLimit", EE).equals(7), "sysprop-legacy-fallback");
            System.clearProperty("entityExpansionLimit");
            System.setProperty("jdk.xml.entityExpansionLimit", "abc");
            Object m10 = c.getDeclaredConstructor().newInstance();
            try { inv(m10, "readSystemProperties"); check(false, "sysprop-invalid"); }
            catch (java.lang.reflect.InvocationTargetException ite) {
                check(ite.getCause() instanceof NumberFormatException, "sysprop-invalid-NFE");
                check(ite.getCause().getMessage().equals("Invalid setting for system property: jdk.xml.entityExpansionLimit"), "sysprop-invalid-message");
            }
        } finally {
            System.clearProperty("jdk.xml.entityExpansionLimit");
            System.clearProperty("entityExpansionLimit");
        }
        // convert(): null value yields a secure default or the passed-in manager; an XSM value passes through
        Object conv1 = inv(c, "convert", (Object) null, (Object) null);
        check(conv1.getClass().equals(c), "convert-null-null-new-instance");
        check((Boolean) inv(conv1, "isSecureProcessing"), "convert-null-null-secure-true");
        Object existing = c.getDeclaredConstructor().newInstance();
        check(inv(c, "convert", (Object) null, existing) == existing, "convert-null-existing-same");
        check(inv(c, "convert", existing, (Object) null) == existing, "convert-xsm-value-same");
        // printWarning dedups per (parserClass, propertyName) pair and writes to stderr
        java.io.PrintStream oldErr = System.err;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(bos, true));
        try {
            inv(c, "printWarning", "C1", "p1", new org.xml.sax.SAXException("boom1"));
            inv(c, "printWarning", "C1", "p1", new org.xml.sax.SAXException("boom2"));
            inv(c, "printWarning", "C2", "p1", new org.xml.sax.SAXException("boom3"));
        } finally {
            System.setErr(oldErr);
        }
        String[] lines = bos.toString().trim().split("\n");
        check(lines.length == 2, "printWarning-dedup-2-lines");
        check(lines[0].equals("Warning: C1: boom1"), "printWarning-line1");
        check(lines[1].equals("Warning: C2: boom3"), "printWarning-line2");
        // getJDKCatalogResolver returns a live catalog resolver
        check(inv(m0, "getJDKCatalogResolver") instanceof javax.xml.catalog.CatalogResolver, "getJDKCatalogResolver-type");
        // debugPrint only prints when the entity-count flag is on (stdout captured, restored in finally)
        Object m11 = c.getDeclaredConstructor().newInstance();
        slStr.invoke(m11, "jdk.xml.getEntityCountInfo", api, "yes");
        java.io.PrintStream oldOut = System.out;
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(bos2, true));
        try {
            inv(m11, "debugPrint", an);
            inv(m0, "debugPrint", an);
        } finally {
            System.setOut(oldOut);
        }
        String dump = bos2.toString();
        check(dump.contains("ENTITY_EXPANSION_LIMIT"), "debugPrint-prints-when-flag-on");
        check(dump.contains("Property") && dump.contains("Entity Name"), "debugPrint-header");
        // real-XML linkage 1: factory attribute round trip through this manager's limit names
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute("jdk.xml.entityExpansionLimit", "1");
        check(dbf.getAttribute("jdk.xml.entityExpansionLimit").equals("1"), "factory-ee-readback");
        // real-XML linkage 2: the limit fires with its exact JAXP error code
        try {
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<!DOCTYPE r [<!ENTITY b \"x\">]><r>&b;&b;</r>".getBytes("UTF-8")));
            check(false, "live-ee-violates");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().startsWith("JAXP00010001: The parser has encountered more than \"1\" entity expansions"),
                    "live-ee-message");
        }
        // real-XML linkage 3: secure processing leaves accessExternalDTD at its default on this JDK
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf2.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        check(dbf2.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals("all"), "fsp-accDTD-stays-all");
    }
}
