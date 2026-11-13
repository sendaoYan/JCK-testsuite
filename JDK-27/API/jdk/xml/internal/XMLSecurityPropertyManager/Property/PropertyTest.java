/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityPropertyManager.Property conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main PropertyTest
 */
public class PropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：外部 DTD 限制错误消息随默认 Locale 本地化（如 zh_CN 为中文），断言的是英文消息
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager$Property", false, PropertyTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityPropertyManager$Property"), "name");
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
        check(ec.contains("ACCESS_EXTERNAL_DTD"), "enum-ACCESS_EXTERNAL_DTD");
        check(ec.contains("ACCESS_EXTERNAL_SCHEMA"), "enum-ACCESS_EXTERNAL_SCHEMA");
        check(ec.contains("ACCESS_EXTERNAL_STYLESHEET"), "enum-ACCESS_EXTERNAL_STYLESHEET");
        check(fd.size() == 0, "field-count");
        check(md.size() == 5, "method-count");
        check(md.contains("equalsName"), "method-equalsName");
        check(md.contains("propertyName"), "method-propertyName");
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager$Property", false, PropertyTest.class.getClassLoader());
        Object[] vals = c.getEnumConstants();
        check(vals.length == 3, "values-length-3");
        check(vals[0].toString().equals("ACCESS_EXTERNAL_DTD") && ((Enum<?>) vals[0]).ordinal() == 0, "order-DTD");
        check(vals[1].toString().equals("ACCESS_EXTERNAL_SCHEMA") && ((Enum<?>) vals[1]).ordinal() == 1, "order-SCHEMA");
        check(vals[2].toString().equals("ACCESS_EXTERNAL_STYLESHEET") && ((Enum<?>) vals[2]).ordinal() == 2, "order-STYLESHEET");
        Object dtd = vals[0], sch = vals[1], sty = vals[2];
        // propertyName() is the URI-styled constant from javax.xml.XMLConstants
        check(inv(dtd, "propertyName").equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD), "pn-DTD");
        check(inv(sch, "propertyName").equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA), "pn-SCHEMA");
        check(inv(sty, "propertyName").equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET), "pn-STYLESHEET");
        check(inv(dtd, "propertyName").equals("http://javax.xml.XMLConstants/property/accessExternalDTD"), "pn-DTD-literal");
        check(inv(sch, "propertyName").equals("http://javax.xml.XMLConstants/property/accessExternalSchema"), "pn-SCHEMA-literal");
        check(inv(sty, "propertyName").equals("http://javax.xml.XMLConstants/property/accessExternalStylesheet"), "pn-STYLESHEET-literal");
        // systemProperty(): the javax.xml.* accessExternal* names
        check(inv(dtd, "systemProperty").equals("javax.xml.accessExternalDTD"), "sp-DTD");
        check(inv(sch, "systemProperty").equals("javax.xml.accessExternalSchema"), "sp-SCHEMA");
        check(inv(sty, "systemProperty").equals("javax.xml.accessExternalStylesheet"), "sp-STYLESHEET");
        // package-private defaultValue(): all three default to "all"
        check(inv(dtd, "defaultValue").equals("all"), "def-DTD-all");
        check(inv(sch, "defaultValue").equals("all"), "def-SCHEMA-all");
        check(inv(sty, "defaultValue").equals("all"), "def-STYLESHEET-all");
        check(inv(dtd, "systemProperty") != inv(dtd, "propertyName"), "sp-differs-from-pn");
        // equalsName: URI or system property name match, null/bogus/cross never
        check((Boolean) inv(dtd, "equalsName", javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD), "eq-api");
        check((Boolean) inv(dtd, "equalsName", "javax.xml.accessExternalDTD"), "eq-sp");
        check(!(Boolean) inv(dtd, "equalsName", (Object) null), "eq-null-false");
        check(!(Boolean) inv(dtd, "equalsName", "bogus"), "eq-bogus-false");
        check(!(Boolean) inv(dtd, "equalsName", javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA), "eq-cross-false");
        check(!(Boolean) inv(sch, "equalsName", "javax.xml.accessExternalDTD"), "eq-cross-sp-false");
        check((Boolean) inv(sty, "equalsName", "javax.xml.accessExternalStylesheet"), "eq-sty-sp");
        // valueOf round trips + bogus rejection + ordering
        for (Object v : vals) check(java.lang.Enum.valueOf((Class<Enum>) (Class) c, v.toString()) == v, "valueOf-" + v);
        try { java.lang.Enum.valueOf((Class<Enum>) (Class) c, "ACCESS_EXTERNAL_X"); check(false, "valueOf-bogus"); }
        catch (IllegalArgumentException iae) { check(true, "valueOf-bogus-IAE"); }
        check(((Comparable) dtd).compareTo(sch) < 0 && ((Comparable) sch).compareTo(sty) < 0, "natural-order");
        check(((Comparable) dtd).compareTo(dtd) == 0, "compare-self-zero");
        // real linkage 1: the URI names are live factory attributes whose default matches defaultValue()
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        check(dbf.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals("all"), "factory-default-all");
        dbf.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        check(dbf.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals("file"), "factory-set-file");
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        tf.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "file");
        check(tf.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET).equals("file"), "tf-stylesheet-file");
        // real linkage 2: restricting to "" blocks an external DTD with the bundle's exact message
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf2.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
        try {
            dbf2.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<?xml version='1.0'?><!DOCTYPE r SYSTEM 'nonexistent-dxml27.dtd'><r/>".getBytes("UTF-8")));
            check(false, "restricted-dtd-violates");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().equals("External DTD: Failed to read external DTD 'nonexistent-dxml27.dtd', "
                    + "because 'file' access is not allowed due to restriction set by the accessExternalDTD property."),
                    "restricted-dtd-message");
        }
        // real linkage 3: FEATURE_SECURE_PROCESSING does not tighten the property default on this JDK
        javax.xml.parsers.DocumentBuilderFactory dbf3 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf3.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        check(dbf3.getAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD).equals("all"), "fsp-default-still-all");
    }
}
