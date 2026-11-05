/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.Processor conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main ProcessorTest
 */
public class ProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Processor", false, ProcessorTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$Processor"), "name");
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
        check(ec.size() == 3, "enum-constant-count");
        check(ec.contains("ANY"), "enum-ANY");
        check(ec.contains("PARSER"), "enum-PARSER");
        check(ec.contains("XPATH"), "enum-XPATH");
        check(fd.size() == 0, "field-count");
        check(md.size() == 2, "method-count");
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
                    if (a[i] == null) continue;
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Processor", false, ProcessorTest.class.getClassLoader());
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, ProcessorTest.class.getClassLoader());
        Object[] vals = c.getEnumConstants();
        check(vals.length == 3, "values-length-3");
        check(vals[0].toString().equals("ANY") && ((Enum<?>) vals[0]).ordinal() == 0, "order-ANY");
        check(vals[1].toString().equals("PARSER") && ((Enum<?>) vals[1]).ordinal() == 1, "order-PARSER");
        check(vals[2].toString().equals("XPATH") && ((Enum<?>) vals[2]).ordinal() == 2, "order-XPATH");
        Object ANY = vals[0], PARSER = vals[1], XPATH = vals[2];
        // name/toString/valueOf round trips
        for (Object v : vals) {
            check(((Enum<?>) v).name().equals(v.toString()), "name-equals-toString-" + v);
            check(java.lang.Enum.valueOf((Class<Enum>) (Class) c, v.toString()) == v, "valueOf-" + v);
        }
        try { java.lang.Enum.valueOf((Class<Enum>) (Class) c, "BOGUS"); check(false, "valueOf-bogus"); }
        catch (IllegalArgumentException iae) { check(true, "valueOf-bogus-IAE"); }
        // natural ordering ANY < PARSER < XPATH
        check(((Comparable) ANY).compareTo(PARSER) < 0, "cmp-ANY-lt-PARSER");
        check(((Comparable) PARSER).compareTo(XPATH) < 0, "cmp-PARSER-lt-XPATH");
        check(((Comparable) XPATH).compareTo(ANY) > 0, "cmp-XPATH-gt-ANY");
        check(((Comparable) PARSER).compareTo(PARSER) == 0, "cmp-equal-zero");
        // routing: every Limit belongs to exactly one of PARSER/XPATH and never to ANY
        int parserCount = 0, xpathCount = 0;
        for (Object l : limC.getEnumConstants()) {
            boolean p = (Boolean) inv(l, "isSupported", PARSER);
            boolean x = (Boolean) inv(l, "isSupported", XPATH);
            check(p ^ x, "exactly-one-processor-" + ((Enum<?>) l).name());
            check(!(Boolean) inv(l, "isSupported", ANY), "never-ANY-" + ((Enum<?>) l).name());
            if (p) parserCount++; else xpathCount++;
        }
        check(parserCount == 13, "parser-limits-13");
        check(xpathCount == 3, "xpath-limits-3");
        // the XPATH trio carries the XPath-specific names
        check(e(limC, "XPATH_GROUP_LIMIT").toString().equals("XPATH_GROUP_LIMIT")
                && (Boolean) inv(e(limC, "XPATH_GROUP_LIMIT"), "isSupported", XPATH), "xpath-group-routed");
        check((Boolean) inv(e(limC, "XPATH_OP_LIMIT"), "isSupported", XPATH), "xpath-op-routed");
        check((Boolean) inv(e(limC, "XPATH_TOTALOP_LIMIT"), "isSupported", XPATH), "xpath-totalop-routed");
        check((Boolean) inv(e(limC, "ENTITY_EXPANSION_LIMIT"), "isSupported", PARSER), "ee-routed-parser");
        // real linkage 1: PARSER-routed limits are live factory attributes
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute("jdk.xml.maxOccurLimit", 12);
        check(dbf.getAttribute("jdk.xml.maxOccurLimit").equals("12"), "factory-maxoccur-readback");
        // real linkage 2: XPATH-routed limits govern a real XPath evaluation under defaults
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        check(xp.evaluate("count(//*)", new org.xml.sax.InputSource(new java.io.StringReader("<r><a/><b/></r>"))).equals("3"),
                "xpath-count-eval");
        check(xp.evaluate("1+1", new org.xml.sax.InputSource(new java.io.StringReader("<r/>"))).equals("2"),
                "xpath-arith-eval");
        // real linkage 3: a PARSER limit set on one factory does not leak into a fresh factory
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        check(dbf2.getAttribute("jdk.xml.maxOccurLimit").equals("5000"), "factory2-maxoccur-default-5000");
        // real linkage 4: the DTD property routes to the parser and flips parse behavior live
        dbf.setAttribute("jdk.xml.dtd.support", "deny");
        try {
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream("<!DOCTYPE r><r/>".getBytes("UTF-8")));
            check(false, "deny-does-not-violate");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().startsWith("JAXP00010008: DOCTYPE is disallowed"), "deny-message");
        }
    }
}
