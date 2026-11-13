/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.IntegerMapper conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main IntegerMapperTest
 */
public class IntegerMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：JAXP 限额错误消息随默认 Locale 本地化（如 zh_CN 为中文），断言的是英文消息
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$IntegerMapper", false, IntegerMapperTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$IntegerMapper"), "name");
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$IntegerMapper", true, IntegerMapperTest.class.getClassLoader());
        Class<?> base = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper", false, IntegerMapperTest.class.getClassLoader());
        Object im = c.getDeclaredConstructor().newInstance();
        check(base.isInstance(im), "instance-is-ValueMapper");
        // toInt: Integer input passes through unparsed
        check(inv(im, "toInt", Integer.valueOf(7)).equals(7), "toInt-Integer-7");
        check(inv(im, "toInt", Integer.valueOf(0)).equals(0), "toInt-Integer-0");
        check(inv(im, "toInt", Integer.valueOf(-3)).equals(-3), "toInt-Integer-negative");
        // toInt: String input parsed with Integer.parseInt
        check(inv(im, "toInt", "42").equals(42), "toInt-str-42");
        check(inv(im, "toInt", "-3").equals(-3), "toInt-str-negative");
        check(inv(im, "toInt", "0").equals(0), "toInt-str-0");
        try { inv(im, "toInt", " 7"); check(false, "toInt-str-ws"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof NumberFormatException, "toInt-str-ws-NFE"); }
        try { inv(im, "toInt", "3.5"); check(false, "toInt-str-3.5"); }
        catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NumberFormatException, "toInt-str-3.5-NFE");
            check(ite.getCause().getMessage().equals("Invalid setting 3.5 for a property of Integer type."), "toInt-NFE-message");
        }
        try { inv(im, "toInt", "abc"); check(false, "toInt-str-abc"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof NumberFormatException, "toInt-str-abc-NFE"); }
        try { inv(im, "toInt", (Object) null); check(false, "toInt-null"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof NullPointerException, "toInt-null-NPE"); }
        // toObject / toString
        check(inv(im, "toObject", 42).equals(42), "toObject-42");
        check(inv(im, "toObject", 42) instanceof Integer, "toObject-type-Integer");
        check(inv(im, "toObject", -1).equals(-1), "toObject-negative");
        check(inv(im, "toString", 42).equals("42"), "toString-42");
        check(inv(im, "toString", 0).equals("0"), "toString-0");
        check(inv(im, "toString", -7).equals("-7"), "toString-negative");
        // round trips
        check(inv(im, "toInt", inv(im, "toObject", 9)).equals(9), "roundtrip-9");
        check(inv(im, "toString", inv(im, "toInt", "123")).equals("123"), "roundtrip-str-123");
        // real linkage: the mapper backs all numeric limits; a String attribute converts through it
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager", false, IntegerMapperTest.class.getClassLoader());
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, IntegerMapperTest.class.getClassLoader());
        Class<?> stC = Class.forName("jdk.xml.internal.JdkProperty$State", false, IntegerMapperTest.class.getClassLoader());
        Object api = e(stC, "APIPROPERTY");
        Object ee = e(limC, "ENTITY_EXPANSION_LIMIT"), mo = e(limC, "MAX_OCCUR_NODE_LIMIT");
        check(inv(ee, "mapper").getClass().getName().endsWith("IntegerMapper"), "ee-limit-uses-IntegerMapper");
        check(inv(mo, "mapper").getClass().getName().endsWith("IntegerMapper"), "mo-limit-uses-IntegerMapper");
        Object m = smC.getDeclaredConstructor().newInstance();
        java.lang.reflect.Method slStr = smC.getMethod("setLimit", String.class, stC, Object.class);
        check((Boolean) slStr.invoke(m, "jdk.xml.entityExpansionLimit", api, "7"), "mgr-setLimit-str-name");
        check(inv(m, "getLimit", ee).equals(7), "mgr-ee-7-after-str");
        check(inv(m, "getLimitValueAsString", ee).equals("7"), "mgr-ee-str-7");
        check(inv(m, "getState", ee).toString().equals("APIPROPERTY"), "mgr-ee-state-api");
        check((Boolean) inv(m, "is", ee, 7), "mgr-is-value-7");
        // live parse loop: entityExpansionLimit=1 via the factory attribute, two references violate it
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute("jdk.xml.entityExpansionLimit", "1");
        check(dbf.getAttribute("jdk.xml.entityExpansionLimit").equals("1"), "factory-attr-readback-1");
        try {
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<!DOCTYPE r [<!ENTITY b \"x\">]><r>&b;&b;</r>".getBytes("UTF-8")));
            check(false, "expansion-limit-violates");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().equals("JAXP00010001: The parser has encountered more than \"1\" entity expansions in this document; "
                    + "this is the limit imposed by \"jdk.xml.entityExpansionLimit\"."), "expansion-limit-message");
        }
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf2.setAttribute("jdk.xml.entityExpansionLimit", "10");
        dbf2.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                "<!DOCTYPE r [<!ENTITY b \"x\">]><r>&b;&b;</r>".getBytes("UTF-8")));
        check(true, "expansion-within-limit-ok");
    }
}
