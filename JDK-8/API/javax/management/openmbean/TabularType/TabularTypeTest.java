/*
 * @test
 * @summary javax.management.openmbean.TabularType conformance: row type and index names.
 * @run main TabularTypeTest
 */

import javax.management.openmbean.*;
public class TabularTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType rowType = new CompositeType("row", "r", new String[]{"a"}, new String[]{"d"}, new OpenType[]{SimpleType.STRING});
        TabularType tt = new TabularType("tab", "t", rowType, new String[]{"a"});
        check("tab".equals(tt.getTypeName()), "type name");
        check(tt.getRowType() == rowType, "row type");
        check(tt.getIndexNames().contains("a"), "index names");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: negative matrix / unmodifiable indexNames / equals with rowType / ser ----
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

    static CompositeType rowType() throws Exception {
        return new CompositeType("Row", "row desc",
                new String[]{"name", "value"},
                new String[]{"name desc", "value desc"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
    }

    static void testDeep() throws Exception {
        // white-box
        Class<?> c = TabularType.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "white-box public");
        check(OpenType.class.isAssignableFrom(c), "white-box extends OpenType");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getMethod("getRowType") != null, "has getRowType");
        check(c.getMethod("getIndexNames") != null, "has getIndexNames");

        CompositeType rt = rowType();
        TabularType tt = new TabularType("MyTab", "desc", rt, new String[]{"name"});
        check(tt.getTypeName().equals("MyTab"), "typeName");
        check(tt.getDescription().equals("desc"), "description");
        check(tt.getClassName().equals(TabularData.class.getName()), "className is TabularData");
        check(!tt.isArray(), "not array");
        check(tt.getRowType().equals(rt), "rowType equals");
        // index names preserve the given order (not sorted)
        check(tt.getIndexNames().equals(java.util.Arrays.asList("name")), "indexNames content");
        TabularType multi = new TabularType("M", "d", rt, new String[]{"value", "name"});
        check(multi.getIndexNames().equals(java.util.Arrays.asList("value", "name")),
                "multi index order preserved");
        // getIndexNames is unmodifiable
        try {
            tt.getIndexNames().add("value");
            check(false, "getIndexNames must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }

        // isValue
        TabularDataSupport td = new TabularDataSupport(tt);
        check(tt.isValue(td), "isValue same type");
        check(!tt.isValue(null), "isValue null false");
        check(!tt.isValue("x"), "isValue non-TabularData false");
        check(new TabularType("MyTab", "desc",
                new CompositeType("Row", "row desc",
                        new String[]{"name", "value"}, new String[]{"name desc", "value desc"},
                        new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}),
                new String[]{"name"}).isValue(td), "isValue structurally equal type");

        // equals / hashCode: typeName, indexNames, rowType all participate
        TabularType tt2 = new TabularType("MyTab", "desc",
                new CompositeType("Row", "row desc",
                        new String[]{"name", "value"}, new String[]{"name desc", "value desc"},
                        new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}),
                new String[]{"name"});
        check(tt.equals(tt2), "equals with structurally equal rowType");
        check(tt.hashCode() == tt2.hashCode(), "hashCode equal with structural rowType");
        check(!tt.equals(new TabularType("Other", "desc", rt, new String[]{"name"})), "different typeName not equal");
        check(!tt.equals(new TabularType("MyTab", "desc", rt, new String[]{"value"})), "different indexNames not equal");
        check(!tt.equals(new TabularType("MyTab", "desc",
                new CompositeType("OtherRow", "d", new String[]{"name"}, new String[]{"d"},
                        new OpenType[]{SimpleType.STRING}), new String[]{"name"})),
                "different rowType not equal");
        check(!tt.equals(null), "equals null false");
        check(!tt.equals("x"), "equals non-TabularType false");

        // toString exact
        check(tt.toString().equals("javax.management.openmbean.TabularType(name=MyTab,"
                + "rowType=javax.management.openmbean.CompositeType(name=Row,"
                + "items=((itemName=name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),"
                + "(itemName=value,itemType=javax.management.openmbean.SimpleType(name=java.lang.Integer)))),"
                + "indexNames=(name))"), "toString exact");

        // serialization round-trip
        check(roundtrip(tt).equals(tt), "ser roundtrip equals");
        check(roundtrip(tt).hashCode() == tt.hashCode(), "ser roundtrip hashCode");

        // negative matrix: exact types and messages (verified on this JDK)
        checkThrowsExact(OpenDataException.class,
                "Argument's element value indexNames[0]=\"nope\" is not a valid item name for rowType.",
                () -> new TabularType("T", "d", rt, new String[]{"nope"}), "index name not in rowType");
        checkThrowsExact(IllegalArgumentException.class, "Argument rowType cannot be null.",
                () -> new TabularType("T", "d", null, new String[]{"name"}), "null rowType");
        checkThrowsExact(IllegalArgumentException.class, "Argument indexNames[] cannot be null or empty.",
                () -> new TabularType("T", "d", rt, null), "null indexNames array");
        checkThrowsExact(IllegalArgumentException.class, "Argument indexNames[] cannot be null or empty.",
                () -> new TabularType("T", "d", rt, new String[0]), "empty indexNames array");
        checkThrowsExact(IllegalArgumentException.class,
                "Argument's element indexNames[0] cannot be null.",
                () -> new TabularType("T", "d", rt, new String[]{null}), "null index name element");
        checkThrowsExact(IllegalArgumentException.class,
                "Argument's element indexNames[0] cannot be an empty string.",
                () -> new TabularType("T", "d", rt, new String[]{""}), "empty index name element");
        checkThrowsExact(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
                () -> new TabularType(null, "d", rt, new String[]{"name"}), "null typeName");
        checkThrowsExact(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
                () -> new TabularType("", "d", rt, new String[]{"name"}), "empty typeName");
        checkThrowsExact(IllegalArgumentException.class, "Argument description cannot be null or empty",
                () -> new TabularType("T", null, rt, new String[]{"name"}), "null description");
        // duplicate index names are NOT rejected (verified: allowed, list keeps duplicates)
        TabularType dup = new TabularType("T", "d", rt, new String[]{"name", "name"});
        check(dup.getIndexNames().equals(java.util.Arrays.asList("name", "name")),
                "duplicate index names allowed");
    }
}
