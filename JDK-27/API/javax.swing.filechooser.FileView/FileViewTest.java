/*
 * @test
 * @summary javax.swing.filechooser.FileView conformance: white-box structure + black-box behavior.
 * @run main FileViewTest
 */

import java.io.File;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.filechooser.FileView;

public class FileViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Empty subclass exposes the default (null-returning) implementations.
    static class DefaultView extends FileView {
    }

    // A user view overriding getName, as JFileChooser users commonly do.
    static class NameOnlyView extends FileView {
        public String getName(File f) { return f == null ? null : f.getName(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.filechooser.FileView");
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
        check(c.getDeclaredMethods().length == 5, "five view methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        File f = new File("document.txt");

        // Default implementations all return null (signalling "ask the L&F").
        FileView def = new DefaultView();
        check(def.getName(f) == null, "default getName null");
        check(def.getDescription(f) == null, "default getDescription null");
        check(def.getTypeDescription(f) == null, "default getTypeDescription null");
        check(def.getIcon(f) == null, "default getIcon null");
        check(def.isTraversable(f) == null, "default isTraversable null");

        // A subclass override is dispatched through the abstract base reference.
        FileView view = new NameOnlyView();
        check("document.txt".equals(view.getName(f)), "overridden getName returns file name");
        check(view.getName(null) == null, "overridden getName handles null");
        check(view.getDescription(f) == null, "un-overridden getDescription stays null");
        check(view.getIcon(f) == null, "un-overridden getIcon stays null");

        // Typical JFileChooser usage: view returns a name, chooser falls back for the rest.
        File f2 = new File("photo.jpg");
        check("photo.jpg".equals(view.getName(f2)), "getName works across files");
        Icon icon = view.getIcon(f2);
        check(icon == null, "getIcon null when not provided (L&F fallback)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
