/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.ValueMapper conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main ValueMapperTest
 */
public class ValueMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper", false, ValueMapperTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$ValueMapper"), "name");
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

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper", false, ValueMapperTest.class.getClassLoader());
        // the abstract contract: exactly three public abstract conversion methods, no fields, no state
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "abstract-" + m.getName());
            check(java.lang.reflect.Modifier.isPublic(m.getModifiers()), "public-" + m.getName());
            names.add(m.getName());
        }
        check(names.size() == 3, "declared-methods-3");
        check(c.getDeclaredConstructors().length == 1, "single-default-ctor");
        check(c.getDeclaredFields().length == 0, "no-state-fields");
        // cannot instantiate the abstract class itself
        try { c.getDeclaredConstructor().newInstance(); check(false, "abstract-newInstance"); }
        catch (InstantiationException ie) { check(true, "abstract-newInstance-IE"); }
        // every concrete mapper IS-A ValueMapper
        Class<?> imC = Class.forName("jdk.xml.internal.XMLSecurityManager$IntegerMapper", false, ValueMapperTest.class.getClassLoader());
        Class<?> bmC = Class.forName("jdk.xml.internal.XMLSecurityManager$BooleanMapper", false, ValueMapperTest.class.getClassLoader());
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager$StringMapper", false, ValueMapperTest.class.getClassLoader());
        Object im = imC.getDeclaredConstructor().newInstance();
        Object bm = bmC.getDeclaredConstructor().newInstance();
        java.util.Map<String, Integer> mp = new java.util.HashMap<>(); mp.put("aa", 0);
        Object sm = smC.getConstructor(java.util.Map.class).newInstance(mp);
        check(c.isInstance(im) && c.isInstance(bm) && c.isInstance(sm), "all-mappers-isInstance");
        // dynamic dispatch through Method objects declared on the abstract class
        java.lang.reflect.Method toInt = c.getMethod("toInt", Object.class);
        java.lang.reflect.Method toObject = c.getMethod("toObject", int.class);
        java.lang.reflect.Method toStringM = c.getMethod("toString", int.class);
        check(toInt.invoke(im, "33").equals(33), "dispatch-int-toInt");
        check(toInt.invoke(im, Integer.valueOf(8)).equals(8), "dispatch-int-toInt-boxed");
        check(toInt.invoke(bm, "true").equals(1), "dispatch-bool-toInt");
        check(toInt.invoke(bm, Boolean.FALSE).equals(0), "dispatch-bool-toInt-boxed");
        check(toInt.invoke(sm, "aa").equals(0), "dispatch-str-toInt");
        check(toObject.invoke(im, 12).equals(12), "dispatch-int-toObject");
        check(toObject.invoke(bm, 1).equals(Boolean.TRUE), "dispatch-bool-toObject");
        check(toObject.invoke(bm, 0).equals(Boolean.FALSE), "dispatch-bool-toObject-0");
        check(toObject.invoke(sm, 0).equals("aa"), "dispatch-str-toObject");
        check(toStringM.invoke(im, 12).equals("12"), "dispatch-int-toString");
        check(toStringM.invoke(bm, 1).equals("true"), "dispatch-bool-toString");
        check(toStringM.invoke(sm, 0).equals("aa"), "dispatch-str-toString");
        // polymorphic round trip through the base view only
        check(toInt.invoke(im, toObject.invoke(im, 1)) instanceof Integer, "roundtrip-base-view-IntegerMapper");
        check(toInt.invoke(bm, toObject.invoke(bm, 1)) instanceof Integer, "roundtrip-base-view-BooleanMapper");
        check(toInt.invoke(sm, toObject.invoke(sm, 0)) instanceof Integer, "roundtrip-base-view-StringMapper");
        // the Limit.mapper() accessor hands out instances of this abstract type
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, ValueMapperTest.class.getClassLoader());
        for (Object l : limC.getEnumConstants()) {
            Object mapper = inv(l, "mapper");
            check(c.isInstance(mapper), "limit-mapper-isValueMapper-" + ((Enum<?>) l).name());
        }
    }
}
