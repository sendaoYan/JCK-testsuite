/*
 * @test
 * @summary jdk.xml.internal.JdkProperty conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main JdkPropertyTest
 */
public class JdkPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkProperty", false, JdkPropertyTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkProperty"), "name");
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
        check(md.size() == 2, "method-count");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("setValue"), "method-setValue");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }
    static Object inv(Object t, String n, Object... a) throws Exception {
        for (Class<?> k = t instanceof Class ? (Class<?>) t : t.getClass(); k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (m.getName().equals(n) && m.getParameterCount() == a.length) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkProperty", false, JdkPropertyTest.class.getClassLoader());
        Class<?> mapC = Class.forName("jdk.xml.internal.JdkProperty$ImplPropMap", false, JdkPropertyTest.class.getClassLoader());
        Class<?> stC = Class.forName("jdk.xml.internal.JdkProperty$State", false, JdkPropertyTest.class.getClassLoader());
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(mapC, Class.class, Object.class, stC);
        Object stDEFAULT = e(stC, "DEFAULT"), stAPI = e(stC, "APIPROPERTY"),
               stSYS = e(stC, "SYSTEMPROPERTY"), stLEGACY = e(stC, "LEGACY_APIPROPERTY");
        Object cd = e(mapC, "CDATACHUNKSIZE"), odp = e(mapC, "OVERRIDEPARSER"), eef = e(mapC, "ENABLEEXTFUNC");
        // Boolean-typed property with DEFAULT state: system property captured at construction
        Object p1 = ctor.newInstance(odp, Boolean.class, Boolean.FALSE, stDEFAULT);
        check(inv(p1, "getValue").equals(Boolean.FALSE), "bool-default-value");
        System.setProperty("jdk.xml.overrideDefaultParser", "true");
        Object p2 = ctor.newInstance(odp, Boolean.class, Boolean.FALSE, stDEFAULT);
        check(inv(p2, "getValue").equals(Boolean.TRUE), "bool-sysprop-captured");
        System.clearProperty("jdk.xml.overrideDefaultParser");
        // construction state other than DEFAULT skips system-property read
        System.setProperty("jdk.xml.overrideDefaultParser", "true");
        Object p3 = ctor.newInstance(odp, Boolean.class, Boolean.FALSE, stAPI);
        check(inv(p3, "getValue").equals(Boolean.FALSE), "non-default-state-skips-sysprop");
        System.clearProperty("jdk.xml.overrideDefaultParser");
        // Integer-typed property with DEFAULT state and no sysprop: Integer.parseInt(null) -> NFE
        try { ctor.newInstance(cd, Integer.class, Integer.valueOf(0), stDEFAULT); check(false, "int-null-parse"); }
        catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NumberFormatException, "int-null-parse-NFE");
        }
        // Integer-typed with sysprop set: value captured and converted
        System.setProperty("jdk.xml.cdataChunkSize", "18");
        Object p4 = ctor.newInstance(cd, Integer.class, Integer.valueOf(0), stDEFAULT);
        check(inv(p4, "getValue").equals(Integer.valueOf(18)), "int-sysprop-18");
        System.setProperty("jdk.xml.cdataChunkSize", "abc");
        try { ctor.newInstance(cd, Integer.class, Integer.valueOf(0), stDEFAULT); check(false, "int-bad-parse"); }
        catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NumberFormatException, "int-bad-parse-NFE");
        }
        System.clearProperty("jdk.xml.cdataChunkSize");
        // Boolean current sysprop short-circuits legacy sysprop fallback (false is non-null)
        System.setProperty("http://www.oracle.com/feature/use-service-mechanism", "true");
        Object p5 = ctor.newInstance(odp, Boolean.class, Boolean.FALSE, stDEFAULT);
        check(inv(p5, "getValue").equals(Boolean.FALSE), "legacy-sp-blocked-by-boolean-current");
        System.clearProperty("http://www.oracle.com/feature/use-service-mechanism");
        // String-typed property: current sysprop null -> legacy sysprop consulted
        Object iss = e(mapC, "ISSTANDALONE");
        System.setProperty("jdk.xml.isStandalone", "yes");
        Object p6 = ctor.newInstance(iss, String.class, "no", stDEFAULT);
        check(inv(p6, "getValue").equals("yes"), "string-sysprop");
        System.clearProperty("jdk.xml.isStandalone");
        // setValue(String, T, State): precedence from getState(name), not from passed state
        Object q1 = ctor.newInstance(cd, Integer.class, Integer.valueOf(5), stAPI);
        check((Boolean) inv(q1, "setValue", "jdk.xml.cdataChunkSize", Integer.valueOf(9), stSYS), "setValue-api-name-wins");
        check(inv(q1, "getValue").equals(Integer.valueOf(9)), "setValue-api-name-applied");
        Object q2 = ctor.newInstance(cd, Integer.class, Integer.valueOf(5), stSYS);
        check((Boolean) inv(q2, "setValue", "jdk.xml.cdataChunkSize", Integer.valueOf(12), stLEGACY), "setValue-legacy-ge-sys");
        check(inv(q2, "getValue").equals(Integer.valueOf(12)), "setValue-legacy-applied");
        // setValue resolves the caller state from the NAME, ignoring the passed state:
        // current API name on an API-initialized property still wins; legacy name loses
        Object q3 = ctor.newInstance(cd, Integer.class, Integer.valueOf(5), stAPI);
        check((Boolean) inv(q3, "setValue", "jdk.xml.cdataChunkSize", Integer.valueOf(7), stLEGACY), "setValue-state-from-name-not-arg");
        check(inv(q3, "getValue").equals(Integer.valueOf(7)), "setValue-state-from-name-applied");
        Object q4 = ctor.newInstance(eef, Boolean.class, Boolean.FALSE, stAPI);
        check(!(Boolean) inv(q4, "setValue", "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions", Boolean.TRUE, stAPI), "setValue-legacy-name-lt-api-blocked");
        check(inv(q4, "getValue").equals(Boolean.FALSE), "setValue-blocked-value-unchanged");
        check(!(Boolean) inv(q3, "setValue", "bogus.name", Integer.valueOf(1), stAPI), "setValue-bogus-name-false");
        // generic bridge: Boolean value on ENABLEEXTFUNC
        Object r1 = ctor.newInstance(eef, Boolean.class, Boolean.FALSE, stDEFAULT);
        check((Boolean) inv(r1, "setValue", "jdk.xml.enableExtensionFunctions", Boolean.TRUE, stAPI), "setValue-eef");
        check(inv(r1, "getValue").equals(Boolean.TRUE), "setValue-eef-value");
        // real-XML linkage: cdataChunkSize sysprop drives StAX factory chunking knob
        System.setProperty("jdk.xml.cdataChunkSize", "32");
        javax.xml.stream.XMLInputFactory xif = javax.xml.stream.XMLInputFactory.newInstance();
        check(xif.getProperty("jdk.xml.cdataChunkSize").equals(Integer.valueOf(32)), "stax-cdata-default-from-sysprop");
        System.clearProperty("jdk.xml.cdataChunkSize");
    }
}
