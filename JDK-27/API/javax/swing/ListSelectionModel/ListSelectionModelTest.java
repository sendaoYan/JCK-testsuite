/*
 * @test
 * @summary javax.swing.ListSelectionModel conformance: type shape via reflection
 *          (headless-safe), selection-mode constants, and polymorphic use.
 * @run main ListSelectionModelTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ListSelectionModel", false, ListSelectionModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSelectionInterval"), "method setSelectionInterval");
        check(names.contains("addSelectionInterval"), "method addSelectionInterval");
        check(names.contains("removeSelectionInterval"), "method removeSelectionInterval");
        check(names.contains("getMinSelectionIndex"), "method getMinSelectionIndex");
        check(names.contains("getMaxSelectionIndex"), "method getMaxSelectionIndex");
        check(names.contains("isSelectedIndex"), "method isSelectedIndex");
        check(names.contains("getAnchorSelectionIndex"), "method getAnchorSelectionIndex");
        check(names.contains("setAnchorSelectionIndex"), "method setAnchorSelectionIndex");
        check(names.contains("clearSelection"), "method clearSelection");
        check(names.contains("isSelectionEmpty"), "method isSelectionEmpty");
        check(names.contains("insertIndexInterval"), "method insertIndexInterval");
        check(names.contains("removeIndexInterval"), "method removeIndexInterval");
        check(names.contains("setValueIsAdjusting"), "method setValueIsAdjusting");
        check(names.contains("setSelectionMode"), "method setSelectionMode");
        check(names.contains("addListSelectionListener"), "method addListSelectionListener");
        check(names.size() == 22, "20 classic methods + getSelectedIndices + getSelectedItemsCount, got "
                + names.size() + ": " + names);
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                abstractCount++;
            } else {
                check(Modifier.isPublic(m.getModifiers()) && !m.isSynthetic(),
                        "non-abstract declared method is a public default: " + m.getName());
            }
        }
        check(abstractCount == 20, "20 abstract methods, got " + abstractCount);
        // removeIndexInterval takes two index endpoints, not index+length.
        Method rm = c.getMethod("removeIndexInterval", int.class, int.class);
        check(rm != null, "removeIndexInterval(int, int) signature");
        Method ins = c.getMethod("insertIndexInterval", int.class, int.class, boolean.class);
        check(ins != null, "insertIndexInterval(int, int, boolean) signature");

        // Selection-mode constants.
        Field f = c.getField("SINGLE_SELECTION");
        check(f.getInt(null) == 0, "SINGLE_SELECTION == 0");
        check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()), "constant modifiers");
        check(c.getField("SINGLE_INTERVAL_SELECTION").getInt(null) == 1, "SINGLE_INTERVAL_SELECTION == 1");
        check(c.getField("MULTIPLE_INTERVAL_SELECTION").getInt(null) == 2, "MULTIPLE_INTERVAL_SELECTION == 2");

        // JDK-21+ adds getSelectedIndices and getSelectedItemsCount as default methods
        // (JDK-8 has neither).
        boolean hasDefaults = false;
        for (Method m : c.getDeclaredMethods()) {
            if (!Modifier.isAbstract(m.getModifiers()) && !m.isSynthetic() && Modifier.isPublic(m.getModifiers())) hasDefaults = true;
        }
        check(hasDefaults, "JDK-27: interface declares default methods");
        Method gi = c.getMethod("getSelectedIndices");
        check(Modifier.isPublic(gi.getModifiers()) && !Modifier.isAbstract(gi.getModifiers()),
                "getSelectedIndices is a default method");
        check(gi.getReturnType() == int[].class, "getSelectedIndices returns int[]");
        check(c.getMethod("getSelectedItemsCount").getReturnType() == int.class,
                "getSelectedItemsCount returns int");

        // Known implementations.
        Class<?> impl = Class.forName("javax.swing.DefaultListSelectionModel", false, ListSelectionModelTest.class.getClassLoader());
        check(c.isAssignableFrom(impl), "DefaultListSelectionModel is a ListSelectionModel");
        Class<?> col = Class.forName("javax.swing.table.DefaultTableColumnModel", false, ListSelectionModelTest.class.getClassLoader());
        check(col.getMethod("getSelectionModel").getReturnType() == c, "DefaultTableColumnModel exposes a ListSelectionModel");

        // Polymorphic contract through the interface reference.
        ListSelectionModel m = new DefaultListSelectionModel();
        m.setSelectionInterval(1, 3);
        check(m.getMinSelectionIndex() == 1 && m.getMaxSelectionIndex() == 3, "bounds through interface");
        check(m.isSelectedIndex(2) && !m.isSelectedIndex(0), "isSelectedIndex through interface");
        check(!m.isSelectionEmpty(), "not empty through interface");
        check(m.getAnchorSelectionIndex() == 1 && m.getLeadSelectionIndex() == 3, "anchor/lead through interface");
        m.addSelectionInterval(6, 6);
        check(m.getMaxSelectionIndex() == 6, "add through interface");
        m.removeSelectionInterval(1, 3);
        check(m.getMinSelectionIndex() == 6, "remove through interface");
        m.insertIndexInterval(6, 2, true);
        check(m.isSelectedIndex(7), "insertIndexInterval through interface");
        m.removeIndexInterval(6, 7);
        check(m.getMinSelectionIndex() == 6, "removeIndexInterval through interface");
        m.setValueIsAdjusting(true);
        check(m.getValueIsAdjusting(), "adjusting through interface");
        m.setValueIsAdjusting(false);
        final boolean[] notified = {false};
        m.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) { notified[0] = true; }
        });
        m.setSelectionInterval(0, 0);
        check(notified[0], "listener notified through interface");
        m.clearSelection();
        check(m.isSelectionEmpty(), "clearSelection through interface");

        // JDK-21+ default conveniences on the same reference.
        m.setSelectionInterval(4, 6);
        check(java.util.Arrays.toString(m.getSelectedIndices()).equals("[4, 5, 6]"),
                "getSelectedIndices lists ascending: " + java.util.Arrays.toString(m.getSelectedIndices()));
        check(m.getSelectedItemsCount() == 3, "getSelectedItemsCount counts the selection");
        m.clearSelection();
        check(m.getSelectedIndices().length == 0, "getSelectedIndices empty after clear");
        check(m.getSelectedItemsCount() == 0, "getSelectedItemsCount 0 after clear");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
