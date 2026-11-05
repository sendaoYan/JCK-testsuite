/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.StringMapper conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main StringMapperTest
 */
public class StringMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$StringMapper", false, StringMapperTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$StringMapper"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.xml.internal.XMLSecurityManager$ValueMapper"), "superclass");
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
        check(md.size() == 3, "method-count");
        check(md.contains("toInt"), "method-toInt");
        check(md.contains("toObject"), "method-toObject");
        check(md.contains("toString"), "method-toString");
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$StringMapper", true, StringMapperTest.class.getClassLoader());
        Class<?> base = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper", false, StringMapperTest.class.getClassLoader());
        // standalone mapper over a custom map
        java.util.Map<String, Integer> mp = new java.util.HashMap<>();
        mp.put("aa", 0); mp.put("bb", 5);
        Object sm1 = c.getConstructor(java.util.Map.class).newInstance(mp);
        check(base.isInstance(sm1), "instance-is-ValueMapper");
        check(inv(sm1, "toInt", "aa").equals(0), "toInt-map-hit-0");
        check(inv(sm1, "toInt", "bb").equals(5), "toInt-map-hit-5");
        check(inv(sm1, "toInt", "AA").equals(0), "toInt-map-lowercased");
        check(inv(sm1, "toInt", "BB").equals(5), "toInt-map-lowercased-5");
        check(inv(sm1, "toInt", "zz").equals(0), "toInt-map-miss-is-0");
        check(inv(sm1, "toObject", 0).equals("aa"), "toObject-map-0");
        check(inv(sm1, "toObject", 5).equals("bb"), "toObject-map-5");
        check(inv(sm1, "toObject", 9) == null, "toObject-map-unmapped-null");
        check(inv(sm1, "toString", 5).equals("bb"), "toString-delegates-toObject");
        try { inv(sm1, "toInt", (Object) null); check(false, "toInt-null"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof NullPointerException, "toInt-null-NPE"); }
        // mapper without a map: numeric strings through Double.parseDouble (fraction truncated)
        Object smn = c.getConstructor(java.util.Map.class).newInstance((Object) null);
        check(inv(smn, "toInt", "42").equals(42), "toInt-nomap-42");
        check(inv(smn, "toInt", "3.7").equals(3), "toInt-nomap-3.7-truncates");
        check(inv(smn, "toInt", "99.999").equals(99), "toInt-nomap-99.999-truncates");
        check(inv(smn, "toInt", "-5").equals(-5), "toInt-nomap-negative");
        try { inv(smn, "toInt", "abc"); check(false, "toInt-nomap-abc"); }
        catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NumberFormatException, "toInt-nomap-abc-NFE");
            check(ite.getCause().getMessage().equals("Invalid setting abc for a property of Integer type."), "toInt-nomap-NFE-message");
        }
        check(inv(smn, "toObject", 7).equals("7"), "toObject-nomap-7");
        check(inv(smn, "toString", 9).equals("9"), "toString-nomap-9");
        check(inv(smn, "toObject", 0).equals("0"), "toObject-nomap-0");
        // real linkage: this mapper with DTD_MAP backs the jdk.xml.dtd.support limit
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager", false, StringMapperTest.class.getClassLoader());
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, StringMapperTest.class.getClassLoader());
        Class<?> stC = Class.forName("jdk.xml.internal.JdkProperty$State", false, StringMapperTest.class.getClassLoader());
        Object api = e(stC, "APIPROPERTY");
        java.lang.reflect.Field dtdMapF = smC.getDeclaredField("DTD_MAP"); dtdMapF.setAccessible(true);
        @SuppressWarnings("unchecked") java.util.Map<String, Integer> dtdMap = (java.util.Map<String, Integer>) dtdMapF.get(null);
        check(dtdMap.get("allow").equals(0) && dtdMap.get("ignore").equals(1) && dtdMap.get("deny").equals(2), "DTD_MAP-values");
        Object dtd = e(limC, "DTD"), jcat = e(limC, "JDKCATALOG_RESOLVE");
        check(inv(dtd, "mapper").getClass().getName().endsWith("StringMapper"), "dtd-limit-uses-StringMapper");
        check(inv(jcat, "mapper").getClass().getName().endsWith("StringMapper"), "jdkcatalog-limit-uses-StringMapper");
        Object m = smC.getDeclaredConstructor().newInstance();
        check(inv(m, "getLimitValueAsString", dtd).equals("allow"), "dtd-default-allow");
        check(inv(m, "getLimit", dtd).equals(0), "dtd-default-0");
        java.lang.reflect.Method slStr = smC.getMethod("setLimit", String.class, stC, Object.class);
        check((Boolean) slStr.invoke(m, "jdk.xml.dtd.support", api, "deny"), "dtd-set-deny");
        check(inv(m, "getLimitValueAsString", dtd).equals("deny"), "dtd-deny-readback");
        check(inv(m, "getLimit", dtd).equals(2), "dtd-deny-2");
        check((Boolean) slStr.invoke(m, "jdk.xml.dtd.support", api, "ignore"), "dtd-set-ignore");
        check(inv(m, "getLimitValueAsString", dtd).equals("ignore") && inv(m, "getLimit", dtd).equals(1), "dtd-ignore-readback");
        check((Boolean) slStr.invoke(m, "jdk.xml.dtd.support", api, "ALLOW"), "dtd-set-mixed-case");
        check(inv(m, "getLimitValueAsString", dtd).equals("allow"), "dtd-mixed-case-lowered");
        check((Boolean) slStr.invoke(m, "jdk.xml.dtd.support", api, "bogus"), "dtd-set-bogus-true");
        check(inv(m, "getLimitValueAsString", dtd).equals("allow"), "dtd-bogus-maps-0-allow");
        check(inv(m, "getLimitValueAsString", jcat).equals("continue"), "jdkcatalog-default-continue");
        check((Boolean) slStr.invoke(m, "jdk.xml.jdkcatalog.resolve", api, "strict"), "jdkcatalog-set-strict");
        check(inv(m, "getLimitValueAsString", jcat).equals("strict") && inv(m, "getLimit", jcat).equals(2), "jdkcatalog-strict-readback");
        // live parse loop: dtd.support=deny turns any DOCTYPE into JAXP00010008
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute("jdk.xml.dtd.support", "deny");
        try {
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<!DOCTYPE r><r/>".getBytes("UTF-8")));
            check(false, "dtd-deny-violates");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().startsWith("JAXP00010008: DOCTYPE is disallowed when the DTD property is set to deny."),
                    "dtd-deny-message");
        }
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf2.setAttribute("jdk.xml.dtd.support", "allow");
        dbf2.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                "<!DOCTYPE r><r/>".getBytes("UTF-8")));
        check(true, "dtd-allow-parse-ok");
    }
}
