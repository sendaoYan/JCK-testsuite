/*
 * @test
 * @summary javax.swing.table.JTableHeader.AccessibleJTableHeader.AccessibleJTableHeaderEntry conformance: white-box structure + black-box behavior.
 * @run main AccessibleJTableHeaderEntryTest
 */

import java.lang.reflect.Modifier;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class AccessibleJTableHeaderEntryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.JTableHeader$AccessibleJTableHeader$AccessibleJTableHeaderEntry");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.AccessibleComponent"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{{1, "a"}, {2, "b"}},
                new Object[]{"ID", "Name"});
        JTable table = new JTable(model);
        JTableHeader header = table.getTableHeader();
        TableColumn col0 = header.getColumnModel().getColumn(0);
        col0.setHeaderValue("IDCol");

        AccessibleContext headerAc = header.getAccessibleContext();
        Accessible child0 = headerAc.getAccessibleChild(0);
        check(child0 != null, "entry 0 non-null");

        AccessibleContext entry = (AccessibleContext) child0;
        check(entry != null, "entry is an AccessibleContext");
        check(child0.getAccessibleContext() == entry, "getAccessibleContext returns self");
        check(entry.getAccessibleIndexInParent() == 0, "entry 0 index in parent");

        // accessible name comes from the header renderer's text
        check("IDCol".equals(entry.getAccessibleName()), "accessible name from header value");

        // set/get accessible name round-trip
        entry.setAccessibleName("CustomHeader");
        check("CustomHeader".equals(entry.getAccessibleName()), "set/get accessible name round-trip");

        // role is non-null and children count non-negative
        AccessibleRole role = entry.getAccessibleRole();
        check(role != null, "accessible role non-null");
        check(entry.getAccessibleChildrenCount() >= 0, "accessible children count non-negative");

        // index in parent for the second column
        Accessible child1 = headerAc.getAccessibleChild(1);
        AccessibleContext entry1 = (AccessibleContext) child1;
        check(entry1.getAccessibleIndexInParent() == 1, "entry 1 index in parent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
