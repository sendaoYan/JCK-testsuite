/*
 * @test
 * @summary javax.swing.filechooser.FileNameExtensionFilter conformance: type shape via
 *          reflection plus behavioral contract (ctor IAE matrix, exact matching, headless).
 * @run main FileNameExtensionFilterTest
 */

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileNameExtensionFilterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown"); }

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
        Class<?> c = Class.forName("javax.swing.filechooser.FileNameExtensionFilter",
                false, FileNameExtensionFilterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "not abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null
              && c.getSuperclass().getName().equals("javax.swing.filechooser.FileFilter"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("accept"), "method accept");
        check(names.contains("getDescription"), "method getDescription");
        check(names.contains("getExtensions"), "method getExtensions");
        check(names.contains("toString"), "method toString");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        // ctor IAE matrix
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new FileNameExtensionFilter("d", (String[]) null); }});
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new FileNameExtensionFilter("d"); }});
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new FileNameExtensionFilter("d", new String[]{"ok", null}); }});
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new FileNameExtensionFilter("d", new String[]{"ok", ""}); }});
        // content of extension strings is NOT validated (spaces/slashes accepted)
        FileNameExtensionFilter weird = new FileNameExtensionFilter("d2", "a b", "x/y");
        check(weird != null, "ctor does not validate spaces/slashes");
        // null description allowed
        FileNameExtensionFilter noDesc = new FileNameExtensionFilter(null, "x");
        check(noDesc.getDescription() == null, "null description preserved");

        // description / extensions exact roundtrip, no normalization
        FileNameExtensionFilter f = new FileNameExtensionFilter("Docs", "txt", "md");
        check("Docs".equals(f.getDescription()), "getDescription exact");
        check(f.getExtensions().length == 2, "getExtensions length");
        check("txt".equals(f.getExtensions()[0]) && "md".equals(f.getExtensions()[1]),
              "getExtensions exact order/content");
        // getExtensions returns a defensive copy
        String[] stolen = f.getExtensions();
        stolen[0] = "hacked";
        check(f.getExtensions()[0].equals("txt"), "getExtensions defensive copy");

        // accept: case-insensitive on both sides, exact suffix match
        check(f.accept(new File("a.txt")), "accept .txt");
        check(f.accept(new File("a.TXT")), "accept uppercase file extension");
        check(f.accept(new File("a.md")), "accept second extension");
        check(!f.accept(new File("a.txtx")), "no partial suffix match");
        check(!f.accept(new File("atxt")), "extension requires the dot");
        FileNameExtensionFilter upper = new FileNameExtensionFilter("U", "TXT");
        check(upper.accept(new File("a.txt")), "lowercase file against uppercase filter ext");
        check(upper.accept(new File("a.TXT")), "uppercase file against uppercase filter ext");
        FileNameExtensionFilter spaced = new FileNameExtensionFilter("S", " csv ");
        check(!spaced.accept(new File("a.csv")), "extension not trimmed for matching");
        // directories are always accepted; null never accepted
        check(f.accept(new File(System.getProperty("java.io.tmpdir"))), "accept directory");
        check(!f.accept(null), "accept(null) false");
        check(!f.accept(new File("noext")), "file without extension rejected");

        // toString includes description and extensions
        String ts = f.toString();
        check(ts.contains("Docs"), "toString contains description");
        check(ts.contains("txt"), "toString contains extension");

        // works as a JFileChooser filter
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(f);
        check(fc.getFileFilter() == f, "usable as JFileChooser file filter");
        check(fc.getFileFilter().accept(new File("z.txt")), "chooser delegates accept");
    }
}
