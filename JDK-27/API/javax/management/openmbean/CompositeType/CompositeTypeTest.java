/*
 * @test
 * @summary javax.management.openmbean.CompositeType conformance: white-box structure + black-box behavior.
 * @run main CompositeTypeTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class CompositeTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static List<String> sorted(Set<String> s) { List<String> l = new ArrayList<>(s); Collections.sort(l); return l; }
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

    static CompositeType make(String typeName) throws Exception {
        String[] names = {"name", "value"};
        String[] descs = {"name desc", "value desc"};
        OpenType<?>[] types = {SimpleType.STRING, SimpleType.INTEGER};
        return new CompositeType(typeName, "desc", names, descs, types);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeType");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(OpenType.class.isAssignableFrom(c), "extends OpenType");
        check(c.getMethod("containsKey", String.class) != null, "has containsKey");
        check(c.getMethod("getDescription", String.class) != null, "has getDescription");
        check(c.getMethod("getType", String.class) != null, "has getType");
        check(c.getMethod("keySet") != null, "has keySet");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CompositeType ct = make("MyType");
        check(ct.getTypeName().equals("MyType"), "typeName");
        check(ct.getDescription().equals("desc"), "description");
        check(ct.getClassName().equals(CompositeData.class.getName()), "className is CompositeData");
        check(!ct.isArray(), "not array");

        // keySet is ascending and unmodifiable
        Set<String> keys = ct.keySet();
        check(keys.size() == 2, "keySet size");
        check(sorted(keys).equals(Arrays.asList("name", "value")), "keySet sorted order");

        check(ct.containsKey("name"), "containsKey name");
        check(ct.containsKey("value"), "containsKey value");
        check(!ct.containsKey("missing"), "containsKey missing false");
        check(!ct.containsKey(null), "containsKey null false");

        check(ct.getType("name") == SimpleType.STRING, "getType name STRING");
        check(ct.getType("value") == SimpleType.INTEGER, "getType value INTEGER");
        check(ct.getType("missing") == null, "getType missing null");
        check(ct.getType(null) == null, "getType null null");

        check(ct.getDescription("name").equals("name desc"), "getDescription name");
        check(ct.getDescription("value").equals("value desc"), "getDescription value");
        check(ct.getDescription("missing") == null, "getDescription missing null");
        check(ct.getDescription(null) == null, "getDescription null null");

        // item names are trimmed when stored
        String[] names2 = {" name ", "value"};
        String[] descs2 = {"d1", "d2"};
        OpenType<?>[] types2 = {SimpleType.STRING, SimpleType.INTEGER};
        CompositeType trimmed = new CompositeType("T", "d", names2, descs2, types2);
        check(trimmed.containsKey("name"), "trimmed key lookup");
        check(!trimmed.containsKey(" name "), "no untrimmed key");

        // isValue via a CompositeData with the same type
        CompositeDataSupport cd = new CompositeDataSupport(ct, new String[]{"name", "value"}, new Object[]{"n", 5});
        check(ct.isValue(cd), "isValue same type");
        check(!ct.isValue(null), "isValue null false");
        check(!ct.isValue("x"), "isValue non-CompositeData false");
        // assignable: a value may have extra items not present in the type
        String[] subNames = {"name"};
        String[] subDescs = {"d"};
        OpenType<?>[] subTypes = {SimpleType.STRING};
        CompositeType sub = new CompositeType("MyType", "d", subNames, subDescs, subTypes);
        CompositeDataSupport subCd = new CompositeDataSupport(sub, subNames, new Object[]{"n"});
        check(sub.isValue(cd), "subset type accepts value with extra items");
        check(!ct.isValue(subCd), "full type rejects value missing an item");

        // equals / hashCode
        CompositeType ct2 = make("MyType");
        check(ct.equals(ct2), "equals independent");
        check(ct.hashCode() == ct2.hashCode(), "hashCode equal");
        check(!ct.equals(null), "equals null false");
        check(!ct.equals("x"), "equals non-CompositeType false");
        CompositeType otherName = make("OtherType");
        check(!ct.equals(otherName), "different typeName not equal");
        CompositeType otherItem = new CompositeType("MyType", "d", new String[]{"name"}, new String[]{"d"}, new OpenType[]{SimpleType.STRING});
        check(!ct.equals(otherItem), "different items not equal");

        // toString contains class name, typeName and items
        check(ct.toString().startsWith("javax.management.openmbean.CompositeType("), "toString prefix");
        check(ct.toString().contains("name=MyType"), "toString typeName");
        check(ct.toString().contains("itemName=name"), "toString item name");
        check(ct.toString().equals(ct.toString()), "toString stable");

        // exceptions (exact messages verified against JDK-27)
        checkThrows(OpenDataException.class, "Argument's element itemNames[1]=\"a\" duplicates a previous item names.",
            () -> { try { new CompositeType("T", "d", new String[]{"a", "a"}, new String[]{"x", "y"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}); } catch (Exception e) { throw sneaky(e); } },
            "duplicate item name");
        checkThrows(IllegalArgumentException.class, "Argument's element itemNames[0] cannot be an empty string.",
            () -> { try { new CompositeType("T", "d", new String[]{"", "b"}, new String[]{"x", "y"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}); } catch (Exception e) { throw sneaky(e); } },
            "empty item name");
        checkThrows(IllegalArgumentException.class, "Array arguments itemNames[], itemDescriptions[] and itemTypes[] should be of same length (got 1, 1 and 2).",
            () -> { try { new CompositeType("T", "d", new String[]{"a"}, new String[]{"x"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}); } catch (Exception e) { throw sneaky(e); } },
            "mismatched array lengths");
        checkThrows(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
            () -> { try { new CompositeType(null, "d", new String[]{"a"}, new String[]{"x"}, new OpenType[]{SimpleType.STRING}); }
                catch (Exception e) { throw sneaky(e); } }, "null typeName");
    }

    // ---- deepened: unmodifiable keySet / full negative matrix / order-independence / ser ----
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
        CompositeType ct = make("MyType");
        // keySet unmodifiable
        try {
            ct.keySet().add("z");
            check(false, "keySet must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        // order-independent item set: same set of names in different order equals
        CompositeType reordered = new CompositeType("MyType", "d",
                new String[]{"value", "name"}, new String[]{"v d", "n d"},
                new OpenType[]{SimpleType.INTEGER, SimpleType.STRING});
        // different descriptions though -> not equal; construct matching descriptions
        CompositeType reorderedMatch = new CompositeType("MyType", "desc",
                new String[]{"value", "name"}, new String[]{"value desc", "name desc"},
                new OpenType[]{SimpleType.INTEGER, SimpleType.STRING});
        // descriptions do NOT participate in equals (verified both JDKs)
        check(ct.equals(reordered), "item descriptions do not participate in equals");
        check(ct.equals(reorderedMatch), "equals with reordered item names");
        check(ct.hashCode() == reorderedMatch.hashCode(), "hashCode equal with reordered items");
        check(ct.equals(new CompositeType("MyType", "other desc",
                new String[]{"name", "value"}, new String[]{"name desc", "value desc"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER})),
                "type-level description does not participate in equals");
        // isValue accepts null item values
        CompositeDataSupport cdNull = new CompositeDataSupport(ct,
                new String[]{"name", "value"}, new Object[]{null, null});
        check(ct.isValue(cdNull), "isValue accepts null item values");
        check(!ct.isValue(new CompositeDataSupport(
                new CompositeType("MyType", "d", new String[]{"name", "value"},
                        new String[]{"d", "d"}, new OpenType[]{SimpleType.STRING, SimpleType.STRING}),
                new String[]{"name", "value"}, new Object[]{"n", "v"})), "rejects wrong item type");
        check(!ct.isValue(new CompositeDataSupport(
                new CompositeType("Other", "d", new String[]{"name", "value"},
                        new String[]{"d", "d"}, new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}),
                new String[]{"name", "value"}, new Object[]{"n", 5})), "rejects different typeName");
        // toString exact
        check(ct.toString().equals("javax.management.openmbean.CompositeType(name=MyType,"
                + "items=((itemName=name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),"
                + "(itemName=value,itemType=javax.management.openmbean.SimpleType(name=java.lang.Integer))))"),
                "toString exact");
        // serialization round-trip
        check(roundtrip(ct).equals(ct), "ser roundtrip equals");
        check(roundtrip(ct).hashCode() == ct.hashCode(), "ser roundtrip hashCode");

        // full negative matrix (messages verified on this JDK)
        checkThrows(IllegalArgumentException.class, "Argument itemNames[] cannot be null or empty.",
            () -> { try { new CompositeType("T", "d", (String[]) null, new String[]{"x"},
                new OpenType[]{SimpleType.STRING}); } catch (Exception e) { throw sneaky(e); } },
            "null itemNames array");
        checkThrows(IllegalArgumentException.class, "Argument itemNames[] cannot be null or empty.",
            () -> { try { new CompositeType("T", "d", new String[0], new String[0], new OpenType[0]); }
                catch (Exception e) { throw sneaky(e); } },
            "empty itemNames array (zero-item type not allowed)");
        checkThrows(IllegalArgumentException.class, "Argument's element itemNames[0] cannot be null.",
            () -> { try { new CompositeType("T", "d", new String[]{null}, new String[]{"x"},
                new OpenType[]{SimpleType.STRING}); } catch (Exception e) { throw sneaky(e); } },
            "null item name element");
        checkThrows(IllegalArgumentException.class, "Argument's element itemDescriptions[0] cannot be null.",
            () -> { try { new CompositeType("T", "d", new String[]{"a"}, new String[]{null},
                new OpenType[]{SimpleType.STRING}); } catch (Exception e) { throw sneaky(e); } },
            "null item description element");
        checkThrows(IllegalArgumentException.class, "Argument's element itemTypes[0] cannot be null.",
            () -> { try { new CompositeType("T", "d", new String[]{"a"}, new String[]{"x"},
                new OpenType[]{null}); } catch (Exception e) { throw sneaky(e); } },
            "null item type element");
        checkThrows(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
            () -> { try { new CompositeType("", "d", new String[]{"a"}, new String[]{"x"},
                new OpenType[]{SimpleType.STRING}); } catch (Exception e) { throw sneaky(e); } },
            "empty typeName");
        checkThrows(IllegalArgumentException.class, "Argument description cannot be null or empty",
            () -> { try { new CompositeType("T", null, new String[]{"a"}, new String[]{"x"},
                new OpenType[]{SimpleType.STRING}); } catch (Exception e) { throw sneaky(e); } },
            "null description");
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
