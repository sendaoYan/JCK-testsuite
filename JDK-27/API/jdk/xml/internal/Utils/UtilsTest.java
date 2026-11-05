/*
 * @test
 * @summary jdk.xml.internal.Utils conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main UtilsTest
 */
public class UtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.Utils", false, UtilsTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.Utils"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
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
        check(md.contains("arraysAppend"), "method-arraysAppend");
        check(md.contains("createEmptyArrayIfNull"), "method-createEmptyArrayIfNull");
        check(md.contains("dPrint"), "method-dPrint");
        check(md.contains("isEmpty"), "method-isEmpty");
        check(md.contains("streamOfIfNonNull"), "method-streamOfIfNonNull");
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

    static void deep() throws Exception {
        // jaxp.debug linkage: the flag is frozen at class initialization, so set it BEFORE first init
        System.setProperty("jaxp.debug", "true");
        Class<?> c;
        java.io.PrintStream oldErr = System.err;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try {
            c = Class.forName("jdk.xml.internal.Utils", true, UtilsTest.class.getClassLoader());
            System.setErr(new java.io.PrintStream(bos, true));
            inv(c, "dPrint", (Object) (java.util.function.Supplier<?>) () -> "hello-dxml27");
            inv(c, "dPrint", (Object) (java.util.function.Supplier<?>) () -> "second-message");
        } finally {
            System.setErr(oldErr);
            System.clearProperty("jaxp.debug");
        }
        check(bos.toString().equals("JAXP: hello-dxml27\nJAXP: second-message\n"), "dPrint-jaxp-debug-output");
        // arraysAppend(null, null) -> null; identity and concatenation for the other combinations
        java.lang.reflect.Method aa = c.getMethod("arraysAppend", Class[].class, Class[].class);
        check(aa.invoke(null, (Object) null, (Object) null) == null, "aa-null-null");
        Class<?>[] orig = {String.class, Integer.class};
        Class<?>[] items = {Boolean.class};
        Class<?>[] both = (Class<?>[]) aa.invoke(null, (Object) orig, (Object) items);
        check(both.length == 3, "aa-concat-length-3");
        check(both[0] == String.class && both[1] == Integer.class && both[2] == Boolean.class, "aa-concat-order");
        check(both != orig, "aa-new-array");
        Class<?>[] onlyOrig = (Class<?>[]) aa.invoke(null, (Object) orig, (Object) null);
        check(onlyOrig.length == 2 && onlyOrig != orig && onlyOrig[0] == String.class, "aa-items-null-copies-original");
        Class<?>[] onlyItems = (Class<?>[]) aa.invoke(null, (Object) null, (Object) items);
        check(onlyItems.length == 1 && onlyItems[0] == Boolean.class, "aa-original-null-uses-items");
        orig[0] = Long.class;
        check(both[0] == String.class, "aa-inputs-unmodified");
        // createEmptyArrayIfNull: byte[] and int[] overloads (exact methods - null args are ambiguous)
        java.lang.reflect.Method ceinB = c.getMethod("createEmptyArrayIfNull", byte[].class);
        java.lang.reflect.Method ceinI = c.getMethod("createEmptyArrayIfNull", int[].class);
        check(((byte[]) ceinB.invoke(null, (Object) new byte[]{1})).length == 1, "cein-byte-nonnull");
        check(((byte[]) ceinB.invoke(null, (Object) (byte[]) null)).length == 0, "cein-byte-null-empty");
        byte[] nb = new byte[]{5};
        check(ceinB.invoke(null, (Object) nb) == nb, "cein-byte-same-instance");
        check(((int[]) ceinI.invoke(null, (Object) (int[]) null)).length == 0, "cein-int-null-empty");
        int[] ni = new int[]{9};
        check(ceinI.invoke(null, (Object) ni) == ni, "cein-int-same-instance");
        // generic createEmptyArrayIfNull(T[], Class<T[]>)
        java.lang.reflect.Method ceinG = c.getMethod("createEmptyArrayIfNull", Object[].class, Class.class);
        String[] sa = {"x"};
        check(ceinG.invoke(null, (Object) sa, String[].class) == sa, "cein-generic-same-instance");
        String[] empty = (String[]) ceinG.invoke(null, (Object) (String[]) null, String[].class);
        check(empty.length == 0 && empty instanceof String[], "cein-generic-null-empty-typed");
        try { ceinG.invoke(null, (Object) null, (Class<?>) null); check(false, "cein-generic-null-type"); }
        catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NullPointerException, "cein-generic-null-type-NPE");
            check(ite.getCause().getMessage().equals("The type argument should not be null."), "cein-generic-NPE-message");
        }
        // streamOfIfNonNull: null yields an empty stream, values stream in order
        java.lang.reflect.Method sof = c.getMethod("streamOfIfNonNull", Object[].class);
        check(((java.util.stream.Stream<?>) sof.invoke(null, (Object) null)).count() == 0, "sof-null-empty");
        java.util.List<?> ls = ((java.util.stream.Stream<?>) sof.invoke(null, (Object) new String[]{"a", "b"}))
                .collect(java.util.stream.Collectors.toList());
        check(ls.equals(java.util.Arrays.asList("a", "b")), "sof-values-in-order");
        // isEmpty: null and "" are empty, everything else is not
        check((Boolean) inv(c, "isEmpty", (Object) null), "isEmpty-null");
        check((Boolean) inv(c, "isEmpty", ""), "isEmpty-empty");
        check(!(Boolean) inv(c, "isEmpty", "a"), "isEmpty-char-false");
        check(!(Boolean) inv(c, "isEmpty", "  "), "isEmpty-spaces-false");
        // real-XML linkage: the factory debug path is what jaxp.debug guards; with the property
        // cleared again, creating and using a factory stays silent on stderr
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(bos2, true));
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<?xml version='1.0'?><root/>".getBytes("UTF-8")));
        } finally {
            System.setErr(oldErr);
        }
        boolean allJaxp = true;
        for (String ln : bos2.toString().split("\n")) if (!ln.isEmpty() && !ln.startsWith("JAXP: ")) allJaxp = false;
        check(allJaxp, "factory-debug-uses-JAXP-prefix");
        // arraysAppend mirrors what factory configuration does when joining parameter lists
        Class<?>[] params = (Class<?>[]) aa.invoke(null,
                (Object) new Class<?>[]{String.class}, (Object) new Class<?>[]{Class.class, Class.class});
        check(params.length == 3 && params[2] == Class.class, "aa-joins-param-lists");
    }
}
