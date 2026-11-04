/*
 * @test
 * @summary javax.swing.filechooser.FileFilter conformance: type shape via reflection
 *          plus behavioral contract (subclass semantics + JFileChooser interop, headless).
 * @run main FileFilterTest
 */

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class FileFilterTest {
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
        Class<?> c = Class.forName("javax.swing.filechooser.FileFilter", false, FileFilterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "not final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("accept"), "method accept");
        check(names.contains("getDescription"), "method getDescription");
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 2, "exactly two abstract methods");
        int publicCtors = 0;
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers())) publicCtors++;
        check(publicCtors == 1, "sole constructor is public");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        FileFilter txt = new FileFilter() {
            public boolean accept(File f) { return f == null ? false : f.getName().toLowerCase().endsWith(".txt"); }
            public String getDescription() { return "Text files"; }
        };
        check(txt instanceof FileFilter, "custom filter instanceof FileFilter");
        check("Text files".equals(txt.getDescription()), "getDescription returns subclass value");
        check(txt.accept(new File("report.txt")), "accept matches .txt");
        check(txt.accept(new File("REPORT.TXT")), "accept case-insensitive (subclass logic)");
        check(!txt.accept(new File("report.java")), "accept rejects other extension");
        check(!txt.accept((File) null), "accept rejects null defensively");
        check(!txt.accept(new File("nosuchdir")), "accept evaluates by name, not existence");

        // polymorphism over a list of files
        File[] files = {new File("a.txt"), new File("b.jpg"), new File("c.txt")};
        int hits = 0;
        for (File f : files) if (txt.accept(f)) hits++;
        check(hits == 2, "polymorphic accept over a file list");

        // interop with JFileChooser
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(txt);
        check(fc.getFileFilter() == txt, "JFileChooser.setFileFilter accepts custom filter");
        check(fc.getFileFilter().getDescription().equals("Text files"),
              "chooser delegates description to custom filter");
        check(fc.getFileFilter().accept(new File("x.txt")), "chooser delegates accept to custom filter");
        boolean removed = fc.removeChoosableFileFilter(txt);
        check(removed, "custom filter removable from choosable list");

        // FileNameExtensionFilter is the JDK-provided concrete subclass
        FileFilter fnef = new javax.swing.filechooser.FileNameExtensionFilter("d", "ext");
        check(fnef instanceof FileFilter, "FileNameExtensionFilter is a FileFilter");
        check(!fnef.getClass().equals(FileFilter.class), "concrete subclass distinct from abstract base");
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
