/*
 * @test
 * @summary javax.swing.filechooser.FileFilter conformance: white-box structure + black-box behavior.
 * @run main FileFilterTest
 */

import java.io.File;
import java.lang.reflect.Modifier;
import javax.swing.filechooser.FileFilter;

public class FileFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Concrete FileFilter accepting only a fixed extension, used as a typical user subclass.
    static class TextFilter extends FileFilter {
        public boolean accept(File f) {
            return f != null && f.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".txt");
        }
        public String getDescription() { return "Text files"; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.filechooser.FileFilter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethods().length == 2, "two methods: accept + getDescription");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        FileFilter filter = new TextFilter();
        check("Text files".equals(filter.getDescription()), "getDescription returns subclass value");
        check(filter.accept(new File("report.txt")), "accept matches .txt");
        check(filter.accept(new File("REPORT.TXT")), "accept case-insensitive (subclass logic)");
        check(!filter.accept(new File("report.java")), "accept rejects other extension");
        check(!filter.accept(null), "accept rejects null defensively");

        // A filter can be used polymorphically, e.g. in a chooser-style selection loop.
        File[] candidates = { new File("a.txt"), new File("b.jpg"), new File("c.txt") };
        int accepted = 0;
        for (File f : candidates) {
            if (filter.accept(f)) accepted++;
        }
        check(accepted == 2, "polymorphic accept over a file list");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Appended contract section: JFileChooser interop and JDK-provided subclass.
    static void testContract() {
        try { javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }
        TextFilter txt = new TextFilter();
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(txt);
        check(fc.getFileFilter() == txt, "JFileChooser.setFileFilter accepts custom filter");
        check("Text files".equals(fc.getFileFilter().getDescription()),
              "chooser delegates description to custom filter");
        check(fc.getFileFilter().accept(new File("x.txt")), "chooser delegates accept to custom filter");
        check(fc.removeChoosableFileFilter(txt), "custom filter removable from choosable list");
        FileFilter fnef = new javax.swing.filechooser.FileNameExtensionFilter("d", "ext");
        check(fnef instanceof FileFilter, "FileNameExtensionFilter is a FileFilter");
        check(!txt.accept(new File("nosuchdir")), "accept evaluates by name, not existence");
    }
}
