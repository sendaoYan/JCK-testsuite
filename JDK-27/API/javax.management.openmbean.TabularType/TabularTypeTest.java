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

    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E { throw (E) t; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
