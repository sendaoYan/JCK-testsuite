/*
 * @test
 * @summary javax.swing.RowFilter conformance: white-box structure + black-box behavior.
 * @run main RowFilterTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.RowFilter;

public class RowFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.RowFilter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RowFilter<Object, Object> regex = RowFilter.regexFilter("a.*");
        check(regex != null, "regexFilter");
        RowFilter<Object, Object> number = RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, 5);
        check(number != null, "numberFilter");
        RowFilter<Object, Object> not = RowFilter.notFilter(regex);
        check(not != null, "notFilter");

        List<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>();
        filters.add(regex);
        filters.add(not);
        check(RowFilter.andFilter(filters) != null, "andFilter");
        check(RowFilter.orFilter(filters) != null, "orFilter");

        RowFilter<Object, Object> anon = new RowFilter<Object, Object>() {
            public boolean include(RowFilter.Entry<? extends Object, ? extends Object> entry) {
                return true;
            }
        };
        check(anon.include(null), "include anonymous");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
