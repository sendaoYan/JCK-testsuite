/*
 * @test
 * @summary javax.management.openmbean.TabularDataSupport conformance: put/get by index.
 * @run main TabularDataSupportTest
 */

import javax.management.openmbean.*;
public class TabularDataSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType rowType = new CompositeType("row", "r", new String[]{"a"}, new String[]{"d"}, new OpenType[]{SimpleType.STRING});
        TabularType tt = new TabularType("tab", "t", rowType, new String[]{"a"});
        TabularDataSupport tds = new TabularDataSupport(tt);
        CompositeDataSupport row = new CompositeDataSupport(rowType, new String[]{"a"}, new Object[]{"x"});
        tds.put(row);
        check(tds.size() == 1, "size");
        check(tds.containsKey(new Object[]{"x"}), "containsKey");
        check("x".equals(tds.get(new Object[]{"x"}).get("a")), "get by index");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: full lifecycle / KeyAlreadyExists / Map view / null-type table / ser ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

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

    static CompositeType rowType() throws Exception {
        return new CompositeType("Row", "row desc",
                new String[]{"name", "value"},
                new String[]{"name desc", "value desc"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
    }

    static CompositeDataSupport row(CompositeType rt, String name, int value) throws Exception {
        return new CompositeDataSupport(rt, new String[]{"name", "value"}, new Object[]{name, Integer.valueOf(value)});
    }

    static void testDeep() throws Exception {
        // white-box
        Class<?> c = TabularDataSupport.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "white-box public");
        check(TabularData.class.isAssignableFrom(c), "implements TabularData");
        check(java.util.Map.class.isAssignableFrom(c), "implements Map");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getMethod("calculateIndex", CompositeData.class) != null, "has calculateIndex");
        // javap verified (JDK-8 == JDK-27): no no-arg ctor, no 3-arg convenience put
        for (java.lang.reflect.Constructor<?> ctor : c.getConstructors())
            check(ctor.getParameterTypes().length != 0, "no no-arg ctor exposed");
        boolean has3ArgPut = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if ("put".equals(m.getName()) && m.getParameterTypes().length == 3) has3ArgPut = true;
        check(!has3ArgPut, "no put(CompositeType,String[],Object[]) convenience");

        CompositeType rt = rowType();
        TabularType tt = new TabularType("MyTab", "desc", rt, new String[]{"name"});
        TabularDataSupport td = new TabularDataSupport(tt);
        check(td.getTabularType().equals(tt), "getTabularType");
        check(td.size() == 0 && td.isEmpty(), "initially empty");

        // capacity/loadFactor ctor follows HashMap argument validation
        TabularDataSupport sized = new TabularDataSupport(tt, 16, 0.75f);
        check(sized.getTabularType().equals(tt), "sized ctor works");
        checkThrowsExact(IllegalArgumentException.class, "Illegal initial capacity: -1",
                () -> new TabularDataSupport(tt, -1, 0.75f), "negative capacity");
        checkThrowsExact(IllegalArgumentException.class, "Illegal load factor: -1.0",
                () -> new TabularDataSupport(tt, 16, -1f), "negative load factor");

        // null-type table is rejected at construction time (verified both JDKs)
        checkThrowsExact(IllegalArgumentException.class, "Argument tabularType cannot be null.",
                () -> new TabularDataSupport(null), "null type ctor");

        // put lifecycle + calculateIndex
        CompositeDataSupport r1 = row(rt, "n", 5);
        Object[] idx = td.calculateIndex(r1);
        check(idx.length == 1 && idx[0].equals("n"), "calculateIndex values follow indexNames");
        TabularType tt2 = new TabularType("T2", "d", rt, new String[]{"value", "name"});
        check(java.util.Arrays.equals(new TabularDataSupport(tt2).calculateIndex(r1), new Object[]{5, "n"}),
                "multi-index order follows indexNames");
        td.put(r1);
        check(td.size() == 1 && !td.isEmpty(), "size 1 after put");
        check(td.containsKey(idx), "containsKey Object[]");
        check(td.containsValue(r1), "containsValue row");
        check(td.get(idx).equals(r1), "get Object[]");
        check(td.get((Object) idx).equals(r1), "Map.get(Object) with index array");
        check(td.containsKey("zz") == false, "containsKey(String) non-index key false");
        check(td.get(new Object[]{"zz"}) == null, "get missing returns null");
        check(td.remove(new Object[]{"zz"}) == null, "remove missing returns null");
        check(!td.containsValue((Object) "notarow"), "containsValue(Object) non-row false");

        // core negative: duplicate index -> KeyAlreadyExistsException
        checkThrowsExact(KeyAlreadyExistsException.class,
                "Argument value's index, calculated according to this TabularData instance's tabularType, "
                + "already refers to a value in this table.",
                () -> td.put(row(rt, "n", 6)), "put duplicate index");
        check(td.size() == 1, "failed put leaves table unchanged");
        // null value -> NPE
        checkThrowsExact(NullPointerException.class, "Argument value cannot be null.",
                () -> td.put((CompositeData) null), "put null value");
        // wrong row type -> InvalidOpenTypeException with both types spelled out
        CompositeType other = new CompositeType("Other", "d", new String[]{"x"},
                new String[]{"dx"}, new OpenType[]{SimpleType.STRING});
        CompositeData otherRow = new CompositeDataSupport(other, new String[]{"x"}, new Object[]{"v"});
        checkThrowsExact(InvalidOpenTypeException.class,
                "Argument value's composite type [" + other + "] is not assignable to this TabularData "
                + "instance's row type [" + rt + "].",
                () -> td.put(otherRow), "put wrong row type");
        checkThrowsExact(InvalidOpenTypeException.class,
                "Argument value's composite type [" + other + "] is not assignable to this TabularData "
                + "instance's row type [" + rt + "].",
                () -> td.calculateIndex(otherRow), "calculateIndex wrong row type");
        checkThrowsExact(NullPointerException.class, "Argument value cannot be null.",
                () -> td.calculateIndex(null), "calculateIndex null");

        // key validation
        checkThrowsExact(InvalidKeyException.class,
                "Argument key's length=2 is different from the number of item values, which is 1, "
                + "specified for the indexing rows in this TabularData instance.",
                () -> td.get(new Object[]{"a", "b"}), "get wrong key length");
        checkThrowsExact(InvalidKeyException.class,
                "Argument element key[0] is not a value for the open type expected for this element "
                + "of the index, whose name is \"name\" and whose open type is "
                + "javax.management.openmbean.SimpleType(name=java.lang.String)",
                () -> td.get(new Object[]{Integer.valueOf(5)}), "get wrong key element type");
        checkThrowsExact(NullPointerException.class, "Argument key cannot be null or empty.",
                () -> td.get((Object[]) null), "get null key");
        checkThrowsExact(NullPointerException.class, "Argument key cannot be null or empty.",
                () -> td.get(new Object[0]), "get empty key");

        // second row + remove semantics
        CompositeDataSupport r2 = row(rt, "m", 7);
        td.put(r2);
        check(td.size() == 2, "size 2");
        Object[] idx2 = td.calculateIndex(r2);
        check(td.remove(idx2).equals(r2), "remove returns the row");
        check(td.size() == 1 && !td.containsKey(idx2), "remove updates size and keySet");

        // putAll(CompositeData[])
        td.putAll(new CompositeData[]{row(rt, "a", 1), row(rt, "b", 2)});
        check(td.size() == 3, "putAll adds rows");
        checkThrowsExact(KeyAlreadyExistsException.class,
                "Argument elements values[1] and values[0] have the same indexes, calculated according "
                + "to this TabularData instance's tabularType.",
                () -> td.putAll(new CompositeData[]{row(rt, "c", 1), row(rt, "c", 2)}),
                "putAll duplicate indexes");

        // Map view: keys are the calculated indexes wrapped as unmodifiable Lists
        java.util.Set<Object> keySet = td.keySet();
        check(keySet.size() == 3, "keySet size");
        check(keySet.contains(java.util.Arrays.asList("n")), "keySet holds index lists");
        try {
            keySet.add(new Object());
            check(false, "keySet must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        java.util.Collection<Object> values = td.values();
        check(values.size() == 3, "values size");
        check(values.contains(r1) && values.containsAll(
                java.util.Arrays.<Object>asList(row(rt, "a", 1), row(rt, "b", 2))), "values holds rows");
        java.util.Set<java.util.Map.Entry<Object, Object>> entries = td.entrySet();
        check(entries.size() == 3, "entrySet size");
        boolean foundEntry = false;
        for (java.util.Map.Entry<Object, Object> e : entries)
            if (java.util.Arrays.asList("n").equals(e.getKey()) && r1.equals(e.getValue())) foundEntry = true;
        check(foundEntry, "entrySet maps index list to row");
        // Map.put ignores the key, returns the row, duplicate index still rejected
        Object ret = td.put(new Object(), row(rt, "p", 9));
        check(ret.equals(row(rt, "p", 9)), "Map.put returns the put row");
        check(td.containsKey(new Object[]{"p"}), "Map.put indexed the row");
        checkThrowsExact(KeyAlreadyExistsException.class,
                "Argument value's index, calculated according to this TabularData instance's tabularType, "
                + "already refers to a value in this table.",
                () -> td.put(new Object(), row(rt, "p", 10)), "Map.put duplicate index");
        // Map.putAll(Map) accepts rows as values
        java.util.Map<Object, Object> bulk = new java.util.HashMap<Object, Object>();
        bulk.put("ignored", row(rt, "q", 3));
        td.putAll(bulk);
        check(td.containsKey(new Object[]{"q"}), "Map.putAll indexes rows");
        // Map.remove requires an Object[] key: a List key fails with ClassCastException (verified on both JDKs)
        checkThrows(ClassCastException.class,
                () -> td.remove((Object) java.util.Arrays.asList("q")), "Map.remove(List) fails");
        checkThrows(ClassCastException.class,
                () -> td.get((Object) "notakey"), "Map.get(non-array) fails");

        // live views: removal shrinks them
        int valuesSizeBefore = td.values().size();
        td.remove(new Object[]{"q"});
        check(td.values().size() == valuesSizeBefore - 1, "values is a live view");

        // equals / hashCode / clone
        TabularDataSupport td2 = new TabularDataSupport(tt);
        td2.putAll(new CompositeData[]{row(rt, "n", 5), row(rt, "a", 1), row(rt, "b", 2), row(rt, "p", 9)});
        check(td.equals(td2), "equals same content");
        check(td.hashCode() == td2.hashCode(), "hashCode equal");
        check(!td.equals(null), "equals null false");
        check(!td.equals("x"), "equals non-table false");
        Object clone = td.clone();
        check(clone instanceof TabularDataSupport && td.equals(clone), "clone equals original");
        ((TabularDataSupport) clone).clear();
        check(td.size() == 4, "clone structurally independent");

        // serialization round-trip keeps type and rows
        TabularDataSupport ser = roundtrip(td);
        check(ser.equals(td), "ser roundtrip equals");
        check(ser.size() == 4, "ser roundtrip keeps rows");
        check(ser.getTabularType().equals(tt), "ser roundtrip keeps type");

        // clear then verify every key is gone
        td.clear();
        check(td.isEmpty() && td.size() == 0, "clear empties table");
        check(td.containsKey(new Object[]{"n"}) == false, "cleared key n");
        check(td.containsKey(new Object[]{"a"}) == false, "cleared key a");
        check(td.containsKey(new Object[]{"b"}) == false, "cleared key b");
        check(td.containsKey(new Object[]{"p"}) == false, "cleared key p");
        check(td.keySet().isEmpty() && td.values().isEmpty() && td.entrySet().isEmpty(),
                "views all empty after clear");
        check(td.get(new Object[]{"n"}) == null, "get after clear null");

        // toString non-empty with type and contents
        TabularDataSupport oneRow = new TabularDataSupport(tt);
        oneRow.put(row(rt, "z", 42));
        check(oneRow.toString().startsWith("javax.management.openmbean.TabularDataSupport(tabularType="),
                "toString prefix");
        check(oneRow.toString().contains("name=z"), "toString shows row contents");
    }
}
