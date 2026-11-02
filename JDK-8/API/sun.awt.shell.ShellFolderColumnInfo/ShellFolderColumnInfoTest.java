/*
 * @test
 * @summary sun.awt.shell.ShellFolderColumnInfo conformance: reflection (no init, native/display-heavy).
 * @run main ShellFolderColumnInfoTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ShellFolderColumnInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.shell.ShellFolderColumnInfo", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.shell.ShellFolderColumnInfo".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public#boolean#isCompareByColumn();public#boolean#isVisible();public#java.lang.Integer#getAlignment();public#java.lang.Integer#getWidth();public#java.lang.String#getTitle();public#java.util.Comparator#getComparator();public#javax.swing.SortOrder#getSortOrder();public#void#setAlignment(java.lang.Integer);public#void#setComparator(java.util.Comparator);public#void#setCompareByColumn(boolean);public#void#setSortOrder(javax.swing.SortOrder);public#void#setTitle(java.lang.String);public#void#setVisible(boolean);public#void#setWidth(java.lang.Integer)".equals(methods(c)), "methods");
        check("private#boolean#compareByColumn;private#boolean#visible;private#java.lang.Integer#alignment;private#java.lang.Integer#width;private#java.lang.String#title;private#java.util.Comparator#comparator;private#javax.swing.SortOrder#sortOrder".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 3, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
