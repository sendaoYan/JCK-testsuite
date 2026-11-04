/*
 * @test
 * @summary javax.swing.event.TableColumnModelEvent conformance: white-box structure + black-box behavior.
 * @run main TableColumnModelEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

public class TableColumnModelEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TableColumnModelEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        TableColumnModel model = new DefaultTableColumnModel();
        javax.swing.event.TableColumnModelEvent ev = new javax.swing.event.TableColumnModelEvent(model, 2, 5);
        check(ev.getSource() == model, "getSource round-trip");
        check(ev.getFromIndex() == 2, "getFromIndex round-trip");
        check(ev.getToIndex() == 5, "getToIndex round-trip");

        javax.swing.event.TableColumnModelEvent single = new javax.swing.event.TableColumnModelEvent(model, 0, 0);
        check(single.getFromIndex() == 0 && single.getToIndex() == 0, "single column event");
        check(single.getSource() == model, "single source round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
