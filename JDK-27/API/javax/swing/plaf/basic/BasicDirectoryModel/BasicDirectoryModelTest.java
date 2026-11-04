/*
 * @test
 * @summary javax.swing.plaf.basic.BasicDirectoryModel conformance: white-box structure + black-box behavior.
 * @run main BasicDirectoryModelTest
 */

import java.lang.reflect.Modifier;

public class BasicDirectoryModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicDirectoryModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractListModel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.beans.PropertyChangeListener"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        java.nio.file.Path tmp = java.nio.file.Files.createTempDirectory("jck-bdm");
        java.io.File dir = tmp.toFile();
        java.io.File f1 = new java.io.File(dir, "a.txt");
        java.io.File f2 = new java.io.File(dir, "b.txt");
        f1.createNewFile();
        f2.createNewFile();
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(dir);
        javax.swing.plaf.basic.BasicDirectoryModel m = new javax.swing.plaf.basic.BasicDirectoryModel(fc);
        check(m.getSize() >= 0, "size non-negative");
        check(m.getFiles() != null, "files non-null");
        check(m.getDirectories() != null, "directories non-null");
        check(!m.contains(new java.io.File(dir, "zzz-not-exists")), "contains false for unknown");
        check(m.indexOf(new java.io.File(dir, "zzz-not-exists")) == -1, "indexOf -1 for unknown");
        boolean aioobe = false;
        try { m.getElementAt(-1); } catch (ArrayIndexOutOfBoundsException ex) { aioobe = true; }
        check(aioobe, "getElementAt(-1) throws AIOOBE");
        java.io.File nf = new java.io.File(dir, "c.txt");
        check(m.renameFile(f1, nf), "renameFile true");
        check(nf.exists(), "renamed file exists");
        final int[] cnt = {0};
        java.beans.PropertyChangeListener l = e -> cnt[0]++;
        m.addPropertyChangeListener(l);
        check(m.getPropertyChangeListeners().length == 1, "pcl count 1");
        m.removePropertyChangeListener(l);
        check(m.getPropertyChangeListeners().length == 0, "pcl removed");
        m.propertyChange(new java.beans.PropertyChangeEvent(fc, javax.swing.JFileChooser.DIRECTORY_CHANGED_PROPERTY, null, dir));
        check(true, "propertyChange DIRECTORY_CHANGED ok");
        m.invalidateFileCache();
        nf.delete();
        f2.delete();
        java.nio.file.Files.deleteIfExists(tmp);
    }
}
