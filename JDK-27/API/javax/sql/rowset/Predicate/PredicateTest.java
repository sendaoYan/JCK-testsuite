/*
 * @test
 * @summary javax.sql.rowset.Predicate conformance: white-box structure + black-box behavior.
 * @run main PredicateTest
 */

import java.sql.SQLException;
import javax.sql.RowSet;
import javax.sql.rowset.Predicate;

public class PredicateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.Predicate");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct superinterface(s)");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // An anonymous Predicate that selects values >= a threshold on column 1 ("ID").
        Predicate p = new Predicate() {
            public boolean evaluate(RowSet rs) {
                if (rs == null) return false;
                try { return rs.getInt(1) >= 10; } catch (SQLException e) { return false; }
            }
            public boolean evaluate(Object value, int column) {
                return column == 1 && value instanceof Integer && ((Integer) value) >= 10;
            }
            public boolean evaluate(Object value, String columnName) {
                return "ID".equalsIgnoreCase(columnName) && value instanceof Integer && ((Integer) value) >= 10;
            }
        };

        // evaluate(Object, int)
        check(p.evaluate(Integer.valueOf(5), 1) == false, "evaluate(5,1) false");
        check(p.evaluate(Integer.valueOf(15), 1) == true, "evaluate(15,1) true");
        check(p.evaluate(Integer.valueOf(15), 2) == false, "evaluate(15,2) false (wrong column)");

        // evaluate(Object, String)
        check(p.evaluate(Integer.valueOf(5), "ID") == false, "evaluate(5,ID) false");
        check(p.evaluate(Integer.valueOf(15), "ID") == true, "evaluate(15,ID) true");
        check(p.evaluate(Integer.valueOf(15), "NAME") == false, "evaluate(15,NAME) false (wrong name)");

        // evaluate(RowSet) with a null rowset should not throw (implementation-defined false)
        check(p.evaluate(null) == false, "evaluate(null) false");

        // A second predicate with inverted logic proves independent state.
        Predicate inverted = new Predicate() {
            public boolean evaluate(RowSet rs) { return false; }
            public boolean evaluate(Object value, int column) { return false; }
            public boolean evaluate(Object value, String columnName) { return false; }
        };
        check(inverted.evaluate(Integer.valueOf(15), 1) == false, "independent predicate instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
