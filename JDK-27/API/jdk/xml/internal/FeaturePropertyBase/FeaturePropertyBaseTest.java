/*
 * @test
 * @summary jdk.xml.internal.FeaturePropertyBase conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main FeaturePropertyBaseTest
 */
public class FeaturePropertyBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.FeaturePropertyBase", false, FeaturePropertyBaseTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.FeaturePropertyBase"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
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
        check(md.size() == 5, "method-count");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("getValueAsString"), "method-getValueAsString");
        check(md.contains("getValueByIndex"), "method-getValueByIndex");
        check(md.contains("setValue"), "method-setValue");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object inv(Object target, String name, Object... args) throws Exception {
        Class<?> c = (target instanceof Class) ? (Class<?>) target : target.getClass();
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (!m.getName().equals(name) || m.getParameterCount() != args.length) continue;
                boolean ok = true;
                for (int i = 0; i < args.length; i++) {
                    Class<?> p = m.getParameterTypes()[i];
                    if (args[i] == null) { if (p.isPrimitive()) { ok = false; break; } continue; }
                    Class<?> a = args[i].getClass();
                    if (p.isPrimitive()) { if (!(a == Integer.class || a == Boolean.class)) { ok = false; break; } }
                    else if (!p.isAssignableFrom(a)) { ok = false; break; }
                }
                if (ok) { m.setAccessible(true); return m.invoke(target instanceof Class ? null : target, args); }
            }
        }
        throw new NoSuchMethodException(name);
    }

    static void deep() throws Exception {
        Class<?> base = Class.forName("jdk.xml.internal.FeaturePropertyBase", false, FeaturePropertyBaseTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(base.getModifiers()), "abstract-cannot-instantiate");
        try { base.getDeclaredConstructor().newInstance(); check(false, "newInstance-InstantiationException"); }
        catch (InstantiationException ie) { check(true, "instantiation-blocked"); }
        // public concrete subclass used as the vehicle for inherited API
        Class<?> mgrC = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager", false, FeaturePropertyBaseTest.class.getClassLoader());
        check(base.isAssignableFrom(mgrC), "subclass-assignable");
        Object mgr = mgrC.getDeclaredConstructor().newInstance();
        Class<?> propC = Class.forName("jdk.xml.internal.XMLSecurityPropertyManager$Property", false, FeaturePropertyBaseTest.class.getClassLoader());
        Class<?> fpbStateC = Class.forName("jdk.xml.internal.FeaturePropertyBase$State", false, FeaturePropertyBaseTest.class.getClassLoader());
        Object dtd = null, schema = null;
        for (Object o : propC.getEnumConstants()) {
            if (o.toString().equals("ACCESS_EXTERNAL_DTD")) dtd = o;
            if (o.toString().equals("ACCESS_EXTERNAL_SCHEMA")) schema = o;
        }
        Object api = null, sysp = null;
        for (Object o : fpbStateC.getEnumConstants()) {
            if (o.toString().equals("APIPROPERTY")) api = o;
            if (o.toString().equals("SYSTEMPROPERTY")) sysp = o;
        }
        // setValue(Enum, State, String) / getValue(Enum, String) round trip
        inv(mgr, "setValue", dtd, api, "file");
        check(inv(mgr, "getValue", dtd).equals("file"), "setValue-enum-getValue-enum");
        check(inv(mgr, "getValue", "javax.xml.accessExternalDTD").equals("file"), "getValue-byname");
        check(inv(mgr, "getValueAsString", "javax.xml.accessExternalDTD").equals("file"), "getValueAsString-hit");
        check(inv(mgr, "getValueAsString", "no.such.prop") == null, "getValueAsString-miss");
        check(inv(mgr, "getValue", "no.such.prop") == null, "getValue-miss-null");
        // overriding order: SYSTEMPROPERTY (lower) must not override APIPROPERTY
        inv(mgr, "setValue", dtd, sysp, "jar");
        check(inv(mgr, "getValue", dtd).equals("file"), "lower-state-blocked");
        // setValue(String, State, Object) by name
        check((Boolean) inv(mgr, "setValue", "javax.xml.accessExternalSchema", api, "http,https"), "setValue-string-obj");
        check(inv(mgr, "getValue", schema).equals("http,https"), "setValue-string-obj-readback");
        check(!(Boolean) inv(mgr, "setValue", "no.such.prop", api, "x"), "setValue-string-obj-miss");
        // setValue(String, State, boolean) maps to FEATURE_TRUE/FEATURE_FALSE literals
        check((Boolean) inv(mgr, "setValue", "javax.xml.accessExternalDTD", api, Boolean.TRUE), "setValue-string-bool");
        check(inv(mgr, "getValue", dtd).equals("true"), "setValue-string-bool-true-literal");
        inv(mgr, "setValue", "javax.xml.accessExternalDTD", api, Boolean.FALSE);
        check(inv(mgr, "getValue", dtd).equals("false"), "setValue-string-bool-false-literal");
        // setValue(int, State, String) / getValueByIndex(int)
        inv(mgr, "setValue", Integer.valueOf(1), api, "ftp");
        check(inv(mgr, "getValueByIndex", Integer.valueOf(1)).equals("ftp"), "setValue-byindex");
        check(inv(mgr, "getValue", schema).equals("ftp"), "byindex-visible-in-enum-read");
        // getIndex(String) abstract routed through subclass
        check((Integer) inv(mgr, "getIndex", "javax.xml.accessExternalDTD") == 0, "getIndex-dtd-0");
        check((Integer) inv(mgr, "getIndex", "javax.xml.accessExternalStylesheet") == 2, "getIndex-ss-2");
        check((Integer) inv(mgr, "getIndex", "bogus") == -1, "getIndex-miss");
        // generic getIndex(Class, String) matches by enum constant toString()
        java.lang.reflect.Method gi = base.getMethod("getIndex", Class.class, String.class);
        check((Integer) gi.invoke(mgr, propC, "ACCESS_EXTERNAL_DTD") == 0, "generic-getIndex-hit");
        check((Integer) gi.invoke(mgr, propC, "ACCESS_EXTERNAL_SCHEMA") == 1, "generic-getIndex-schema");
        check((Integer) gi.invoke(mgr, propC, "BOGUS") == -1, "generic-getIndex-miss");
        // package-private getSystemProperty(Enum, String): unconditional write on hit
        java.lang.reflect.Method gsp = base.getDeclaredMethod("getSystemProperty", Enum.class, String.class);
        gsp.setAccessible(true);
        check(!(Boolean) gsp.invoke(mgr, schema, "javax.xml.accessExternalSchema"), "getSystemProperty-miss");
        System.setProperty("javax.xml.accessExternalSchema", "ftp");
        check((Boolean) gsp.invoke(mgr, schema, "javax.xml.accessExternalSchema"), "getSystemProperty-hit");
        check(inv(mgr, "getValue", schema).equals("ftp"), "getSystemProperty-applies-value");
        System.clearProperty("javax.xml.accessExternalSchema");
        // package-private getPropertyConfig(Enum, String): reads JAXP config file
        java.lang.reflect.Method gpc = base.getDeclaredMethod("getPropertyConfig", Enum.class, String.class);
        gpc.setAccessible(true);
        check(!(Boolean) gpc.invoke(mgr, schema, "no.such.config.key"), "getPropertyConfig-miss");
        // states array reflects the precedence engine (field inherited from base)
        java.lang.reflect.Field stf = base.getDeclaredField("states");
        stf.setAccessible(true);
        Object[] sts = (Object[]) stf.get(mgr);
        check(sts.length == 3, "states-length-3");
        check(sts[0].toString().equals("APIPROPERTY"), "state-slot0-api");
        // values array length follows Property.values()
        java.lang.reflect.Field vf = base.getDeclaredField("values");
        vf.setAccessible(true);
        check(((String[]) vf.get(mgr)).length == 3, "values-length-3");
    }
}
