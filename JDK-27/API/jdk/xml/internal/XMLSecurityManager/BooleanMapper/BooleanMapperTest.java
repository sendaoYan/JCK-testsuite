/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.BooleanMapper conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main BooleanMapperTest
 */
public class BooleanMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$BooleanMapper", false, BooleanMapperTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$BooleanMapper"), "name");
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$BooleanMapper", true, BooleanMapperTest.class.getClassLoader());
        Class<?> base = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper", false, BooleanMapperTest.class.getClassLoader());
        Object bm = c.getDeclaredConstructor().newInstance();
        check(base.isInstance(bm), "instance-is-ValueMapper");
        // toInt: Boolean input
        check(inv(bm, "toInt", Boolean.TRUE).equals(1), "toInt-TRUE-1");
        check(inv(bm, "toInt", Boolean.FALSE).equals(0), "toInt-FALSE-0");
        // toInt: String input via equalsIgnoreCase("true")
        check(inv(bm, "toInt", "true").equals(1), "toInt-str-true");
        check(inv(bm, "toInt", "TRUE").equals(1), "toInt-str-TRUE");
        check(inv(bm, "toInt", "True").equals(1), "toInt-str-True-case-insensitive");
        check(inv(bm, "toInt", "false").equals(0), "toInt-str-false");
        check(inv(bm, "toInt", "FALSE").equals(0), "toInt-str-FALSE");
        check(inv(bm, "toInt", "yes").equals(0), "toInt-str-yes-not-true");
        check(inv(bm, "toInt", "").equals(0), "toInt-str-empty");
        check(inv(bm, "toInt", "1").equals(0), "toInt-str-1-not-true");
        try { inv(bm, "toInt", (Object) null); check(false, "toInt-null"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof NullPointerException, "toInt-null-NPE"); }
        // toObject: any non-zero int maps to TRUE
        check(inv(bm, "toObject", 1).equals(Boolean.TRUE), "toObject-1-true");
        check(inv(bm, "toObject", 0).equals(Boolean.FALSE), "toObject-0-false");
        check(inv(bm, "toObject", 2).equals(Boolean.TRUE), "toObject-2-true-nonzero");
        check(inv(bm, "toObject", -1).equals(Boolean.TRUE), "toObject-neg-true");
        // toString
        check(inv(bm, "toString", 0).equals("false"), "toString-0");
        check(inv(bm, "toString", 1).equals("true"), "toString-1");
        check(inv(bm, "toString", 5).equals("true"), "toString-5-nonzero");
        // round trip both directions
        check(inv(bm, "toInt", inv(bm, "toObject", 1)).equals(1), "roundtrip-1");
        check(inv(bm, "toInt", inv(bm, "toObject", 0)).equals(0), "roundtrip-0");
        check(inv(bm, "toObject", inv(bm, "toInt", "true")).equals(Boolean.TRUE), "roundtrip-str-true");
        check(inv(bm, "toObject", inv(bm, "toInt", "nope")).equals(Boolean.FALSE), "roundtrip-str-nope");
        // real linkage: the mapper backs the STAX_SUPPORT_DTD and XERCES_DISALLOW_DTD limits
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager", false, BooleanMapperTest.class.getClassLoader());
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, BooleanMapperTest.class.getClassLoader());
        Class<?> stC = Class.forName("jdk.xml.internal.JdkProperty$State", false, BooleanMapperTest.class.getClassLoader());
        Object api = e(stC, "APIPROPERTY");
        Object m = smC.getDeclaredConstructor().newInstance();
        Object stax = e(limC, "STAX_SUPPORT_DTD"), xer = e(limC, "XERCES_DISALLOW_DTD");
        check(inv(e(limC, "STAX_SUPPORT_DTD"), "mapper").getClass().getName().endsWith("BooleanMapper"), "stax-limit-uses-BooleanMapper");
        check(inv(e(limC, "XERCES_DISALLOW_DTD"), "mapper").getClass().getName().endsWith("BooleanMapper"), "xerces-limit-uses-BooleanMapper");
        check(inv(m, "getLimitValueAsString", stax).equals("true"), "stax-default-true");
        check(inv(m, "getLimitValueAsString", xer).equals("false"), "xerces-default-false");
        java.lang.reflect.Method slObj = smC.getMethod("setLimit", limC, stC, Object.class);
        slObj.invoke(m, stax, api, Boolean.FALSE);
        check(inv(m, "getLimitValueAsString", stax).equals("false"), "stax-set-false-str");
        check(inv(m, "getLimit", stax).equals(0), "stax-set-false-int");
        check(!(Boolean) inv(m, "is", stax), "stax-is-false-after-unset");
        slObj.invoke(m, xer, api, Boolean.TRUE);
        check(inv(m, "getLimitValueAsString", xer).equals("true"), "xerces-set-true");
        slObj.invoke(m, xer, api, "TRUE");
        check(inv(m, "getLimitValueAsString", xer).equals("true"), "xerces-set-str-TRUE");
        slObj.invoke(m, xer, api, "bogus");
        check(inv(m, "getLimitValueAsString", xer).equals("false"), "xerces-set-bogus-false");
        // live StAX linkage: SUPPORT_DTD property on a real factory follows the same default
        javax.xml.stream.XMLInputFactory xif = javax.xml.stream.XMLInputFactory.newInstance();
        check(xif.getProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD).equals(Boolean.TRUE), "stax-factory-default-true");
        xif.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        check(xif.getProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD).equals(Boolean.FALSE), "stax-factory-set-false");
        // with DTD support off, a document carrying an internal DTD subset still streams through
        javax.xml.stream.XMLStreamReader r = xif.createXMLStreamReader(
                new java.io.StringReader("<!DOCTYPE r [<!ENTITY b \"y\">]><r/>"));
        while (r.hasNext()) r.next();
        check(true, "stax-factory-nodtd-parse-ok");
        r.close();
    }
}
