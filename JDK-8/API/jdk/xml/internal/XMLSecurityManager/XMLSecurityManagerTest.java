/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager conformance: XML security manager (internal).
 * @run main XMLSecurityManagerTest
 */


public class XMLSecurityManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    static Object en(Class<?> c, String name) {
        return java.lang.Enum.valueOf((Class) c, name);
    }

    static void restore(String name, String prev) {
        if (prev == null) System.clearProperty(name);
        else System.setProperty(name, prev);
    }

    public static void main(String[] args) throws Exception {
        jdk.xml.internal.XMLSecurityManager m = new jdk.xml.internal.XMLSecurityManager();
        check(m != null, "constructed");
        m.setSecureProcessing(true);
        check(m.isSecureProcessing(), "secure processing");
        check(m.getLimit(jdk.xml.internal.XMLSecurityManager.Limit.ENTITY_EXPANSION_LIMIT) >= 0, "limit non-negative");

        // ---- reflection surface ----
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager");
        Class<?> stC = Class.forName("jdk.xml.internal.XMLSecurityManager$State");
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit");
        Object[] limits = limC.getEnumConstants();
        java.lang.reflect.Method getLimit = smC.getMethod("getLimit", limC);
        java.lang.reflect.Method getLimitValueAsString = smC.getMethod("getLimitValueAsString", limC);
        java.lang.reflect.Method getState = smC.getMethod("getState", limC);
        java.lang.reflect.Method getStateLiteral = smC.getMethod("getStateLiteral", limC);
        java.lang.reflect.Method isSecure = smC.getMethod("isSecureProcessing");

        // ---- default-arg constructor equals fsp=false ----
        Object defCtor = smC.getConstructor().newInstance();
        Object smF = smC.getConstructor(boolean.class).newInstance(false);
        Object smT = smC.getConstructor(boolean.class).newInstance(true);
        check(!((Boolean) isSecure.invoke(defCtor)).booleanValue(), "no-arg constructor -> secure processing off");
        check(!((Boolean) isSecure.invoke(smF)).booleanValue(), "fsp=false constructor");
        check(((Boolean) isSecure.invoke(smT)).booleanValue(), "fsp=true constructor");

        // ---- full limit-value matrix for both FSP states ----
        int[] expectDef = {0, 0, 0, 0, 0, 0, 0, 1000, 0, 10, 100, 10000};
        int[] expectSec = {64000, 5000, 10000, 50000000, 0, 1000000, 0, 1000, 3000000, 10, 100, 10000};
        java.lang.reflect.Method defaultOf = limC.getMethod("defaultValue");
        java.lang.reflect.Method secureOf = limC.getDeclaredMethod("secureValue");
        secureOf.setAccessible(true);
        for (int i = 0; i < limits.length; i++) {
            String n = ((java.lang.Enum<?>) limits[i]).name();
            check(((Integer) getLimit.invoke(smF, limits[i])).intValue() == expectDef[i], n + " default value " + expectDef[i]);
            check(((Integer) getLimit.invoke(smT, limits[i])).intValue() == expectSec[i], n + " secure value " + expectSec[i]);
            check(((Integer) getLimit.invoke(smF, limits[i])).intValue() == ((Integer) defaultOf.invoke(limits[i])).intValue(), n + " fsp=false == Limit.defaultValue()");
            check(((Integer) getLimit.invoke(smT, limits[i])).intValue() == ((Integer) secureOf.invoke(limits[i])).intValue(), n + " fsp=true == Limit.secureValue()");
            check(String.valueOf(expectDef[i]).equals(getLimitValueAsString.invoke(smF, limits[i])), n + " value-as-string");
            check(((java.lang.Enum<?>) getState.invoke(smF, limits[i])).name().equals("DEFAULT"), n + " state DEFAULT when fsp=false");
            check(((java.lang.Enum<?>) getState.invoke(smT, limits[i])).name().equals("FSP"), n + " state FSP when fsp=true");
            check("default".equals(getStateLiteral.invoke(smF, limits[i])), n + " literal default");
            check("FEATURE_SECURE_PROCESSING".equals(getStateLiteral.invoke(smT, limits[i])), n + " literal FSP");
        }

        // ---- setSecureProcessing toggles value and state on a live instance ----
        Object toggled = smC.getConstructor(boolean.class).newInstance(false);
        java.lang.reflect.Method setSecure = smC.getMethod("setSecureProcessing", boolean.class);
        Object eel = limits[0];
        setSecure.invoke(toggled, true);
        check(((Boolean) isSecure.invoke(toggled)).booleanValue(), "toggled secure on");
        check(((Integer) getLimit.invoke(toggled, eel)).intValue() == 64000, "toggled entity expansion limit to secure value");
        check(((java.lang.Enum<?>) getState.invoke(toggled, eel)).name().equals("FSP"), "toggled state FSP");
        setSecure.invoke(toggled, false);
        check(!((Boolean) isSecure.invoke(toggled)).booleanValue(), "toggled secure off");
        check(((Integer) getLimit.invoke(toggled, eel)).intValue() == 0, "toggled back to default value");

        // ---- setLimit(String, State, Object): managed/unmanaged names, parsing ----
        java.lang.reflect.Method setLimitS = smC.getMethod("setLimit", String.class, stC, Object.class);
        Object apiSt = en(stC, "APIPROPERTY");
        check(((Boolean) setLimitS.invoke(smF, "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", apiSt, "123")).booleanValue(), "setLimit by api name returns true");
        check(((Integer) getLimit.invoke(smF, eel)).intValue() == 123, "readback 123");
        check(!((Boolean) setLimitS.invoke(smF, "jck.bogus.limit", apiSt, "1")).booleanValue(), "setLimit unknown name returns false");
        setLimitS.invoke(smF, "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", apiSt, "-5");
        check(((Integer) getLimit.invoke(smF, eel)).intValue() == 0, "negative value clamped to 0 (no limit)");
        setLimitS.invoke(smF, "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", apiSt, "not-a-number");
        check(((Integer) getLimit.invoke(smF, eel)).intValue() == 0, "unparseable value -> 0");
        check("property".equals(getStateLiteral.invoke(smF, eel)), "state literal after api set");

        // ---- setLimit precedence: lower-precedence writes are ignored ----
        java.lang.reflect.Method setLimitI = smC.getMethod("setLimit", int.class, stC, int.class);
        Object fspSt = en(stC, "FSP");
        Object prec = smC.getConstructor(boolean.class).newInstance(false);
        setLimitI.invoke(prec, 0, apiSt, 7);
        setLimitI.invoke(prec, 0, fspSt, 999); // FSP < APIPROPERTY -> ignored
        check(((Integer) getLimit.invoke(prec, eel)).intValue() == 7, "FSP write below APIPROPERTY ignored");
        check(((Boolean) smC.getMethod("isSet", int.class).invoke(prec, 0)).booleanValue(), "isSet true after explicit set");
        check(!((Boolean) smC.getMethod("isSet", int.class).invoke(prec, 1)).booleanValue(), "isSet false for untouched limit");
        setLimitI.invoke(prec, 0, apiSt, 9);
        check(((Integer) getLimit.invoke(prec, eel)).intValue() == 9, "same-state write overwrites");

        // ---- setLimit(Limit, State, int) overload ----
        java.lang.reflect.Method setLimitL = smC.getMethod("setLimit", limC, stC, int.class);
        setLimitL.invoke(prec, eel, apiSt, 42);
        check(((Integer) getLimit.invoke(prec, eel)).intValue() == 42, "setLimit(Limit,State,int) readback");

        // ---- getIndex: api names, entity-count-info pseudo index, unknown, null ----
        java.lang.reflect.Method getIndex = smC.getMethod("getIndex", String.class);
        check(((Integer) getIndex.invoke(smF, "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit")).intValue() == 0, "getIndex entity expansion");
        check(((Integer) getIndex.invoke(smF, "http://www.oracle.com/xml/jaxp/properties/maxXMLNameLimit")).intValue() == 7, "getIndex maxXMLNameLimit");
        check(((Integer) getIndex.invoke(smF, "http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo")).intValue() == 10000, "getIndex entity count info pseudo index");
        check(((Integer) getIndex.invoke(smF, "jdk.xml.entityExpansionLimit")).intValue() == -1, "system property name is not an api index");
        check(((Integer) getIndex.invoke(smF, "bogus")).intValue() == -1, "getIndex unknown -1");
        try {
            getIndex.invoke(smF, (Object) null);
            check(false, "getIndex(null) must throw NPE");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NullPointerException, "getIndex(null) -> NullPointerException");
        }

        // ---- isNoLimit ----
        java.lang.reflect.Method isNoLimit = smC.getMethod("isNoLimit", int.class);
        check(((Boolean) isNoLimit.invoke(smF, 0)).booleanValue(), "0 means no limit");
        check(!((Boolean) isNoLimit.invoke(smF, 1)).booleanValue(), "non-zero is a limit");

        // ---- isOverLimit(int, String, int, analyzer): threshold, recording, no-limit ----
        Class<?> laC = Class.forName("jdk.xml.internal.XMLLimitAnalyzer");
        java.lang.reflect.Method over4 = smC.getMethod("isOverLimit", int.class, String.class, int.class, laC);
        Object thr = smC.getConstructor(boolean.class).newInstance(false);
        setLimitI.invoke(thr, 0, apiSt, 5);
        Object la = laC.getConstructor().newInstance();
        check(((Boolean) over4.invoke(thr, 0, "ent", 6, la)).booleanValue(), "size 6 over limit 5");
        check(((Integer) laC.getMethod("getTotalValue", int.class).invoke(la, 0)).intValue() == 6, "analyzer recorded the offending size");
        check(!((Boolean) over4.invoke(thr, 0, "ent", 5, la)).booleanValue(), "size 5 at limit 5 not over");
        Object noLim = smC.getConstructor(boolean.class).newInstance(false);
        check(!((Boolean) over4.invoke(noLim, 0, "ent", 100000, la)).booleanValue(), "no-limit (0) never over");

        // ---- isOverLimit(int, analyzer): cumulative vs per-name evaluation ----
        java.lang.reflect.Method over2 = smC.getMethod("isOverLimit", int.class, laC);
        java.lang.reflect.Method laAdd = laC.getMethod("addValue", limC, String.class, int.class);
        Object cum = laC.getConstructor().newInstance();
        laAdd.invoke(cum, eel, "a", 3);
        laAdd.invoke(cum, eel, "b", 4);
        check(((Boolean) over2.invoke(thr, 0, cum)).booleanValue(), "cumulative total 7 over limit 5");
        Object thr2 = smC.getConstructor(boolean.class).newInstance(false);
        setLimitI.invoke(thr2, 0, apiSt, 10);
        check(!((Boolean) over2.invoke(thr2, 0, cum)).booleanValue(), "cumulative total 7 within limit 10");
        Object genLa = laC.getConstructor().newInstance();
        Object genL = limits[4]; // GENERAL_ENTITY_SIZE_LIMIT: per-name max, not cumulative
        laAdd.invoke(genLa, genL, "g", 7);
        Object genThr = smC.getConstructor(boolean.class).newInstance(false);
        setLimitI.invoke(genThr, 4, apiSt, 5);
        check(((Boolean) over2.invoke(genThr, 4, genLa)).booleanValue(), "per-name max 7 over limit 5");

        // ---- printEntityCountInfo flag and debugPrint ----
        Object prn = smC.getConstructor(boolean.class).newInstance(false);
        check(!((Boolean) smC.getMethod("printEntityCountInfo").invoke(prn)).booleanValue(), "entity count info off by default");
        java.lang.reflect.Method setLimitO = smC.getMethod("setLimit", int.class, stC, Object.class);
        setLimitO.invoke(prn, 10000, apiSt, "yes");
        check(((Boolean) smC.getMethod("printEntityCountInfo").invoke(prn)).booleanValue(), "entity count info on after 'yes'");
        check("yes".equals(smC.getMethod("getLimitValueByIndex", int.class).invoke(prn, 10000)), "pseudo index value readback");
        smC.getMethod("debugPrint", laC).invoke(prn, laC.getConstructor().newInstance());
        check(true, "debugPrint no throw");
        Object prnOff = smC.getConstructor(boolean.class).newInstance(false);
        setLimitO.invoke(prnOff, 10000, apiSt, "no");
        check(!((Boolean) smC.getMethod("printEntityCountInfo").invoke(prnOff)).booleanValue(), "'no' keeps entity count info off");
        check(((Integer) getLimit.invoke(smF, eel)).intValue() >= 0, "limits unaffected by pseudo index writes");

        // ---- printWarning: static, deduplicated, never throws ----
        org.xml.sax.SAXException warn = new org.xml.sax.SAXNotRecognizedException("jck-unrecognized");
        smC.getMethod("printWarning", String.class, String.class, org.xml.sax.SAXException.class)
                .invoke(null, "JckParser", "jck.prop", warn);
        smC.getMethod("printWarning", String.class, String.class, org.xml.sax.SAXException.class)
                .invoke(null, "JckParser", "jck.prop", warn);
        check(true, "printWarning repeated call no throw");

        // ---- convert: null passthrough, instance passthrough, legacy type ----
        java.lang.reflect.Method convert = smC.getMethod("convert", Object.class, smC);
        Object fromNull = convert.invoke(null, null, null);
        check(((Boolean) isSecure.invoke(fromNull)).booleanValue(), "convert(null,null) -> secure manager");
        check(convert.invoke(null, null, smF) == smF, "convert(null,sm) returns sm");
        check(convert.invoke(null, smT, smF) == smT, "convert(sm,other) returns sm");
        check(convert.invoke(null, "not-a-manager", smF) == smF, "convert(unknown-type,sm) returns sm");

        // ---- system property linkage: new name, legacy name, invalid value ----
        String prev = System.setProperty("jdk.xml.entityExpansionLimit", "77");
        try {
            Object spm = smC.getConstructor(boolean.class).newInstance(false);
            check(((Integer) getLimit.invoke(spm, eel)).intValue() == 77, "sysprop jdk.xml.entityExpansionLimit=77 observed");
            check(((java.lang.Enum<?>) getState.invoke(spm, eel)).name().equals("SYSTEMPROPERTY"), "state SYSTEMPROPERTY");
            Object spmT = smC.getConstructor(boolean.class).newInstance(true);
            check(((Integer) getLimit.invoke(spmT, eel)).intValue() == 77, "sysprop overrides fsp-secure value");
        } finally {
            restore("jdk.xml.entityExpansionLimit", prev);
        }
        String prevOld = System.setProperty("entityExpansionLimit", "88");
        try {
            Object opm = smC.getConstructor(boolean.class).newInstance(false);
            check(((Integer) getLimit.invoke(opm, eel)).intValue() == 88, "legacy sysprop entityExpansionLimit=88 observed via NameMap");
        } finally {
            restore("entityExpansionLimit", prevOld);
        }
        Object restored = smC.getConstructor(boolean.class).newInstance(false);
        check(((Integer) getLimit.invoke(restored, eel)).intValue() == 0, "default restored after properties cleared");
        String prevBad = System.setProperty("jdk.xml.entityExpansionLimit", "not-a-number");
        try {
            smC.getConstructor(boolean.class).newInstance(false);
            check(false, "invalid numeric sysprop must fail construction");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NumberFormatException, "invalid numeric sysprop -> NumberFormatException");
            check(String.valueOf(ite.getCause().getMessage()).contains("jdk.xml.entityExpansionLimit"), "exception names the offending property");
        } finally {
            restore("jdk.xml.entityExpansionLimit", prevBad);
        }

        // ==== REAL PARSE LINKAGE 1: jdk.xml.entityExpansionLimit=2 rejects 3 expansions ====
        String threeEntities = "<!DOCTYPE root [<!ENTITY e1 \"A1\"><!ENTITY e2 \"A2\"><!ENTITY e3 \"A3\">]><root>&e1;&e2;&e3;</root>";
        String prevParse = System.setProperty("jdk.xml.entityExpansionLimit", "2");
        try {
            javax.xml.parsers.DocumentBuilder db =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
            try {
                db.parse(new org.xml.sax.InputSource(new java.io.StringReader(threeEntities)));
                check(false, "3 entity expansions must be rejected under limit 2");
            } catch (org.xml.sax.SAXParseException spe) {
                check(spe.getMessage() != null && spe.getMessage().contains("entity expansions"), "JAXP00010001 expansion-limit message");
                check(spe.getMessage().contains("\"2\""), "message cites the configured limit");
            }
        } finally {
            restore("jdk.xml.entityExpansionLimit", prevParse);
        }
        // after restore the same document parses fine
        org.w3c.dom.Document docOk = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(threeEntities)));
        check("root".equals(docOk.getDocumentElement().getNodeName()), "document parses after property restore");

        // ==== REAL PARSE LINKAGE 2: XXE / external DTD blocked by FSP and ACCESS_EXTERNAL_DTD ====
        String xxe = "<!DOCTYPE root [<!ENTITY xxe SYSTEM \"file:///nonexistent-xxe-jck\">]><root>&xxe;</root>";
        // FSP=true: external file access denied -> SAXParseException mentioning the restriction
        javax.xml.parsers.DocumentBuilderFactory dbfSec = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbfSec.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        check(dbfSec.getFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING), "dbf feature readback true");
        try {
            dbfSec.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(xxe)));
            check(false, "XXE under FSP must be rejected");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage() != null && spe.getMessage().contains("access is not allowed"), "FSP blocks external entity: " + spe.getMessage());
        }
        // FSP=false, no explicit property: protocol defaults to all -> the file is actually fetched -> missing file surfaces as FileNotFoundException
        try {
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new org.xml.sax.InputSource(new java.io.StringReader(xxe)));
            check(false, "nonexistent external entity must fail");
        } catch (java.io.FileNotFoundException fnfe) {
            check(true, "default (access all) attempts file read -> FileNotFoundException");
        }
        // ACCESS_EXTERNAL_DTD='' without FSP: same restriction message as FSP
        javax.xml.parsers.DocumentBuilderFactory dbfAed = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbfAed.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
        check("".equals(dbfAed.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD)), "accessExternalDTD readback empty");
        try {
            dbfAed.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(xxe)));
            check(false, "XXE with empty accessExternalDTD must be rejected");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage() != null && spe.getMessage().contains("accessExternalDTD"), "empty accessExternalDTD blocks external entity");
        }
        // clean document still parses under FSP
        javax.xml.parsers.DocumentBuilderFactory dbfClean = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbfClean.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        org.w3c.dom.Document clean = dbfClean.newDocumentBuilder().parse(
                new org.xml.sax.InputSource(new java.io.StringReader("<root><a>ok</a></root>")));
        check("root".equals(clean.getDocumentElement().getNodeName()) && clean.getDocumentElement().getFirstChild().getNodeName().equals("a"), "clean XML parses under FSP");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
