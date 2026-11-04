/*
 * @test
 * @summary com.sun.java.swing.plaf.motif.MotifFileChooserUI conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.java.swing.plaf.motif:+open
 * @run main MotifFileChooserUITest
 */

import java.util.*;
import java.lang.reflect.*;

public class MotifFileChooserUITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.motif.MotifFileChooserUI", false, MotifFileChooserUITest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.java.swing.plaf.motif.MotifFileChooserUI".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && "javax.swing.plaf.basic.BasicFileChooserUI".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("align;createDirectoryList;createFilesList;createFilterComboBoxModel;createFilterComboBoxRenderer;createPropertyChangeListener;createUI;ensureFileIsVisible;getApproveButton;getBottomPanel;getDirectoryName;getFileName;getMaximumSize;getMinimumSize;getPreferredSize;installComponents;installIcons;installStrings;installUI;rescanCurrentDirectory;setDirectoryName;setFileName;uninstallComponents;uninstallIcons;uninstallUI".equals(methodNames(c)), "public/protected methods");
        check("approveButton;directoryList;fileList;filenameTextField;filterComboBox;pathField".equals(fieldNames(c)), "public/protected fields");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String ifaces(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return String.join(";", s);
    }

    static String methodNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return String.join(";", s);
    }

    static String fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            int fm = f.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(f.getName());
        }
        return String.join(";", s);
    }

    static String enumConstants(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) s.add(f.getName());
        return String.join(";", s);
    }
}
