/*
 * @test
 * @summary javax.swing.table.TableColumn conformance: column props (headless-safe).
 * @run main TableColumnTest
 */

import javax.swing.table.TableColumn;

public class TableColumnTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        TableColumn c = new TableColumn(2);
        check(c.getModelIndex() == 2, "model index");
        c.setHeaderValue("H");
        check("H".equals(c.getHeaderValue()), "header");
        check("H".equals(c.getIdentifier()), "identifier");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
