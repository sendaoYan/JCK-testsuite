/*
 * @test
 * @summary javax.swing.table.TableStringConverter conformance: white-box structure + black-box behavior.
 * @run main TableStringConverterTest
 */

import java.lang.reflect.Modifier;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

public class TableStringConverterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableStringConverter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TableModel model = new DefaultTableModel(
                new Object[][]{{1, "a"}, {2, "b"}},
                new Object[]{"ID", "Name"});

        // anonymous subclass converting values to strings
        TableStringConverter converter = new TableStringConverter() {
            public String toString(TableModel m, int row, int column) {
                Object v = m.getValueAt(row, column);
                return v == null ? "<null>" : v.toString();
            }
        };
        check("1".equals(converter.toString(model, 0, 0)), "toString integer");
        check("a".equals(converter.toString(model, 0, 1)), "toString string");
        check("2".equals(converter.toString(model, 1, 0)), "toString row 1");

        // null value fallback
        TableModel nullModel = new DefaultTableModel(1, 1);
        check("<null>".equals(converter.toString(nullModel, 0, 0)), "toString null -> <null>");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
