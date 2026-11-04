/*
 * @test
 * @summary javax.management.openmbean.TabularDataSupport conformance: white-box structure + black-box behavior.
 * @run main TabularDataSupportTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.InvalidOpenTypeException;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class TabularDataSupportTest {
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

    static CompositeType rowType() throws Exception {
        return new CompositeType("Row", "row desc",
            new String[]{"name", "value"},
            new String[]{"name desc", "value desc"},
            new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
    }
    static CompositeDataSupport row(CompositeType rt, String name, int value) throws Exception {
        return new CompositeDataSupport(rt, new String[]{"name", "value"}, new Object[]{name, value});
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.TabularDataSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(TabularData.class.isAssignableFrom(c), "implements TabularData");
        check(Map.class.isAssignableFrom(c), "implements Map");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getMethod("calculateIndex", CompositeData.class) != null, "has calculateIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CompositeType rt = rowType();
        TabularType tt = new TabularType("MyTab", "desc", rt, new String[]{"name"});
        TabularDataSupport td = new TabularDataSupport(tt);

        check(td.getTabularType().equals(tt), "getTabularType");
        check(td.size() == 0, "size 0");
        check(td.isEmpty(), "isEmpty initially");

        CompositeDataSupport r1 = row(rt, "n", 5);
        Object[] idx = td.calculateIndex(r1);
        check(idx.length == 1 && idx[0].equals("n"), "calculateIndex");

        td.put(r1);
        check(td.size() == 1, "size 1 after put");
        check(!td.isEmpty(), "not empty after put");
        check(td.containsKey(idx), "containsKey Object[]");
        check(td.containsValue(r1), "containsValue CompositeData");
        check(td.get(idx).equals(r1), "get Object[]");
        check(td.get((Object) idx) instanceof CompositeData, "get Object");

        // keySet / values / entrySet views
        Set<Object> keys = td.keySet();
        check(keys.size() == 1, "keySet size");
        java.util.Collection<Object> vals = td.values();
        check(vals.size() == 1 && vals.contains(r1), "values");
        Set<Map.Entry<Object, Object>> entries = td.entrySet();
        check(entries.size() == 1, "entrySet size");

        // calculateIndex does not check for duplicate index; put with same index throws KeyAlreadyExistsException
        CompositeDataSupport r1dup = row(rt, "n", 6);
        checkThrows(KeyAlreadyExistsException.class, "Argument value's index, calculated according to this TabularData instance's tabularType, already refers to a value in this table.",
            () -> td.put(r1dup), "put duplicate index");

        // put a second row with a different index
        CompositeDataSupport r2 = row(rt, "m", 7);
        td.put(r2);
        check(td.size() == 2, "size 2");

        // Map-style put(Object,Object) ignores key and uses calculated index
        td.put(new Object(), row(rt, "p", 9));
        check(td.size() == 3, "size 3 after Map.put");

        // remove by Object[] returns the removed row
        Object[] idx2 = td.calculateIndex(row(rt, "m", 7));
        CompositeData removed = td.remove(idx2);
        check(removed.equals(r2), "remove returns row");
        check(td.size() == 2, "size after remove");
        check(!td.containsKey(idx2), "no longer contains removed key");
        check(td.remove(idx2) == null, "remove missing returns null");

        // clear
        td.clear();
        check(td.isEmpty(), "empty after clear");
        check(td.size() == 0, "size 0 after clear");

        // putAll(CompositeData[]) bulk add
        td.putAll(new CompositeData[]{row(rt, "a", 1), row(rt, "b", 2)});
        check(td.size() == 2, "putAll size");

        // equals / clone
        TabularDataSupport td2 = new TabularDataSupport(tt);
        td2.putAll(new CompositeData[]{row(rt, "a", 1), row(rt, "b", 2)});
        check(td.equals(td2), "equals");
        check(td.hashCode() == td2.hashCode(), "hashCode equal");
        check(!td.equals(null), "equals null false");
        check(!td.equals("x"), "equals non-TabularData false");
        Object clone = td.clone();
        check(clone instanceof TabularDataSupport, "clone type");
        check(td.equals(clone), "clone equals");
        ((TabularDataSupport) clone).clear();
        check(td.size() == 2, "clone independent");

        // exceptions (exact messages verified against JDK-27)
        checkThrows(InvalidOpenTypeException.class, null,
            () -> { try { td.calculateIndex(new CompositeDataSupport(
                new CompositeType("Other", "d", new String[]{"x"}, new String[]{"dx"}, new OpenType[]{SimpleType.STRING}),
                new String[]{"x"}, new Object[]{"v"})); } catch (Exception e) { throw sneaky(e); } },
            "calculateIndex wrong row type");
        checkThrows(InvalidKeyException.class, "Argument key's length=2 is different from the number of item values, which is 1, specified for the indexing rows in this TabularData instance.",
            () -> td.get(new Object[]{"a", "b"}), "get bad key length");
        checkThrows(NullPointerException.class, "Argument value cannot be null.",
            () -> td.put((CompositeData) null), "put null value");
        checkThrows(NullPointerException.class, "Argument key cannot be null or empty.",
            () -> td.get((Object[]) null), "get null key");
    }

    // ---- deepened: ctor negatives / null-type table / exact messages / Map view / ser / clear ----
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
        Class<?> c = TabularDataSupport.class;
        // javap verified (JDK-8 == JDK-27): no no-arg ctor, no 3-arg convenience put
        for (java.lang.reflect.Constructor<?> ctor : c.getConstructors())
            check(ctor.getParameterTypes().length != 0, "no no-arg ctor exposed");
        boolean has3ArgPut = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if ("put".equals(m.getName()) && m.getParameterTypes().length == 3) has3ArgPut = true;
        check(!has3ArgPut, "no put(CompositeType,String[],Object[]) convenience");

        CompositeType rt = rowType();
        TabularType tt = new TabularType("MyTab", "desc", rt, new String[]{"name"});

        // capacity/loadFactor ctor follows HashMap validation
        check(new TabularDataSupport(tt, 16, 0.75f).getTabularType().equals(tt), "sized ctor works");
        checkThrows(IllegalArgumentException.class, "Illegal initial capacity: -1",
            () -> { try { new TabularDataSupport(tt, -1, 0.75f); } catch (Exception e) { throw sneaky(e); } },
            "negative capacity");
        checkThrows(IllegalArgumentException.class, "Illegal load factor: -1.0",
            () -> { try { new TabularDataSupport(tt, 16, -1f); } catch (Exception e) { throw sneaky(e); } },
            "negative load factor");
        // null-type table is rejected at construction time (verified both JDKs)
        checkThrows(IllegalArgumentException.class, "Argument tabularType cannot be null.",
            () -> { try { new TabularDataSupport(null); } catch (Exception e) { throw sneaky(e); } },
            "null type ctor");

        // lifecycle with exact messages
        TabularDataSupport td = new TabularDataSupport(tt);
        CompositeDataSupport r1 = row(rt, "n", 5);
        td.put(r1);
        checkThrows(InvalidOpenTypeException.class,
                "Argument value's composite type [" + new CompositeType("Other", "d", new String[]{"x"},
                    new String[]{"dx"}, new OpenType[]{SimpleType.STRING}) + "] is not assignable to this "
                + "TabularData instance's row type [" + rt + "].",
            () -> { try { td.put(new CompositeDataSupport(new CompositeType("Other", "d", new String[]{"x"},
                    new String[]{"dx"}, new OpenType[]{SimpleType.STRING}), new String[]{"x"}, new Object[]{"v"})); }
                catch (Exception e) { throw sneaky(e); } },
            "put wrong row type exact message");
        checkThrows(NullPointerException.class, "Argument value cannot be null.",
            () -> { try { td.calculateIndex(null); } catch (Exception e) { throw sneaky(e); } },
            "calculateIndex null");
        checkThrows(NullPointerException.class, "Argument key cannot be null or empty.",
            () -> { try { td.get(new Object[0]); } catch (Exception e) { throw sneaky(e); } },
            "get empty key");
        checkThrows(InvalidKeyException.class,
                "Argument element key[0] is not a value for the open type expected for this element "
                + "of the index, whose name is \"name\" and whose open type is "
                + "javax.management.openmbean.SimpleType(name=java.lang.String)",
            () -> { try { td.get(new Object[]{Integer.valueOf(5)}); } catch (Exception e) { throw sneaky(e); } },
            "get wrong key element type");

        // putAll duplicate indexes: distinct exact message
        td.putAll(new CompositeData[]{row(rt, "a", 1), row(rt, "b", 2)});
        check(td.size() == 3, "putAll adds rows");
        checkThrows(KeyAlreadyExistsException.class,
                "Argument elements values[1] and values[0] have the same indexes, calculated according "
                + "to this TabularData instance's tabularType.",
            () -> { try { td.putAll(new CompositeData[]{row(rt, "c", 1), row(rt, "c", 2)}); }
                catch (Exception e) { throw sneaky(e); } },
            "putAll duplicate indexes");
        // multi-index order
        TabularType tt2 = new TabularType("T2", "d", rt, new String[]{"value", "name"});
        check(java.util.Arrays.equals(new TabularDataSupport(tt2).calculateIndex(r1), new Object[]{5, "n"}),
                "multi-index order follows indexNames");

        // Map view details: keys are index lists; Map.put returns the row; duplicate still rejected
        check(td.keySet().contains(java.util.Arrays.asList("n")), "keySet holds index lists");
        try {
            td.keySet().add(new Object());
            check(false, "keySet must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        boolean foundEntry = false;
        for (Map.Entry<Object, Object> e : td.entrySet())
            if (java.util.Arrays.asList("n").equals(e.getKey()) && r1.equals(e.getValue())) foundEntry = true;
        check(foundEntry, "entrySet maps index list to row");
        Object ret = td.put(new Object(), row(rt, "p", 9));
        check(ret.equals(row(rt, "p", 9)), "Map.put returns the put row");
        check(td.containsKey(new Object[]{"p"}), "Map.put indexed the row");
        checkThrows(KeyAlreadyExistsException.class,
                "Argument value's index, calculated according to this TabularData instance's tabularType, "
                + "already refers to a value in this table.",
            () -> { try { td.put(new Object(), row(rt, "p", 10)); } catch (Exception e) { throw sneaky(e); } },
            "Map.put duplicate index");
        Map<Object, Object> bulk = new java.util.HashMap<Object, Object>();
        bulk.put("ignored", row(rt, "q", 3));
        td.putAll(bulk);
        check(td.containsKey(new Object[]{"q"}), "Map.putAll indexes rows");
        // Map.remove/get require an Object[] key: non-array keys fail with ClassCastException (type-only:
        // message wording differs across JDKs)
        checkThrows(ClassCastException.class, null,
            () -> { try { td.remove((Object) java.util.Arrays.asList("q")); } catch (Exception e) { throw sneaky(e); } },
            "Map.remove(List) fails");
        checkThrows(ClassCastException.class, null,
            () -> { try { td.get((Object) "notakey"); } catch (Exception e) { throw sneaky(e); } },
            "Map.get(non-array) fails");
        // containsKey(Object) with non-array key returns false (CCE swallowed), unlike get/remove
        check(!td.containsKey((Object) "notakey"), "Map.containsKey(non-array) false without exception");
        check(!td.containsValue((Object) "notarow"), "containsValue(Object) non-row false");

        // live views shrink on removal
        int before = td.values().size();
        td.remove(new Object[]{"q"});
        check(td.values().size() == before - 1, "values is a live view");

        // serialization round-trip keeps type and rows
        TabularDataSupport ser = roundtrip(td);
        check(ser.equals(td), "ser roundtrip equals");
        check(ser.size() == td.size(), "ser roundtrip keeps rows");
        check(ser.getTabularType().equals(tt), "ser roundtrip keeps type");

        // clear then verify every key gone
        td.clear();
        check(td.isEmpty() && td.size() == 0, "clear empties table");
        check(!td.containsKey(new Object[]{"n"}), "cleared key n");
        check(!td.containsKey(new Object[]{"p"}), "cleared key p");
        check(td.keySet().isEmpty() && td.values().isEmpty() && td.entrySet().isEmpty(),
                "views all empty after clear");
        check(td.get(new Object[]{"n"}) == null, "get after clear null");
        // toString
        TabularDataSupport oneRow = new TabularDataSupport(tt);
        oneRow.put(row(rt, "z", 42));
        check(oneRow.toString().startsWith("javax.management.openmbean.TabularDataSupport(tabularType="),
                "toString prefix");
        check(oneRow.toString().contains("name=z"), "toString shows row contents");
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
