/*
 * @test
 * @summary javax.management.openmbean.CompositeDataSupport conformance: white-box structure + black-box behavior.
 * @run main CompositeDataSupportTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class CompositeDataSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void checkThrows(java.lang.Class<? extends Throwable> type, String exactMessage, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected " + type.getSimpleName());
        } catch (Throwable e) {
            if (e instanceof AssertionError) throw (AssertionError) e;
            check(type.isInstance(e), label + ": type " + e.getClass().getName());
            if (exactMessage != null) check(exactMessage.equals(e.getMessage()),
                label + ": message <" + e.getMessage() + "> != <" + exactMessage + ">");
        }
    }

    static CompositeType type() throws Exception {
        return new CompositeType("MyType", "desc",
            new String[]{"name", "value"},
            new String[]{"name desc", "value desc"},
            new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(CompositeData.class.isAssignableFrom(c), "implements CompositeData");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getMethod("getCompositeType") != null, "has getCompositeType");
        check(c.getMethod("get", String.class) != null, "has get");
        check(c.getMethod("getAll", String[].class) != null, "has getAll");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CompositeType ct = type();
        String[] names = {"name", "value"};
        CompositeDataSupport cd = new CompositeDataSupport(ct, names, new Object[]{"n", 5});

        check(cd.getCompositeType().equals(ct), "getCompositeType");
        check(cd.get("name").equals("n"), "get name");
        check(cd.get("value").equals(5), "get value");

        // getAll returns values in the requested key order
        Object[] all = cd.getAll(new String[]{"value", "name"});
        check(all.length == 2, "getAll length");
        check(all[0].equals(5) && all[1].equals("n"), "getAll order");
        check(cd.getAll(new String[0]).length == 0, "getAll empty");
        check(cd.getAll(null).length == 0, "getAll null");

        check(cd.containsKey("name"), "containsKey name");
        check(cd.containsKey("value"), "containsKey value");
        check(!cd.containsKey("missing"), "containsKey missing");
        check(!cd.containsKey(null), "containsKey null false");
        check(!cd.containsKey(""), "containsKey empty false");

        check(cd.containsValue("n"), "containsValue n");
        check(cd.containsValue(5), "containsValue 5");
        check(!cd.containsValue("x"), "containsValue x false");
        check(!cd.containsValue(null), "containsValue null false");

        // values() collection is unmodifiable and ascending by item name
        java.util.Collection<?> values = cd.values();
        check(values.size() == 2, "values size");
        check(values.contains("n") && values.contains(5), "values contents");

        // null item values are allowed when the type permits (non-null check only for type validity)
        CompositeDataSupport withNull = new CompositeDataSupport(ct, names, new Object[]{null, null});
        check(withNull.get("name") == null, "null value stored");
        check(withNull.containsValue(null), "containsValue null true");

        // Map constructor: same semantics
        Map<String, Object> map = new HashMap<>();
        map.put("name", "n");
        map.put("value", 5);
        CompositeDataSupport cdMap = new CompositeDataSupport(ct, map);
        check(cdMap.equals(cd), "map ctor equals array ctor");

        // equals / hashCode
        CompositeDataSupport cd2 = new CompositeDataSupport(ct, names, new Object[]{"n", 5});
        check(cd.equals(cd2), "equals");
        check(cd.hashCode() == cd2.hashCode(), "hashCode equal");
        check(!cd.equals(null), "equals null false");
        check(!cd.equals("x"), "equals non-CompositeData false");
        CompositeDataSupport cdDiff = new CompositeDataSupport(ct, names, new Object[]{"n", 6});
        check(!cd.equals(cdDiff), "different value not equal");
        CompositeType otherCt = new CompositeType("Other", "d", names, names, new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
        CompositeDataSupport cdOtherType = new CompositeDataSupport(otherCt, names, new Object[]{"n", 5});
        check(!cd.equals(cdOtherType), "different type not equal");

        // toString
        check(cd.toString().startsWith("javax.management.openmbean.CompositeDataSupport("), "toString prefix");
        check(cd.toString().contains("compositeType="), "toString compositeType");
        check(cd.toString().contains("contents="), "toString contents");
        check(cd.toString().contains("name=n"), "toString contains mapping");

        // exceptions (exact messages verified against JDK-27)
        checkThrows(InvalidKeyException.class, "Argument key=\"missing\" is not an existing item name for this CompositeData instance.",
            () -> cd.get("missing"), "get missing key");
        checkThrows(IllegalArgumentException.class, "Argument key cannot be a null or empty String.",
            () -> cd.get(null), "get null key");
        checkThrows(IllegalArgumentException.class, "Argument key cannot be a null or empty String.",
            () -> cd.get("  "), "get empty key");
        checkThrows(OpenDataException.class, "Argument value of wrong type for item value: value notint, type javax.management.openmbean.SimpleType(name=java.lang.Integer)",
            () -> { try { new CompositeDataSupport(ct, names, new Object[]{"n", "notint"}); } catch (Exception e) { throw sneaky(e); } },
            "wrong value type");
        checkThrows(OpenDataException.class, "Item names do not match CompositeType: names in items but not in CompositeType: [extra]; names in CompositeType but not in items: [value]",
            () -> { try { new CompositeDataSupport(ct, new String[]{"name", "extra"}, new Object[]{"n", 1}); } catch (Exception e) { throw sneaky(e); } },
            "missing/extra item names");
        checkThrows(IllegalArgumentException.class, "Argument compositeType cannot be null.",
            () -> { try { new CompositeDataSupport((CompositeType) null, names, new Object[]{"n", 5}); } catch (Exception e) { throw sneaky(e); } },
            "null compositeType");
    }

    // ---- deepened: Map ctor negatives / length mismatches / unmodifiable values / ser / impl quirk ----
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
        CompositeType ct = type();
        String[] names = {"name", "value"};
        CompositeDataSupport cd = new CompositeDataSupport(ct, names, new Object[]{"n", 5});

        // mismatched array lengths -> IllegalArgumentException (NOT OpenDataException)
        checkThrows(IllegalArgumentException.class, "Different lengths: itemNames[2], itemValues[1]",
            () -> { try { new CompositeDataSupport(ct, names, new Object[]{"x"}); } catch (Exception e) { throw sneaky(e); } },
            "mismatched array lengths");
        checkThrows(IllegalArgumentException.class, "Null itemNames or itemValues",
            () -> { try { new CompositeDataSupport(ct, (String[]) null, new Object[]{"x", 1}); } catch (Exception e) { throw sneaky(e); } },
            "null names array");
        checkThrows(IllegalArgumentException.class, "Null itemNames or itemValues",
            () -> { try { new CompositeDataSupport(ct, names, (Object[]) null); } catch (Exception e) { throw sneaky(e); } },
            "null values array");
        // null value for a typed (INTEGER) item is legal too
        CompositeDataSupport nullInt = new CompositeDataSupport(ct, names, new Object[]{"n", null});
        check(nullInt.get("value") == null, "null value for INTEGER item stored");
        // ctor argument order independence
        CompositeDataSupport reordered = new CompositeDataSupport(ct, new String[]{"value", "name"},
                new Object[]{5, "n"});
        check(reordered.equals(cd), "ctor argument order independent");
        check(reordered.hashCode() == cd.hashCode(), "hashCode order independent");
        // Map ctor negatives
        checkThrows(IllegalArgumentException.class, "Null or empty items map",
            () -> { try { new CompositeDataSupport(ct, (Map<String, ?>) null); } catch (Exception e) { throw sneaky(e); } },
            "null map");
        Map<String, Object> nullKeyMap = new HashMap<>();
        nullKeyMap.put("name", "n");
        nullKeyMap.put("value", 5);
        nullKeyMap.put(null, "v");
        checkThrows(IllegalArgumentException.class, "Null or empty item name",
            () -> { try { new CompositeDataSupport(ct, nullKeyMap); } catch (Exception e) { throw sneaky(e); } },
            "map with null key");
        Map<String, Object> missingMap = new HashMap<>();
        missingMap.put("name", "n");
        checkThrows(OpenDataException.class,
            "Item names do not match CompositeType: names in items but not in CompositeType: []; "
            + "names in CompositeType but not in items: [value]",
            () -> { try { new CompositeDataSupport(ct, missingMap); } catch (Exception e) { throw sneaky(e); } },
            "map missing item");
        Map<String, Object> wrongMap = new HashMap<>();
        wrongMap.put("name", "n");
        wrongMap.put("value", "str");
        checkThrows(OpenDataException.class,
            "Argument value of wrong type for item value: value str, "
            + "type javax.management.openmbean.SimpleType(name=java.lang.Integer)",
            () -> { try { new CompositeDataSupport(ct, wrongMap); } catch (Exception e) { throw sneaky(e); } },
            "map wrong value type");
        // values() unmodifiable
        try {
            java.util.Collection raw = cd.values();
            raw.add("z");
            check(false, "values must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        // getAll negatives
        checkThrows(InvalidKeyException.class,
            "Argument key=\"zz\" is not an existing item name for this CompositeData instance.",
            () -> cd.getAll(new String[]{"name", "zz"}), "getAll missing key");
        checkThrows(IllegalArgumentException.class, "Argument key cannot be a null or empty String.",
            () -> cd.getAll(new String[]{"name", null}), "getAll null key element");
        // serialization round-trips (with null value)
        CompositeDataSupport serNull = roundtrip(new CompositeDataSupport(ct, names, new Object[]{null, 1}));
        check(serNull.get("name") == null, "ser roundtrip keeps null value");
        check(roundtrip(cd).equals(cd), "ser roundtrip equals");
        // reference impl quirk: hand-made CompositeData with identical contents is NOT equal
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
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E { throw (E) t; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
