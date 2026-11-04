/*
 * @test
 * @summary javax.management.openmbean.TabularType conformance: white-box structure + black-box behavior.
 * @run main TabularTypeTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class TabularTypeTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.TabularType");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(OpenType.class.isAssignableFrom(c), "extends OpenType");
        check(c.getMethod("getRowType") != null, "has getRowType");
        check(c.getMethod("getIndexNames") != null, "has getIndexNames");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CompositeType rt = rowType();
        TabularType tt = new TabularType("MyTab", "desc", rt, new String[]{"name"});
        check(tt.getTypeName().equals("MyTab"), "typeName");
        check(tt.getDescription().equals("desc"), "description");
        check(tt.getClassName().equals(TabularData.class.getName()), "className TabularData");
        check(!tt.isArray(), "not array");
        check(tt.getRowType() == rt, "rowType identity");
        check(tt.getRowType().equals(rt), "rowType equals");

        // index names list preserves given order and is unmodifiable
        List<String> indexNames = tt.getIndexNames();
        check(indexNames.size() == 1, "indexNames size");
        check(indexNames.get(0).equals("name"), "indexNames first");
        check(indexNames.equals(Arrays.asList("name")), "indexNames equals list");

        // isValue on a TabularData with same tabular type
        TabularDataSupport td = new TabularDataSupport(tt);
        check(tt.isValue(td), "isValue same type");
        check(!tt.isValue(null), "isValue null false");
        check(!tt.isValue("x"), "isValue non-TabularData false");

        // equals / hashCode
        TabularType tt2 = new TabularType("MyTab", "desc", rt, new String[]{"name"});
        check(tt.equals(tt2), "equals independent");
        check(tt.hashCode() == tt2.hashCode(), "hashCode equal");
        check(!tt.equals(null), "equals null false");
        check(!tt.equals("x"), "equals non-TabularType false");
        TabularType otherName = new TabularType("Other", "desc", rt, new String[]{"name"});
        check(!tt.equals(otherName), "different typeName not equal");
        TabularType otherIndex = new TabularType("MyTab", "desc", rt, new String[]{"value"});
        check(!tt.equals(otherIndex), "different indexNames not equal");
        TabularType otherRow = new TabularType("MyTab", "desc",
            new CompositeType("OtherRow", "d", new String[]{"name"}, new String[]{"d"}, new OpenType[]{SimpleType.STRING}),
            new String[]{"name"});
        check(!tt.equals(otherRow), "different rowType not equal");

        check(tt.toString().startsWith("javax.management.openmbean.TabularType("), "toString prefix");
        check(tt.toString().contains("name=MyTab"), "toString typeName");

        // exceptions (exact messages verified against JDK-27)
        checkThrows(OpenDataException.class, "Argument's element value indexNames[0]=\"nope\" is not a valid item name for rowType.",
            () -> { try { new TabularType("T", "d", rt, new String[]{"nope"}); } catch (Exception e) { throw sneaky(e); } },
            "invalid index name");
        checkThrows(IllegalArgumentException.class, "Argument rowType cannot be null.",
            () -> { try { new TabularType("T", "d", null, new String[]{"name"}); } catch (Exception e) { throw sneaky(e); } },
            "null rowType");
        checkThrows(IllegalArgumentException.class, "Argument indexNames[] cannot be null or empty.",
            () -> { try { new TabularType("T", "d", rt, new String[0]); } catch (Exception e) { throw sneaky(e); } },
            "empty indexNames");
        checkThrows(IllegalArgumentException.class, "Argument's element indexNames[0] cannot be an empty string.",
            () -> { try { new TabularType("T", "d", rt, new String[]{""}); } catch (Exception e) { throw sneaky(e); } },
            "empty index name");
    }

    // ---- deepened: unmodifiable indexNames / duplicates / structural rowType equality / ser ----
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
        CompositeType rt = rowType();
        TabularType tt = new TabularType("MyTab", "desc", rt, new String[]{"name"});
        // getIndexNames unmodifiable
        try {
            tt.getIndexNames().add("value");
            check(false, "getIndexNames must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        // index order preserved as given (not sorted)
        check(new TabularType("M", "d", rt, new String[]{"value", "name"}).getIndexNames()
                .equals(Arrays.asList("value", "name")), "multi index order preserved");
        // duplicate index names are NOT rejected (verified: allowed, list keeps duplicates)
        TabularType dup = new TabularType("T", "d", rt, new String[]{"name", "name"});
        check(dup.getIndexNames().equals(Arrays.asList("name", "name")), "duplicate index names allowed");
        // equals with structurally equal (but distinct) rowType instance
        TabularType structural = new TabularType("MyTab", "desc",
                new CompositeType("Row", "row desc",
                        new String[]{"name", "value"}, new String[]{"name desc", "value desc"},
                        new OpenType[]{SimpleType.STRING, SimpleType.INTEGER}),
                new String[]{"name"});
        check(tt.equals(structural), "equals with structurally equal rowType");
        check(tt.hashCode() == structural.hashCode(), "hashCode equal with structural rowType");
        // isValue accepts a table with a structurally equal type
        TabularDataSupport td = new TabularDataSupport(tt);
        check(structural.isValue(td), "isValue structurally equal type");
        // toString exact
        check(tt.toString().equals("javax.management.openmbean.TabularType(name=MyTab,"
                + "rowType=javax.management.openmbean.CompositeType(name=Row,"
                + "items=((itemName=name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),"
                + "(itemName=value,itemType=javax.management.openmbean.SimpleType(name=java.lang.Integer)))),"
                + "indexNames=(name))"), "toString exact");
        // serialization round-trip
        check(roundtrip(tt).equals(tt), "ser roundtrip equals");
        check(roundtrip(tt).hashCode() == tt.hashCode(), "ser roundtrip hashCode");
        // additional negatives (messages verified on this JDK)
        checkThrows(IllegalArgumentException.class, "Argument indexNames[] cannot be null or empty.",
            () -> { try { new TabularType("T", "d", rt, null); } catch (Exception e) { throw sneaky(e); } },
            "null indexNames array");
        checkThrows(IllegalArgumentException.class, "Argument's element indexNames[0] cannot be null.",
            () -> { try { new TabularType("T", "d", rt, new String[]{null}); } catch (Exception e) { throw sneaky(e); } },
            "null index name element");
        checkThrows(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
            () -> { try { new TabularType(null, "d", rt, new String[]{"name"}); } catch (Exception e) { throw sneaky(e); } },
            "null typeName");
        checkThrows(IllegalArgumentException.class, "Argument typeName cannot be null or empty",
            () -> { try { new TabularType("", "d", rt, new String[]{"name"}); } catch (Exception e) { throw sneaky(e); } },
            "empty typeName");
        checkThrows(IllegalArgumentException.class, "Argument description cannot be null or empty",
            () -> { try { new TabularType("T", null, rt, new String[]{"name"}); } catch (Exception e) { throw sneaky(e); } },
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
