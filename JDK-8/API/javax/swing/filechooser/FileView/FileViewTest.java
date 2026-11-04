/*
 * @test
 * @summary javax.swing.filechooser.FileView conformance: type shape via reflection plus
 *          behavioral contract (null defaults, subclass overrides, JFileChooser interop).
 * @run main FileViewTest
 */

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileView;

public class FileViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        shape();
        behavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.filechooser.FileView",
                false, FileViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("getDescription"), "method getDescription");
        check(names.contains("getTypeDescription"), "method getTypeDescription");
        check(names.contains("getIcon"), "method getIcon");
        check(names.contains("isTraversable"), "method isTraversable");
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 0, "no abstract methods: empty subclass is legal");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        File doc = new File("document.txt");

        // an empty subclass keeps the null-returning defaults ("ask the L&F")
        FileView bare = new FileView() {};
        check(bare.getName(doc) == null, "default getName null");
        check(bare.getDescription(doc) == null, "default getDescription null");
        check(bare.getTypeDescription(doc) == null, "default getTypeDescription null");
        check(bare.getIcon(doc) == null, "default getIcon null");
        check(bare.isTraversable(doc) == null, "default isTraversable null");

        // a fully overridden view returns its own metadata
        final Icon icon = new Icon() {
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {}
            public int getIconWidth() { return 8; }
            public int getIconHeight() { return 8; }
        };
        FileView rich = new FileView() {
            public String getName(File f) { return f == null ? null : "NAME-" + f.getName(); }
            public String getDescription(File f) { return "DESC"; }
            public String getTypeDescription(File f) { return "TYPE"; }
            public Icon getIcon(File f) { return icon; }
            public Boolean isTraversable(File f) { return Boolean.TRUE; }
        };
        check("NAME-document.txt".equals(rich.getName(doc)), "overridden getName");
        check(rich.getName(null) == null, "overridden getName handles null");
        check("DESC".equals(rich.getDescription(doc)), "overridden getDescription");
        check("TYPE".equals(rich.getTypeDescription(doc)), "overridden getTypeDescription");
        check(rich.getIcon(doc) == icon, "overridden getIcon");
        check(Boolean.TRUE.equals(rich.isTraversable(doc)), "overridden isTraversable");
        check("NAME-other.md".equals(rich.getName(new File("other.md"))),
              "getName applies per file");

        // partial override: un-overridden accessors keep null defaults
        FileView partial = new FileView() {
            public String getName(File f) { return f.getName(); }
        };
        check("document.txt".equals(partial.getName(doc)), "partial override getName");
        check(partial.getDescription(doc) == null, "partial override: description stays null");
        check(partial.getIcon(doc) == null, "partial override: icon stays null");

        // interop with JFileChooser
        JFileChooser fc = new JFileChooser();
        check(fc.getFileView() == null, "default chooser fileView null");
        fc.setFileView(rich);
        check(fc.getFileView() == rich, "setFileView roundtrip");
        fc.setFileView(null);
        check(fc.getFileView() == null, "setFileView(null)");
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
