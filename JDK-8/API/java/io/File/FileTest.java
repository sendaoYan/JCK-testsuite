/*
 * @test
 * @summary java.io.File API conformance: full contract coverage.
 *          Path components (getName/getParent/getAbsoluteFile/isAbsolute),
 *          filesystem queries (exists/isFile/isDirectory/length), lifecycle
 *          (createNewFile/mkdir/mkdirs/renameTo/delete), list/listFiles, equals/
 *          hashCode/compareTo, createTempFile, toPath/toFile roundtrip.
 *          All temp files live in an isolated dir cleaned up in finally.
 * @run main FileTest
 */

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class FileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void deleteRecursively(File f) {
        File[] kids = f.listFiles();
        if (kids != null) for (File k : kids) deleteRecursively(k);
        f.delete();
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-file", ".d");
        check(d.delete(), "seed temp file delete");
        check(d.mkdir(), "mk temp dir");
        return d;
    }

    // ---- 1. path components ----
    static void testPathComponents() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "f.txt");
            check("f.txt".equals(f.getName()), "getName");
            check(f.getParentFile() != null && f.getParentFile().equals(d), "getParentFile");
            check(f.getAbsoluteFile().isAbsolute(), "getAbsoluteFile is absolute");

            // relative path: getAbsoluteFile() materializes an absolute form
            File rel = new File("jck-rel");
            check(!rel.isAbsolute(), "relative name is not absolute");
            check(rel.getAbsoluteFile().isAbsolute(), "relative getAbsoluteFile is absolute");

            // no hardcoded separator: nested via constructor
            File nested = new File(new File(d, "a"), "b");
            check(nested.getPath().endsWith("a" + File.separator + "b"), "nested path uses separator");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 2. filesystem queries + lifecycle ----
    static void testLifecycle() throws Exception {
        File d = newTempDir();
        try {
            File nested = new File(new File(d, "a"), "b");
            check(nested.mkdirs() && nested.isDirectory(), "mkdirs nested");
            check(new File(d, "a").isDirectory(), "parent dir isDirectory");

            File f = new File(d, "f.txt");
            check(f.createNewFile(), "createNewFile");
            check(f.exists() && f.isFile() && !f.isDirectory(), "exists/isFile/isDirectory");
            check(f.length() == 0, "length 0 before write");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(new byte[]{1, 2, 3, 4, 5});
            fos.close();
            check(f.length() == 5, "length after write");

            File renamed = new File(d, "g.txt");
            check(f.renameTo(renamed) && renamed.exists() && !f.exists(), "renameTo");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 3. list / listFiles ----
    static void testList() throws Exception {
        File d = newTempDir();
        try {
            File a = new File(d, "a");
            check(a.mkdir(), "mk dir a");
            File f = new File(d, "f.txt");
            check(f.createNewFile(), "create f.txt");

            String[] lst = d.list();
            Arrays.sort(lst);
            check(Arrays.equals(lst, new String[]{"a", "f.txt"}), "list sorted");

            File[] files = d.listFiles();
            String[] fnames = new String[files.length];
            for (int i = 0; i < files.length; i++) fnames[i] = files[i].getName();
            Arrays.sort(fnames);
            check(Arrays.equals(fnames, new String[]{"a", "f.txt"}), "listFiles sorted");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 4. equals / hashCode / compareTo ----
    static void testEquality() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "g.txt");
            check(f.createNewFile(), "create");
            File other = new File(d, "g.txt");
            check(f.equals(other) && f.hashCode() == other.hashCode(), "equals/hashCode");
            check(f.compareTo(other) == 0, "compareTo == 0");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 5. delete semantics + nonexistent queries ----
    static void testDeleteAndMissing() throws Exception {
        File d = newTempDir();
        try {
            File a = new File(d, "a");
            check(a.mkdir(), "mk dir a");
            File f = new File(a, "f.txt");
            check(f.createNewFile(), "create inner file");

            check(!a.delete(), "non-empty dir delete == false");
            check(f.delete() && !f.exists(), "delete file");
            check(a.delete() && !a.exists(), "delete now-empty dir");

            File missing = new File(d, "nope");
            check(!missing.exists() && !missing.isFile() && !missing.isDirectory(),
                  "nonexistent exists/isFile/isDirectory false");
            check(missing.length() == 0, "nonexistent length 0");
            check(missing.list() == null, "nonexistent list null");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 6. createTempFile + toPath/toFile ----
    static void testTempAndRoundtrip() throws Exception {
        File d = newTempDir();
        try {
            File tmp = File.createTempFile("jck", ".tmp", d);
            check(tmp.exists() && tmp.isFile(), "createTempFile in dir");
            check(tmp.getName().startsWith("jck") && tmp.getName().endsWith(".tmp"),
                  "createTempFile name");
            check(tmp.getParentFile().equals(d), "createTempFile parent");
            check(tmp.toPath().toFile().equals(tmp), "toPath/toFile roundtrip");
            check(tmp.delete(), "delete temp file");
        } finally {
            deleteRecursively(d);
        }
    }

    public static void main(String[] args) throws Exception {
        testPathComponents();
        testLifecycle();
        testList();
        testEquality();
        testDeleteAndMissing();
        testTempAndRoundtrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
