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

    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E { throw (E) t; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
