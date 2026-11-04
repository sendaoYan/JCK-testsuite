/*
 * @test
 * @summary javax.swing.event.TableColumnModelEvent conformance: white-box structure + black-box behavior.
 * @run main TableColumnModelEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.TableColumnModelEvent", false, TableColumnModelEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getFromIndex"), "method getFromIndex");
        check(names.contains("getToIndex"), "method getToIndex");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
