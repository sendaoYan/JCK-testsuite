/*
 * @test
 * @summary javax.swing.table.TableStringConverter conformance: type shape via reflection
 *          (headless-safe) plus the toString(TableModel,int,int) contract exercised through
 *          a concrete subclass and its integration with TableRowSorter.
 * @run main TableStringConverterTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

public class TableStringConverterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static class SuffixConverter extends TableStringConverter {
        int calls;
        public String toString(TableModel m, int r, int c) {
            calls++;
            if (m == null) return "";
            Object v = m.getValueAt(r, c);
            return v == null ? "" : v + "!";
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableStringConverter", false, TableStringConverterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("toString"), "method toString");
        check(c.getDeclaredMethods().length == 1, "single declared method toString(TableModel,int,int)");
        Method m = c.getDeclaredMethod("toString", TableModel.class, int.class, int.class);
        check(Modifier.isAbstract(m.getModifiers()), "toString is abstract");
        check(m.getReturnType() == String.class, "toString returns String");

        // JDK-9+ exposes only a protected no-arg constructor (JDK-8 had it public).
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "JDK-27: protected no-arg constructor");

        // Contract: the converter is the single mapping point used by TableRowSorter
        // for rendering model values as sortable strings.
        final SuffixConverter conv = new SuffixConverter();
        DefaultTableModel model = new DefaultTableModel(new Object[][]{{"b"}, {"a"}}, new Object[]{"S"});
        check("b!".equals(conv.toString(model, 0, 0)), "converter content");
        check(conv.toString(null, 0, 0).equals(""), "subclass-defined null-model behaviour is subclass territory");

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);
        sorter.setStringConverter(conv);
        check(sorter.getStringConverter() == conv, "TableRowSorter stores converter");
        int before = conv.calls;
        sorter.toggleSortOrder(0);
        check(conv.calls > before, "converter invoked by sorting");
        check(sorter.convertRowIndexToModel(0) == 1, "converter values drive sort order");
        sorter.setStringConverter(null);
        check(sorter.getStringConverter() == null, "converter cleared");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
