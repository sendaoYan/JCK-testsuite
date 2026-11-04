/*
 * @test
 * @summary javax.management.openmbean.CompositeType conformance: item type access.
 * @run main CompositeTypeTest
 */

import javax.management.openmbean.*;
public class CompositeTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType ct = new CompositeType("type", "desc", new String[]{"a"}, new String[]{"a desc"}, new OpenType[]{SimpleType.STRING});
        check("type".equals(ct.getTypeName()), "type name");
        check(ct.containsKey("a"), "containsKey");
        check(ct.getType("a") == SimpleType.STRING, "getType");
        check("a desc".equals(ct.getDescription("a")), "getDescription");
        check(ct.keySet().contains("a"), "keySet");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: negative matrix / unmodifiable keySet / equals order-independence / ser ----
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
        // white-box: public, extends OpenType, item accessors present
        Class<?> c = CompositeType.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "white-box public");
        check(OpenType.class.isAssignableFrom(c), "white-box extends OpenType");
        check(c.getMethod("containsKey", String.class) != null, "has containsKey");
        check(c.getMethod("getDescription", String.class) != null, "has getDescription(String)");
        check(c.getMethod("getType", String.class) != null, "has getType");
        check(c.getMethod("keySet") != null, "has keySet");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");

        CompositeType full = new CompositeType("MyType", "desc",
                new String[]{"name", "value"},
                new String[]{"name desc", "value desc"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
        // getters precise
        check(full.getTypeName().equals("MyType"), "typeName");
        check(full.getDescription().equals("desc"), "description");
        check(full.getClassName().equals(CompositeData.class.getName()), "className is CompositeData");
        check(!full.isArray(), "not array");
        check(full.containsKey("name") && full.containsKey("value"), "containsKey both");
        check(!full.containsKey("missing") && !full.containsKey(null) && !full.containsKey(""),
                "containsKey missing/null/empty false");
        check(full.getType("name") == SimpleType.STRING, "getType name");
        check(full.getType("value") == SimpleType.INTEGER, "getType value");
        check(full.getType("missing") == null && full.getType(null) == null, "getType missing/null null");
        check(full.getDescription("name").equals("name desc"), "getDescription name");
        check(full.getDescription("missing") == null && full.getDescription(null) == null,
                "getDescription missing/null null");
        check(full.keySet().size() == 2, "keySet size");
        // keySet ascending alphabetical, unmodifiable
        java.util.List<String> keys = new java.util.ArrayList<String>(full.keySet());
        java.util.Collections.sort(keys);
        check(keys.equals(java.util.Arrays.asList("name", "value")), "keySet sorted contents");
        try {
            full.keySet().add("z");
            check(false, "keySet must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        // item names are trimmed when stored
        CompositeType trimmed = new CompositeType("T", "d",
                new String[]{" name ", "value"}, new String[]{"d1", "d2"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
        check(trimmed.containsKey("name") && !trimmed.containsKey(" name "), "item names trimmed");

        // isValue matrix
        CompositeDataSupport cd = new CompositeDataSupport(full,
                new String[]{"name", "value"}, new Object[]{"n", 5});
        CompositeDataSupport cdNull = new CompositeDataSupport(full,
                new String[]{"name", "value"}, new Object[]{null, null});
        check(full.isValue(cd), "isValue same type");
        check(full.isValue(cdNull), "isValue accepts null item values");
        check(!full.isValue(null), "isValue null false");
        check(!full.isValue("x"), "isValue non-CompositeData false");
        CompositeType sub = new CompositeType("MyType", "d",
                new String[]{"name"}, new String[]{"d"}, new OpenType[]{SimpleType.STRING});
        CompositeDataSupport subCd = new CompositeDataSupport(sub, new String[]{"name"}, new Object[]{"n"});
        check(sub.isValue(cd), "subset type accepts value with extra items");
        check(!full.isValue(subCd), "full type rejects value missing an item");
        CompositeType wrongType = new CompositeType("MyType", "d",
                new String[]{"name", "value"}, new String[]{"d", "d"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING});
        CompositeDataSupport wrongCd = new CompositeDataSupport(wrongType,
                new String[]{"name", "value"}, new Object[]{"n", "v"});
        check(!full.isValue(wrongCd), "rejects value with wrong item type");
        check(!full.isValue(new CompositeDataSupport(
                new CompositeType("Other", "d", new String[]{"name", "value"},
                        new String[]{"d", "d"}, new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}),
                new String[]{"name", "value"}, new Object[]{"n", 5})), "rejects different typeName");

        // equals/hashCode: order-independent item set, typeName participates
        CompositeType reordered = new CompositeType("MyType", "desc",
                new String[]{"value", "name"},
                new String[]{"value desc", "name desc"},
                new OpenType[]{SimpleType.INTEGER, SimpleType.STRING});
        check(full.equals(reordered), "equals with reordered item names");
        check(full.hashCode() == reordered.hashCode(), "hashCode equal with reordered items");
        check(full.equals(new CompositeType("MyType", "desc",
                new String[]{"name", "value"}, new String[]{"name desc", "value desc"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER})), "equals independent instance");
        check(!full.equals(new CompositeType("Other", "desc",
                new String[]{"name", "value"}, new String[]{"d", "d"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER})), "different typeName not equal");
        check(!full.equals(new CompositeType("MyType", "desc", new String[]{"name"},
                new String[]{"d"}, new OpenType[]{SimpleType.STRING})), "different item set not equal");
        // descriptions (type-level and item-level) do NOT participate in equals (verified both JDKs)
        check(full.equals(new CompositeType("MyType", "different desc",
                new String[]{"name", "value"}, new String[]{"name desc", "value desc"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER})),
                "type-level description does not participate in equals");
        check(full.equals(new CompositeType("MyType", "desc",
                new String[]{"name", "value"}, new String[]{"a", "b"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER})),
                "item descriptions do not participate in equals");
        check(!full.equals(null), "equals null false");
        check(!full.equals("x"), "equals non-CompositeType false");

        // toString exact
        check(full.toString().equals("javax.management.openmbean.CompositeType(name=MyType,"
                + "items=((itemName=name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),"
                + "(itemName=value,itemType=javax.management.openmbean.SimpleType(name=java.lang.Integer))))"),
                "toString exact");

        // serialization round-trip
        check(roundtrip(full).equals(full), "ser roundtrip equals");
        check(roundtrip(full).hashCode() == full.hashCode(), "ser roundtrip hashCode");

        // negative matrix: exact types and messages (verified on this JDK)
        checkThrowsExact(IllegalArgumentException.class,
                "Array arguments itemNames[], itemDescriptions[] and itemTypes[] should be of same length (got 1, 1 and 2).",
                () -> new CompositeType("T", "d", new String[]{"a"}, new String[]{"x"},
                        new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}),
                "mismatched array lengths");
        checkThrowsExact(IllegalArgumentException.class, "Argument itemNames[] cannot be null or empty.",
                () -> new CompositeType("T", "d", (String[]) null, new String[]{"x"},
                        new OpenType[]{SimpleType.STRING}), "null itemNames array");
        checkThrowsExact(IllegalArgumentException.class, "Argument itemNames[] cannot be null or empty.",
                () -> new CompositeType("T", "d", new String[0], new String[0], new OpenType[0]),
                "empty itemNames array (zero-item type not allowed)");
        checkThrowsExact(IllegalArgumentException.class, "Argument's element itemNames[0] cannot be null.",
                () -> new CompositeType("T", "d", new String[]{null}, new String[]{"x"},
                        new OpenType[]{SimpleType.STRING}), "null item name element");
        checkThrowsExact(IllegalArgumentException.class, "Argument's element itemNames[0] cannot be an empty string.",
                () -> new CompositeType("T", "d", new String[]{""}, new String[]{"x"},
                        new OpenType[]{SimpleType.STRING}), "empty item name element");
        checkThrowsExact(IllegalArgumentException.class, "Argument's element itemDescriptions[0] cannot be null.",
                () -> new CompositeType("T", "d", new String[]{"a"}, new String[]{null},
                        new OpenType[]{SimpleType.STRING}), "null item description element");
        checkThrowsExact(IllegalArgumentException.class, "Argument's element itemTypes[0] cannot be null.",
                () -> new CompositeType("T", "d", new String[]{"a"}, new String[]{"x"},
                        new OpenType[]{null}), "null item type element");
        checkThrowsExact(OpenDataException.class,
                "Argument's element itemNames[1]=\"a\" duplicates a previous item names.",
                () -> new CompositeType("T", "d", new String[]{"a", "a"}, new String[]{"x", "y"},
                        new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}), "duplicate item name");
        checkThrowsExact(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
                () -> new CompositeType(null, "d", new String[]{"a"}, new String[]{"x"},
                        new OpenType[]{SimpleType.STRING}), "null typeName");
        checkThrowsExact(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
                () -> new CompositeType("", "d", new String[]{"a"}, new String[]{"x"},
                        new OpenType[]{SimpleType.STRING}), "empty typeName");
        checkThrowsExact(IllegalArgumentException.class, "Argument description cannot be null or empty",
                () -> new CompositeType("T", null, new String[]{"a"}, new String[]{"x"},
                        new OpenType[]{SimpleType.STRING}), "null description");
    }
}
