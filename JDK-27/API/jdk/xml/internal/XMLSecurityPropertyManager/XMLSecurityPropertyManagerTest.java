/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityPropertyManager conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main XMLSecurityPropertyManagerTest
 */
public class XMLSecurityPropertyManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：外部 DTD 限制错误消息随默认 Locale 本地化（如 zh_CN 为中文），断言的是英文消息
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager", false, XMLSecurityPropertyManagerTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityPropertyManager"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.xml.internal.FeaturePropertyBase"), "superclass");
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
        check(fd.size() == 0, "field-count");
        check(md.size() == 7, "method-count");
        check(md.contains("clone"), "method-clone");
        check(md.contains("cloneAndUpdate"), "method-cloneAndUpdate");
        check(md.contains("find"), "method-find");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("readSystemProperties"), "method-readSystemProperties");
        check(md.contains("setValue"), "method-setValue");
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager", false, XMLSecurityPropertyManagerTest.class.getClassLoader());
        Class<?> prC = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager$Property", false, XMLSecurityPropertyManagerTest.class.getClassLoader());
        Class<?> stC = Class.forName("jdk.xml.internal.FeaturePropertyBase$State", false, XMLSecurityPropertyManagerTest.class.getClassLoader());
        Object FSP = e(stC, "FSP"), JDP = e(stC, "JAXPDOTPROPERTIES"), SYSP = e(stC, "SYSTEMPROPERTY"), API = e(stC, "APIPROPERTY");
        Object pDTD = e(prC, "ACCESS_EXTERNAL_DTD"), pSCH = e(prC, "ACCESS_EXTERNAL_SCHEMA"), pSTY = e(prC, "ACCESS_EXTERNAL_STYLESHEET");
        // default constructor seeds all three values with "all"
        Object pm = c.getDeclaredConstructor().newInstance();
        check(inv(pm, "getValue", pDTD).equals("all"), "def-DTD-all");
        check(inv(pm, "getValue", pSCH).equals("all"), "def-SCH-all");
        check(inv(pm, "getValue", pSTY).equals("all"), "def-STY-all");
        check(inv(pm, "getValueByIndex", 0).equals("all"), "getValueByIndex-0");
        // setValue(Property, State, String) with the full precedence chain DEFAULT < FSP < JAXPDOTPROPERTIES < SYSTEMPROPERTY < APIPROPERTY
        inv(pm, "setValue", pDTD, FSP, "");
        check(inv(pm, "getValue", pDTD).equals(""), "fsp-set-empty");
        inv(pm, "setValue", pDTD, JDP, "file"); // higher state overrides FSP
        check(inv(pm, "getValue", pDTD).equals("file"), "jdp-overrides-fsp");
        inv(pm, "setValue", pDTD, SYSP, "http");
        check(inv(pm, "getValue", pDTD).equals("http"), "sysp-overrides-jdp");
        inv(pm, "setValue", pDTD, API, "jar");
        check(inv(pm, "getValue", pDTD).equals("jar"), "api-overrides-sysp");
        inv(pm, "setValue", pDTD, SYSP, "file"); // lower state is blocked once APIPROPERTY is recorded
        check(inv(pm, "getValue", pDTD).equals("jar"), "sysp-blocked-below-api");
        inv(pm, "setValue", pDTD, API, "jar2"); // same state still overwrites
        check(inv(pm, "getValue", pDTD).equals("jar2"), "same-state-overwrites");
        // setValue by name: URI name and system property name both work; boolean overload yields literals
        check((Boolean) inv(pm, "setValue", javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, API, "file"), "setname-uri");
        check(inv(pm, "getValue", pSCH).equals("file"), "setname-uri-applied");
        check((Boolean) inv(pm, "setValue", "javax.xml.accessExternalSchema", API, "http"), "setname-sp");
        check(inv(pm, "getValue", pSCH).equals("http"), "setname-sp-applied");
        check(!(Boolean) inv(pm, "setValue", "bogus.name", API, "x"), "setname-bogus-false");
        java.lang.reflect.Method svBool = c.getMethod("setValue", String.class, stC, boolean.class);
        check((Boolean) svBool.invoke(pm, javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET, API, true), "setbool-true");
        check(inv(pm, "getValue", pSTY).equals("true"), "setbool-true-literal");
        svBool.invoke(pm, javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET, API, false);
        check(inv(pm, "getValue", pSTY).equals("false"), "setbool-false-literal");
        // setValue(int, State, String) index form
        java.lang.reflect.Method svInt = c.getMethod("setValue", int.class, stC, String.class);
        svInt.invoke(pm, 1, API, "none");
        check(inv(pm, "getValueByIndex", 1).equals("none"), "setint-1-none");
        // getValue(String) resolves both name forms; getValueAsString mirrors it
        check(inv(pm, "getValue", (Object) javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals("jar2"), "getValue-by-uri");
        check(inv(pm, "getValue", (Object) "javax.xml.accessExternalDTD").equals("jar2"), "getValue-by-sp");
        check(inv(pm, "getValue", (Object) "bogus") == null, "getValue-bogus-null");
        check(inv(pm, "getValueAsString", "javax.xml.accessExternalDTD").equals("jar2"), "getValueAsString-sp");
        check(inv(pm, "getValueAsString", "bogus") == null, "getValueAsString-bogus-null");
        // find() canonicalizes both name forms to the URI property name
        check(inv(pm, "find", javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD), "find-uri");
        check(inv(pm, "find", "javax.xml.accessExternalSchema").equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA), "find-sp");
        check(inv(pm, "find", "bogus") == null, "find-bogus-null");
        // getIndex matrix incl. the generic Class,String overload from the base
        check(inv(pm, "getIndex", javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals(0), "idx-dtd-0");
        check(inv(pm, "getIndex", "javax.xml.accessExternalSchema").equals(1), "idx-sch-1");
        check(inv(pm, "getIndex", "javax.xml.accessExternalStylesheet").equals(2), "idx-sty-2");
        check(inv(pm, "getIndex", "bogus").equals(-1), "idx-bogus-m1");
        check(inv(pm, "getIndex", prC, "ACCESS_EXTERNAL_DTD").equals(0), "idx-generic-0");
        check(inv(pm, "getIndex", prC, "NOPE").equals(-1), "idx-generic-m1");
        // clone: same concrete type, values independent
        Object pa = c.getDeclaredConstructor().newInstance();
        Object pb = inv(pa, "clone");
        check(pb != pa && pb.getClass().equals(c), "clone-distinct-type");
        inv(pa, "setValue", pDTD, API, "x");
        check(inv(pb, "getValue", pDTD).equals("all"), "clone-independent");
        // cloneAndUpdate: picks up the live system property into the copy only (restored in finally)
        System.setProperty("javax.xml.accessExternalDTD", "file");
        try {
            Object pc = c.getDeclaredConstructor().newInstance();
            Object pcc = inv(pc, "cloneAndUpdate");
            check(inv(pcc, "getValue", pDTD).equals("file"), "cloneAndUpdate-picks-up-sysprop");
            check(inv(pc, "getValue", pDTD).equals("all"), "cloneAndUpdate-original-untouched");
            // readSystemProperties: an FSP-set value is upgraded to the system property, an APIPROPERTY-set one is not
            Object pd = c.getDeclaredConstructor().newInstance();
            inv(pd, "setValue", pDTD, FSP, "");
            inv(pd, "readSystemProperties");
            check(inv(pd, "getValue", pDTD).equals("file"), "readsysp-overrides-fsp");
            Object pe = c.getDeclaredConstructor().newInstance();
            inv(pe, "setValue", pDTD, API, "");
            inv(pe, "readSystemProperties");
            check(inv(pe, "getValue", pDTD).equals(""), "readsysp-cannot-override-api");
        } finally {
            System.clearProperty("javax.xml.accessExternalDTD");
        }
        // real-XML linkage 1: factory attribute round trip for all three properties
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        check(dbf.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals("file"), "factory-dtd-file");
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        tf.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "http");
        check(tf.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET).equals("http"), "tf-sty-http");
        // real-XML linkage 2: "" restriction makes an external DTD fail with the exact bundle message
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf2.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
        try {
            dbf2.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<?xml version='1.0'?><!DOCTYPE r SYSTEM 'nonexistent-dxml27.dtd'><r/>".getBytes("UTF-8")));
            check(false, "restricted-parse-violates");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().equals("External DTD: Failed to read external DTD 'nonexistent-dxml27.dtd', "
                    + "because 'file' access is not allowed due to restriction set by the accessExternalDTD property."),
                    "restricted-parse-message");
        }
        // real-XML linkage 3: with the default "all" the same document instead fails on the missing file itself
        javax.xml.parsers.DocumentBuilderFactory dbf3 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        try {
            dbf3.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<?xml version='1.0'?><!DOCTYPE r SYSTEM 'nonexistent-dxml27.dtd'><r/>".getBytes("UTF-8")));
            check(false, "all-parse-fails-anyway");
        } catch (Throwable t) {
            check(!(t instanceof org.xml.sax.SAXParseException)
                    || !t.getMessage().contains("access is not allowed"), "all-parse-different-failure");
        }
    }
}
