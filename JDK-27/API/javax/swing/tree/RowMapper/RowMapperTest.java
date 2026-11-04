/*
 * @test
 * @summary javax.swing.tree.RowMapper conformance: type shape via reflection
 *          (headless-safe) plus polymorphic use of getRowsForPaths.
 * @run main RowMapperTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;

public class RowMapperTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Fixed mapping: "a" -> row 0, "b" -> row 2, anything else -> -1.
    static class FixedMapper implements RowMapper {
        int calls;
        public int[] getRowsForPaths(TreePath[] paths) {
            calls++;
            int[] rows = new int[paths.length];
            for (int i = 0; i < paths.length; i++) {
                String s = String.valueOf(paths[i].getLastPathComponent());
                rows[i] = "a".equals(s) ? 0 : "b".equals(s) ? 2 : -1;
            }
            return rows;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.RowMapper", false, RowMapperTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRowsForPaths"), "method getRowsForPaths");
        check(names.size() == 1, "single-method interface, got " + names.size() + ": " + names);
        Method m = c.getMethod("getRowsForPaths", TreePath[].class);
        check(Modifier.isAbstract(m.getModifiers()), "getRowsForPaths abstract");
        check(m.getReturnType() == int[].class, "getRowsForPaths returns int[]");
        for (Method dm : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(dm.getModifiers()), "abstract method " + dm.getName());
        }

        // Polymorphic contract through the interface reference: the returned rows
        // are positionally aligned with the paths argument.
        RowMapper rm = new FixedMapper();
        TreePath[] ps = new TreePath[]{new TreePath("a"), new TreePath("b"), new TreePath("z")};
        int[] rows = rm.getRowsForPaths(ps);
        check(rows.length == 3, "one row per path");
        check(rows[0] == 0 && rows[1] == 2 && rows[2] == -1, "positional mapping: " + java.util.Arrays.toString(rows));
        check(((FixedMapper) rm).calls == 1, "invoked through interface");
        check(rm.getRowsForPaths(new TreePath[0]).length == 0, "empty path array gives empty rows");

        // DefaultTreeSelectionModel consumes a RowMapper (see DefaultTreeSelectionModelTest).
        Class<?> sel = Class.forName("javax.swing.tree.DefaultTreeSelectionModel", false, RowMapperTest.class.getClassLoader());
        check(sel.getMethod("setRowMapper", RowMapper.class) != null, "DefaultTreeSelectionModel.setRowMapper(RowMapper)");
        check(sel.getMethod("getRowMapper").getReturnType() == RowMapper.class, "DefaultTreeSelectionModel.getRowMapper returns RowMapper");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
