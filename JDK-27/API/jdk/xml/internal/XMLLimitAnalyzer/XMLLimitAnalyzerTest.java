/*
 * @test
 * @summary jdk.xml.internal.XMLLimitAnalyzer conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main XMLLimitAnalyzerTest
 */
public class XMLLimitAnalyzerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLLimitAnalyzer", false, XMLLimitAnalyzerTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLLimitAnalyzer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(fd.size() == 0, "field-count");
        check(md.size() == 9, "method-count");
        check(md.contains("addValue"), "method-addValue");
        check(md.contains("debugPrint"), "method-debugPrint");
        check(md.contains("endEntity"), "method-endEntity");
        check(md.contains("getTotalValue"), "method-getTotalValue");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("getValueByIndex"), "method-getValueByIndex");
        check(md.contains("isTracking"), "method-isTracking");
        check(md.contains("reset"), "method-reset");
        check(md.contains("startEntity"), "method-startEntity");
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
                    if (p.isPrimitive()) { if (a[i].getClass() != Integer.class) { ok = false; break; } }
                    else if (!p.isAssignableFrom(a[i].getClass())) { ok = false; break; }
                }
                if (ok) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }
    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLLimitAnalyzer", false, XMLLimitAnalyzerTest.class.getClassLoader());
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, XMLLimitAnalyzerTest.class.getClassLoader());
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager", false, XMLLimitAnalyzerTest.class.getClassLoader());
        Object EE = e(limC, "ENTITY_EXPANSION_LIMIT"), MO = e(limC, "MAX_OCCUR_NODE_LIMIT"),
             EA = e(limC, "ELEMENT_ATTRIBUTE_LIMIT"), TES = e(limC, "TOTAL_ENTITY_SIZE_LIMIT"),
             GES = e(limC, "GENERAL_ENTITY_SIZE_LIMIT"), PES = e(limC, "PARAMETER_ENTITY_SIZE_LIMIT"),
             MED = e(limC, "MAX_ELEMENT_DEPTH_LIMIT"), ERL = e(limC, "ENTITY_REPLACEMENT_LIMIT"),
             XPG = e(limC, "XPATH_GROUP_LIMIT");
        Object an = c.getDeclaredConstructor().newInstance();
        // accumulation family: ENTITY_EXPANSION/MAX_OCCUR/ELEMENT_ATTRIBUTE/TOTAL_ENTITY_SIZE/ENTITY_REPLACEMENT
        // only accumulate into totalValue; the running max stays untouched
        inv(an, "addValue", EE, "e1", 5);
        inv(an, "addValue", EE, "e2", 3);
        check(inv(an, "getValue", EE).equals(0), "acc-max-stays-0");
        check(inv(an, "getTotalValue", EE).equals(8), "acc-total-8");
        check(inv(an, "getValueByIndex", 0).equals(0), "acc-byIndex-0");
        inv(an, "addValue", MO, "m1", 4);
        inv(an, "addValue", EA, "a1", 6);
        check(inv(an, "getTotalValue", MO).equals(4), "acc-MO-4");
        check(inv(an, "getTotalValue", EA).equals(6), "acc-EA-6");
        // set-value family: MAX_ELEMENT_DEPTH and MAX_NAME overwrite both value and total
        inv(an, "addValue", MED, "x", 7);
        check(inv(an, "getValue", MED).equals(7), "set-value-7");
        check(inv(an, "getTotalValue", MED).equals(7), "set-total-7");
        inv(an, "addValue", MED, "x", 2);
        check(inv(an, "getValue", MED).equals(2), "set-value-overwritten");
        check(inv(an, "getTotalValue", MED).equals(2), "set-total-overwritten");
        // cache family: per-entity accumulation with a running max (general entities)
        inv(an, "addValue", GES, "g1", 10);
        inv(an, "addValue", GES, "g1", 5); // g1 accumulates to 15
        inv(an, "addValue", GES, "g2", 3);
        check(inv(an, "getValue", GES).equals(15), "cache-max-15");
        check(inv(an, "getTotalValue", GES).equals(0), "cache-own-total-0");
        // general and parameter entity sizes feed the TOTAL_ENTITY_SIZE accumulator
        check(inv(an, "getTotalValue", TES).equals(18), "TES-fed-by-GES");
        inv(an, "addValue", PES, "p1", 2);
        check(inv(an, "getTotalValue", TES).equals(20), "TES-fed-by-PES");
        // ENTITY_REPLACEMENT_LIMIT reads back the accumulated total from getValue
        inv(an, "addValue", ERL, "r1", 4);
        check(inv(an, "getValue", ERL).equals(4), "ERL-getValue-is-total");
        check(inv(an, "getTotalValue", ERL).equals(4), "ERL-total-4");
        // the XPath group limit uses the cache family as well
        inv(an, "addValue", XPG, "grp", 6);
        check(inv(an, "getValue", XPG).equals(6), "XPG-cache-6");
        // int-index forms mirror the enum forms
        check(inv(an, "getValue", 0).equals(0), "getValue-int-0");
        check(inv(an, "getTotalValue", 0).equals(8), "getTotalValue-int-8");
        check(inv(an, "getValue", 8).equals(4), "getValue-int-ERL-total");
        // entity tracking: startEntity arms a single name, endEntity disarms and evicts the cache entry
        check(!(Boolean) inv(an, "isTracking", "anything"), "tracking-unarmed");
        inv(an, "startEntity", "g1");
        check((Boolean) inv(an, "isTracking", "g1"), "tracking-armed-g1");
        check(!(Boolean) inv(an, "isTracking", "g2"), "tracking-other-false");
        inv(an, "endEntity", GES, "g1");
        check(!(Boolean) inv(an, "isTracking", "g1"), "tracking-cleared-after-end");
        // after eviction, re-adding the same name starts from scratch (no stale accumulation)
        inv(an, "addValue", GES, "g1", 7);
        check(inv(an, "getValue", GES).equals(15), "max-kept-after-eviction");
        inv(an, "addValue", GES, "g1", 20);
        check(inv(an, "getValue", GES).equals(27), "fresh-accumulation-after-eviction");
        // reset: TOTAL_ENTITY_SIZE_LIMIT zeroes its total, GENERAL_ENTITY_SIZE_LIMIT clears everything
        inv(an, "reset", TES);
        check(inv(an, "getTotalValue", TES).equals(0), "reset-TES-0");
        inv(an, "reset", GES);
        check(inv(an, "getValue", GES).equals(0), "reset-GES-value-0");
        inv(an, "addValue", GES, "g3", 1);
        check(inv(an, "getValue", GES).equals(1), "GES-fresh-after-reset");
        check(inv(an, "getTotalValue", TES).equals(1), "TES-restarts-from-GES");
        // debugPrint dumps one line per limit when enabled (stdout captured and restored)
        java.io.PrintStream oldOut = System.out;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(bos, true));
        try {
            inv(an, "debugPrint", smC.getDeclaredConstructor().newInstance());
        } finally {
            System.setOut(oldOut);
        }
        String dump = bos.toString();
        check(dump.contains("ENTITY_EXPANSION_LIMIT"), "debugPrint-limit-row");
        check(dump.contains("XPATH_TOTALOP_LIMIT"), "debugPrint-last-limit-row");
        check(dump.contains("JDKCATALOG_RESOLVE"), "debugPrint-final-limit-row");
        check(dump.trim().split("\n").length == 17, "debugPrint-17-lines");
        // real-XML linkage: the analyzer feeds the entity-expansion error path of a real parse
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute("jdk.xml.entityExpansionLimit", "1");
        try {
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<!DOCTYPE r [<!ENTITY b \"x\">]><r>&b;&b;</r>".getBytes("UTF-8")));
            check(false, "live-expansion-violates");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().startsWith("JAXP00010001: The parser has encountered more than \"1\" entity expansions"),
                    "live-expansion-message");
        }
        // with a generous limit the same document parses cleanly end to end
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf2.setAttribute("jdk.xml.entityExpansionLimit", "100");
        org.w3c.dom.Document doc = dbf2.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                "<!DOCTYPE r [<!ENTITY b \"x\">]><r>&b;&b;</r>".getBytes("UTF-8")));
        check(doc.getDocumentElement().getTagName().equals("r"), "live-parse-docel");
        check(doc.getDocumentElement().getTextContent().equals("xx"), "live-parse-entity-text");
    }
}
