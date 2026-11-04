/*
 * @test
 * @summary javax.management.openmbean.CompositeDataSupport conformance: get/containsKey.
 * @run main CompositeDataSupportTest
 */

import javax.management.openmbean.*;
public class CompositeDataSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType ct = new CompositeType("type", "desc", new String[]{"a"}, new String[]{"a desc"}, new OpenType[]{SimpleType.STRING});
        CompositeDataSupport cd = new CompositeDataSupport(ct, new String[]{"a"}, new Object[]{"x"});
        check("x".equals(cd.get("a")), "get");
        check(cd.containsKey("a"), "containsKey");
        check(cd.getCompositeType() == ct, "getCompositeType");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: negative matrix / Map ctor / views / equals / ser ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrowsExact(Class<? extends Throwable> expected, String message, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (!message.equals(t.getMessage()))
                    throw new AssertionError(msg + ": message <" + t.getMessage() + "> != <" + message + ">");
                passed++;
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundtrip(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        T copy = (T) ois.readObject();
        ois.close();
        return copy;
    }

    static void testDeep() throws Exception {
        // white-box
        Class<?> c = CompositeDataSupport.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "white-box public");
        check(CompositeData.class.isAssignableFrom(c), "implements CompositeData");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getMethod("getCompositeType") != null, "has getCompositeType");
        check(c.getMethod("get", String.class) != null, "has get(String)");
        check(c.getMethod("getAll", String[].class) != null, "has getAll");
        check(c.getMethod("values") != null, "has values");
        // CompositeData interface has NO keySet (values only)
        boolean keySetOnCd = false;
        for (java.lang.reflect.Method m : CompositeData.class.getMethods())
            if ("keySet".equals(m.getName())) keySetOnCd = true;
        check(!keySetOnCd, "CompositeData has no keySet");

        CompositeType ct = new CompositeType("MyType", "desc",
                new String[]{"name", "value"},
                new String[]{"name desc", "value desc"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
        String[] names = {"name", "value"};
        CompositeDataSupport cd = new CompositeDataSupport(ct, names, new Object[]{"n", 5});

        check(cd.getCompositeType().equals(ct), "getCompositeType equals");
        check(cd.get("name").equals("n"), "get name");
        check(cd.get("value").equals(5), "get value");
        Object[] all = cd.getAll(new String[]{"value", "name"});
        check(all.length == 2 && all[0].equals(5) && all[1].equals("n"), "getAll order follows request");
        check(cd.getAll(new String[0]).length == 0, "getAll empty");
        check(cd.getAll(null).length == 0, "getAll null treated as empty");
        check(cd.containsKey("name") && cd.containsKey("value"), "containsKey");
        check(!cd.containsKey("missing") && !cd.containsKey(null) && !cd.containsKey(""),
                "containsKey missing/null/empty false");
        check(cd.containsValue("n") && cd.containsValue(5), "containsValue");
        check(!cd.containsValue("x") && !cd.containsValue(null), "containsValue absent/null false");
        java.util.Collection<?> values = cd.values();
        check(values.size() == 2, "values size");
        check(values.contains("n") && values.contains(5), "values contents");
        try {
            java.util.Collection raw = values;
            raw.add("z");
            check(false, "values must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }

        // null item values are allowed for any item type (only non-null values are type-checked)
        CompositeDataSupport withNull = new CompositeDataSupport(ct, names, new Object[]{null, null});
        check(withNull.get("name") == null && withNull.get("value") == null, "null values stored");
        check(withNull.containsValue(null), "containsValue null true");
        check(ct.isValue(withNull), "type still validates null-valued instance");

        // Map constructor round-trip and negatives
        java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();
        map.put("name", "n");
        map.put("value", 5);
        CompositeDataSupport cdMap = new CompositeDataSupport(ct, map);
        check(cdMap.equals(cd) && cdMap.hashCode() == cd.hashCode(), "Map ctor equals array ctor");
        // constructor order-independence: values matched by name
        CompositeDataSupport cdReordered = new CompositeDataSupport(ct, new String[]{"value", "name"},
                new Object[]{5, "n"});
        check(cdReordered.equals(cd), "ctor argument order independent");
        check(cdReordered.hashCode() == cd.hashCode(), "hashCode order independent");
        checkThrowsExact(IllegalArgumentException.class, "Null or empty items map",
                () -> new CompositeDataSupport(ct, (java.util.Map<String, ?>) null), "null map");
        java.util.Map<String, Object> nullKeyMap = new java.util.HashMap<String, Object>();
        nullKeyMap.put("name", "n");
        nullKeyMap.put("value", 5);
        nullKeyMap.put(null, "v");
        checkThrowsExact(IllegalArgumentException.class, "Null or empty item name",
                () -> new CompositeDataSupport(ct, nullKeyMap), "map with null key");
        java.util.Map<String, Object> missingMap = new java.util.HashMap<String, Object>();
        missingMap.put("name", "n");
        checkThrowsExact(OpenDataException.class,
                "Item names do not match CompositeType: names in items but not in CompositeType: []; "
                + "names in CompositeType but not in items: [value]",
                () -> new CompositeDataSupport(ct, missingMap), "map missing item");
        java.util.Map<String, Object> wrongMap = new java.util.HashMap<String, Object>();
        wrongMap.put("name", "n");
        wrongMap.put("value", "str");
        checkThrowsExact(OpenDataException.class,
                "Argument value of wrong type for item value: value str, "
                + "type javax.management.openmbean.SimpleType(name=java.lang.Integer)",
                () -> new CompositeDataSupport(ct, wrongMap), "map wrong value type");

        // equals / hashCode
        CompositeDataSupport cd2 = new CompositeDataSupport(ct, names, new Object[]{"n", 5});
        check(cd.equals(cd2) && cd2.equals(cd), "equals symmetric");
        check(cd.hashCode() == cd2.hashCode(), "hashCode equal");
        check(!cd.equals(new CompositeDataSupport(ct, names, new Object[]{"n", 6})), "different values not equal");
        check(!cd.equals(new CompositeDataSupport(
                new CompositeType("Other", "d", names, new String[]{"a", "b"},
                        new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}),
                names, new Object[]{"n", 5})), "different compositeType not equal");
        check(!cd.equals(null), "equals null false");
        check(!cd.equals("x"), "equals non-CompositeData false");
        // reference impl quirk: a hand-made CompositeData with identical contents is NOT equal
        // (CompositeDataSupport.equals requires the same class, not just interface contract)
        final CompositeType ctF = ct;
        CompositeData custom = new CompositeData() {
            public CompositeType getCompositeType() { return ctF; }
            public Object get(String key) { return "name".equals(key) ? "n" : Integer.valueOf(5); }
            public Object[] getAll(String[] ks) {
                Object[] r = new Object[ks.length];
                for (int i = 0; i < ks.length; i++) r[i] = get(ks[i]);
                return r;
            }
            public boolean containsKey(String key) { return "name".equals(key) || "value".equals(key); }
            public boolean containsValue(Object v) { return false; }
            public java.util.Collection<?> values() { return java.util.Collections.emptyList(); }
        };
        check(!cd.equals(custom), "reference impl requires CompositeDataSupport class");

        // toString
        check(cd.toString().startsWith("javax.management.openmbean.CompositeDataSupport("), "toString prefix");
        check(cd.toString().contains("compositeType=") && cd.toString().contains("contents="),
                "toString structure");
        check(cd.toString().contains("name=n") && cd.toString().contains("value=5"),
                "toString contains item values");

        // serialization round-trip, including null field values
        CompositeDataSupport serCopy = roundtrip(cd);
        check(serCopy.equals(cd), "ser roundtrip equals");
        check(serCopy.getCompositeType().equals(ct), "ser roundtrip type");
        CompositeDataSupport serNull = roundtrip(withNull);
        check(serNull.get("name") == null, "ser roundtrip keeps null value");

        // negative matrix: exact types and messages (verified on this JDK)
        checkThrowsExact(IllegalArgumentException.class, "Different lengths: itemNames[2], itemValues[1]",
                () -> new CompositeDataSupport(ct, names, new Object[]{"x"}), "mismatched array lengths");
        checkThrowsExact(IllegalArgumentException.class, "Null itemNames or itemValues",
                () -> new CompositeDataSupport(ct, (String[]) null, new Object[]{"x", 1}), "null names array");
        checkThrowsExact(IllegalArgumentException.class, "Null itemNames or itemValues",
                () -> new CompositeDataSupport(ct, names, (Object[]) null), "null values array");
        checkThrowsExact(OpenDataException.class,
                "Argument value of wrong type for item value: value notint, "
                + "type javax.management.openmbean.SimpleType(name=java.lang.Integer)",
                () -> new CompositeDataSupport(ct, names, new Object[]{"n", "notint"}), "wrong value type");
        checkThrowsExact(OpenDataException.class,
                "Item names do not match CompositeType: names in items but not in CompositeType: [extra]; "
                + "names in CompositeType but not in items: [value]",
                () -> new CompositeDataSupport(ct, new String[]{"name", "extra"}, new Object[]{"n", 1}),
                "extra/missing item names");
        checkThrowsExact(IllegalArgumentException.class, "Argument compositeType cannot be null.",
                () -> new CompositeDataSupport((CompositeType) null, names, new Object[]{"n", 5}),
                "null compositeType");
        checkThrowsExact(InvalidKeyException.class,
                "Argument key=\"missing\" is not an existing item name for this CompositeData instance.",
                () -> cd.get("missing"), "get missing key");
        checkThrowsExact(IllegalArgumentException.class, "Argument key cannot be a null or empty String.",
                () -> cd.get(null), "get null key");
        checkThrowsExact(IllegalArgumentException.class, "Argument key cannot be a null or empty String.",
                () -> cd.get("  "), "get blank key");
        checkThrowsExact(InvalidKeyException.class,
                "Argument key=\"zz\" is not an existing item name for this CompositeData instance.",
                () -> cd.getAll(new String[]{"name", "zz"}), "getAll missing key");
        checkThrowsExact(IllegalArgumentException.class, "Argument key cannot be a null or empty String.",
                () -> cd.getAll(new String[]{"name", null}), "getAll null key element");
    }
}
