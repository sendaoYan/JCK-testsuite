/*
 * @test
 * @summary jdk.xml.internal.XMLLimitAnalyzer.NameMap conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main NameMapTest
 */
public class NameMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：SAXMessages 的“属性未识别”消息随默认 Locale 本地化（如 zh_CN 为中文），断言的是英文消息
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        Class<?> c = Class.forName("jdk.xml.internal.XMLLimitAnalyzer$NameMap", false, NameMapTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLLimitAnalyzer$NameMap"), "name");
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
        check(ec.contains("ENTITY_EXPANSION_LIMIT"), "enum-ENTITY_EXPANSION_LIMIT");
        check(ec.contains("MAX_OCCUR_NODE_LIMIT"), "enum-MAX_OCCUR_NODE_LIMIT");
        check(ec.contains("ELEMENT_ATTRIBUTE_LIMIT"), "enum-ELEMENT_ATTRIBUTE_LIMIT");
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
                    if (a[i] == null) { if (m.getParameterTypes()[i].isPrimitive()) { ok = false; break; } continue; }
                    Class<?> p = m.getParameterTypes()[i];
                    if (!p.isAssignableFrom(a[i].getClass())) { ok = false; break; }
                }
                if (ok) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }
    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLLimitAnalyzer$NameMap", false, NameMapTest.class.getClassLoader());
        Object[] vals = c.getEnumConstants();
        check(vals.length == 3, "values-length-3");
        check(vals[0].toString().equals("ENTITY_EXPANSION_LIMIT") && ((Enum<?>) vals[0]).ordinal() == 0, "order-EE");
        check(vals[1].toString().equals("MAX_OCCUR_NODE_LIMIT") && ((Enum<?>) vals[1]).ordinal() == 1, "order-MO");
        check(vals[2].toString().equals("ELEMENT_ATTRIBUTE_LIMIT") && ((Enum<?>) vals[2]).ordinal() == 2, "order-EA");
        // getOldName(String): package-private mapping from the jdk.xml.* name to the pre-JDK-17 plain name
        check(inv(vals[0], "getOldName", "jdk.xml.entityExpansionLimit").equals("entityExpansionLimit"), "old-EE");
        check(inv(vals[1], "getOldName", "jdk.xml.maxOccurLimit").equals("maxOccurLimit"), "old-MO");
        check(inv(vals[2], "getOldName", "jdk.xml.elementAttributeLimit").equals("elementAttributeLimit"), "old-EA");
        // a non-matching new name yields null on every constant (each map only knows its own pair)
        check(inv(vals[0], "getOldName", "jdk.xml.maxOccurLimit") == null, "old-EE-wrong-name-null");
        check(inv(vals[1], "getOldName", "jdk.xml.entityExpansionLimit") == null, "old-MO-wrong-name-null");
        check(inv(vals[2], "getOldName", "bogus") == null, "old-EA-bogus-null");
        check(inv(vals[0], "getOldName", "entityExpansionLimit") == null, "old-EE-old-name-not-new-null");
        // null input hits the receiver immediately: NPE from newName.equals(...)
        try { inv(vals[0], "getOldName", (Object) null); check(false, "old-null"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof NullPointerException, "old-null-NPE"); }
        // the legacy names are exactly the Limit.spOld() values of the same three limits
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, NameMapTest.class.getClassLoader());
        check(inv(e(limC, "ENTITY_EXPANSION_LIMIT"), "spOld").equals("entityExpansionLimit"), "spOld-EE-matches");
        check(inv(e(limC, "MAX_OCCUR_NODE_LIMIT"), "spOld").equals("maxOccurLimit"), "spOld-MO-matches");
        check(inv(e(limC, "ELEMENT_ATTRIBUTE_LIMIT"), "spOld").equals("elementAttributeLimit"), "spOld-EA-matches");
        // valueOf round trips and bogus rejection
        for (Object v : vals) check(java.lang.Enum.valueOf((Class<Enum>) (Class) c, v.toString()) == v, "valueOf-" + v);
        try { java.lang.Enum.valueOf((Class<Enum>) (Class) c, "TOTAL_ENTITY_SIZE_LIMIT"); check(false, "valueOf-bogus"); }
        catch (IllegalArgumentException iae) { check(true, "valueOf-bogus-IAE"); }
        // real linkage 1: the old plain names are STILL honored as system properties (readSystemProperties fallback)
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager", false, NameMapTest.class.getClassLoader());
        System.setProperty("entityExpansionLimit", "7");
        try {
            Object m = smC.getDeclaredConstructor().newInstance();
            inv(m, "readSystemProperties");
            check(inv(m, "getLimit", e(limC, "ENTITY_EXPANSION_LIMIT")).equals(7), "sysprop-legacy-honored");
        } finally {
            System.clearProperty("entityExpansionLimit");
        }
        // real linkage 2: but the current name wins when both are present
        System.setProperty("entityExpansionLimit", "7");
        System.setProperty("jdk.xml.entityExpansionLimit", "9");
        try {
            Object m = smC.getDeclaredConstructor().newInstance();
            inv(m, "readSystemProperties");
            check(inv(m, "getLimit", e(limC, "ENTITY_EXPANSION_LIMIT")).equals(9), "sysprop-new-wins");
        } finally {
            System.clearProperty("entityExpansionLimit");
            System.clearProperty("jdk.xml.entityExpansionLimit");
        }
        // real linkage 3: the old plain name is NOT an API attribute name - factories reject it
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        try {
            dbf.setAttribute("entityExpansionLimit", "3");
            check(false, "legacy-plain-attr-accepted");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage().equals("Property 'entityExpansionLimit' is not recognized."), "legacy-plain-attr-IAE");
        }
        dbf.setAttribute("jdk.xml.entityExpansionLimit", "3");
        check(dbf.getAttribute("jdk.xml.entityExpansionLimit").equals("3"), "current-name-attr-readback");
    }
}
