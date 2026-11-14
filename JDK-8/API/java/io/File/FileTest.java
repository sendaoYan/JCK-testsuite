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

    // ================= 深化追加（契约级） =================
    // 以下所有行为均先以探针在 JDK-8 与 JDK-27 双侧实测锁定（见 DESIGN.md「深化追加」）。

    static interface Thrower { void run() throws Throwable; }

    static void mustThrow(Class<? extends Throwable> type, String msgPart, Thrower r, String what) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t) && (msgPart == null
                    || (t.getMessage() != null && t.getMessage().contains(msgPart)))) {
                passed++;
                return;
            }
            throw new AssertionError(what + ": expected " + type.getSimpleName()
                    + (msgPart != null ? " containing \"" + msgPart + "\"" : "")
                    + " but got " + t.getClass().getName() + " [" + t.getMessage() + "]");
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }

    // ---- 7. constructor matrix + null branches ----
    static void testCtorNullsAndMatrix() throws Exception {
        mustThrow(NullPointerException.class, null, () -> new File((String) null), "File((String)null)");
        // null String parent is legal: acts like File(child)
        check(new File((String) null, "c").equals(new File("c")), "null String parent acts like File(child)");
        mustThrow(NullPointerException.class, null, () -> new File("p", null), "File(parent, null child)");
        check(new File((File) null, "c").equals(new File("c")), "null File parent acts like File(child)");
        mustThrow(NullPointerException.class, null, () -> new File(new File("p"), null), "File(File, null child)");
        mustThrow(NullPointerException.class, null, () -> new File((java.net.URI) null), "File((URI)null)");

        File d = newTempDir();
        try {
            File sub = new File(d, "sub");
            check(new File(sub, "child").getParentFile().equals(sub), "(File,String) parentFile is parent");
            check("child".equals(new File(sub, "child").getName()), "(File,String) name is child");
            File byStr = new File(d.getAbsolutePath(), "child");
            check(byStr.getAbsoluteFile().getParentFile().equals(d), "(String,String) absolute parent");
            check("child".equals(byStr.getName()), "(String,String) name is child");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 8. normalization: getPath keeps ".", ".."; collapses duplicate seps; trims trailing sep ----
    static void testPathNormalization() throws Exception {
        // 平台分隔符：Windows 上构造时给的 "/" 在 getPath() 里统一成 "\"，
        // 期望串必须用 File.separator 拼，否则断言的是 Linux 的分隔符。
        String s = File.separator;
        check(("a" + s + ".." + s + "b").equals(new File("a/../b").getPath()), "getPath keeps ..");
        check(("a" + s + "." + s + "b").equals(new File("a/./b").getPath()), "getPath keeps .");
        check(("a" + s + "b").equals(new File("a//b").getPath()), "duplicate separators collapsed");
        check(("a" + s + "b").equals(new File("a/b/").getPath()), "trailing separator trimmed");
        check(("a" + s + ".." + s + "b").equals(new File("a/../b").getAbsolutePath()
                  .substring(new File("a/../b").getAbsolutePath().length() - 6)),
              "getAbsolutePath does not resolve .. either");
        // canonical path resolves .. and . lexically against the working directory
        check(new File("a/../b").getCanonicalPath().equals(new File("b").getCanonicalPath()),
              "canonical a/../b == canonical b");
        check(new File("./x").getCanonicalPath().equals(new File("x").getCanonicalPath()),
              "canonical ./x == canonical x");
        check(new File("a/b/..").getCanonicalPath().equals(new File("a").getCanonicalPath()),
              "canonical a/b/.. == canonical a");
        check(new File("a//b").getCanonicalPath().equals(new File("a/b").getCanonicalPath()),
              "canonical collapses duplicates");
        check(new File("a/./b").getCanonicalPath().equals(new File("a/b").getCanonicalPath()),
              "canonical resolves .");
        File d = newTempDir();
        try {
            File b = new File(d, "b");
            check(b.mkdir(), "mkdir b");
            check(new File(d, "a/../b").getCanonicalPath().equals(b.getCanonicalPath()),
                  "canonical .. resolves against a real dir");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 9. special names: empty, rootless, root ----
    static void testSpecialNames() {
        check("".equals(new File("").getPath()), "empty path kept");
        check("".equals(new File("").getName()), "empty path has empty name");
        check(new File("").getParent() == null, "empty path has no parent");
        check(new File("a").getParent() == null, "single name has null parent");
        check(new File("a").getParentFile() == null, "single name has null parentFile");
        check("b".equals(new File("a/b/").getName()), "trailing sep trimmed before getName");
        check("a".equals(new File("a/b/").getParent()), "trailing sep trimmed before getParent");
        check(!new File("a/b").isAbsolute(), "relative path isAbsolute false");
    }

    // ---- 10. case sensitivity (Linux) + compare/hash contracts ----
    static void testCaseAndCompare() {
        check(new File("A").equals(new File("A")), "equals reflexive");
        check(new File("x/y").hashCode() == new File("x/y").hashCode(), "hashCode stable across instances");
        check(!new File("x").equals("x"), "equals non-File is false");
        check(new File("x/y").compareTo(new File("x/y")) == 0, "compareTo equal paths is 0");
        check(new File("a").compareTo(new File("ab")) < 0, "a < ab");
        check(new File("ab").compareTo(new File("b")) < 0, "ab < b");
        check(new File("b").compareTo(new File("a")) > 0, "b > a");
        mustThrow(NullPointerException.class, null, () -> new File("x").compareTo(null), "compareTo(null)");
        if (File.separatorChar == '/') {                 // POSIX fs is case sensitive
            check(!new File("A").equals(new File("a")), "Linux: File(A) != File(a)");
            check(new File("A").hashCode() != new File("a").hashCode(), "Linux: hashes differ with case");
            check(new File("A").compareTo(new File("a")) < 0, "Linux: 'A' sorts before 'a'");
            check(new File("a").compareTo(new File("A")) > 0, "Linux: 'a' sorts after 'A'");
        }
    }

    // ---- 11. toString / separators / listRoots ----
    static void testConstantsAndToString() {
        check(new File("a/b").toString().equals(new File("a/b").getPath()), "toString == getPath");
        check(new File("").toString().equals(""), "toString of empty is empty");
        check(File.separator.equals(String.valueOf(File.separatorChar)), "separator matches separatorChar");
        check(File.pathSeparator.equals(String.valueOf(File.pathSeparatorChar)),
              "pathSeparator matches pathSeparatorChar");
        File[] roots = File.listRoots();
        check(roots != null && roots.length >= 1, "listRoots non-empty");
        if (File.separatorChar == '/') {
            check("/".equals(File.separator) && File.pathSeparator.equals(":"), "POSIX separators");
            boolean hasRoot = false;
            for (File r : roots) if (r.getPath().equals("/")) hasRoot = true;
            check(hasRoot, "Linux: roots contain /");
            File root = new File("/");
            check(root.exists() && root.isDirectory() && root.isAbsolute(), "/ exists, dir, absolute");
            check("".equals(root.getName()), "root name is empty");
        }
    }

    // ---- 12. readability/writability/executability boolean matrix ----
    static void testPermissionsMatrix() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "perm.txt");
            check(f.createNewFile(), "create for permissions");
            check(f.canRead(), "new file canRead");
            check(f.canWrite(), "new file canWrite");
            if (File.separatorChar == '/') {
                check(!f.canExecute(), "new file !canExecute");
            } else {
                // Windows：java.io.File 只映射“只读”属性；对存在的文件
                // checkAccess(READ/EXECUTE) 恒为 true，setReadable/setExecutable 是空操作
                // （WinNTFileSystem.setPermission0 对 READ/EXECUTE 直接返回 enable）。
                check(f.canExecute(), "Windows: existing file canExecute is always true");
                check(!f.setReadable(false), "Windows: setReadable(false) returns false");
                check(f.canRead(), "Windows: setReadable(false) does not revoke read (no-op)");
                check(f.setReadable(true), "Windows: setReadable(true) returns true");
                check(!f.setExecutable(false), "Windows: setExecutable(false) returns false");
                check(f.canExecute(), "Windows: setExecutable(false) does not revoke execute (no-op)");
            }
            check(f.setReadOnly(), "setReadOnly true");
            check(!f.canWrite(), "after setReadOnly canWrite false");
            check(f.canRead(), "after setReadOnly still readable");
            check(f.setWritable(true), "setWritable(true)");
            check(f.canWrite(), "writable again");
            check(f.setWritable(false), "setWritable(false)");
            check(!f.canWrite(), "not writable");
            check(f.setWritable(true), "restore writable");
            if (File.separatorChar == '/') {           // 读/执行位只有 POSIX 后端真正生效
                check(f.setReadable(false), "setReadable(false)");
                check(!f.canRead(), "unreadable");
                check(f.setReadable(true, true), "setReadable(true, ownerOnly=true)");
                check(f.canRead(), "owner readable");
                check(f.setReadable(false, false), "setReadable(false, ownerOnly=false)");
                check(!f.canRead(), "ownerOnly=false clears every read bit");
                check(f.setReadable(true), "restore readable");
                check(f.setExecutable(true), "setExecutable(true)");
                check(f.canExecute(), "executable");
                check(f.setExecutable(false, true), "setExecutable(false, ownerOnly=true)");
                check(!f.canExecute(), "owner bit cleared");
                check(f.setExecutable(true, false), "setExecutable(true, ownerOnly=false)");
                check(f.canExecute(), "all bits set");
                check(f.setExecutable(false), "clear executable");
            }

            File missing = new File(d, "nope");
            check(!missing.canRead() && !missing.canWrite() && !missing.canExecute(),
                  "missing file permissions all false");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 13. lastModified / setLastModified ----
    static void testLastModifiedContracts() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "lm.txt");
            check(f.createNewFile(), "create for lastModified");
            check(f.lastModified() > 0, "new file lastModified > 0");
            long t = 1234567890123L;
            check(f.setLastModified(t), "setLastModified returns true");
            check(f.lastModified() == t, "lastModified round-trips exactly (Linux ms)");
            mustThrow(IllegalArgumentException.class, "Negative time",
                      () -> f.setLastModified(-1L), "setLastModified(-1)");
            check(f.setLastModified(0L), "setLastModified(0) allowed");
            check(f.lastModified() == 0L, "epoch reads back");
            check(new File(d, "nope").lastModified() == 0L, "missing file lastModified 0");
            check(!new File(d, "nope").setLastModified(t), "setLastModified on missing false");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 14. createNewFile / delete edge matrix ----
    static void testCreateDeleteEdges() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "e.txt");
            check(f.createNewFile(), "createNewFile first true");
            check(!f.createNewFile(), "createNewFile on existing false");
            // 原因文本来自 OS（zh_CN Windows 下是中文），只锁异常类型。
            mustThrow(java.io.IOException.class, null,
                      () -> new File(new File(d, "nodir"), "x").createNewFile(),
                      "createNewFile with missing parent");
            check(!new File(d, "zzz").delete(), "delete missing false");
            check(f.delete(), "delete existing true");
            check(!f.exists(), "gone after delete");
            File sub = new File(d, "sub");
            check(sub.mkdir() && new File(sub, "k").createNewFile(), "setup non-empty dir");
            check(!sub.delete(), "delete non-empty dir false");
            check(new File(sub, "k").delete() && sub.delete(), "delete after emptying");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 15. mkdir / mkdirs matrix ----
    static void testMkdirMatrix() throws Exception {
        File d = newTempDir();
        try {
            check(!d.mkdir(), "mkdir on existing dir false");
            check(!new File(new File(d, "q"), "r").mkdir(), "mkdir with missing parent false");
            check(!d.mkdirs(), "mkdirs on existing path false");
            File asFile = new File(d, "mid");
            check(asFile.createNewFile(), "setup mid as file");
            check(!new File(new File(asFile, "s"), "t").mkdirs(), "mkdirs through a file false");
            File deep = new File(new File(new File(d, "l1"), "l2"), "l3");
            check(deep.mkdirs(), "mkdirs multi-level true");
            check(deep.isDirectory() && new File(new File(d, "l1"), "l2").isDirectory(),
                  "intermediate dirs created");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 16. list/listFiles null and empty contracts + null filters ----
    static void testListContracts() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "plain");
            check(f.createNewFile(), "setup plain file");
            check(f.list() == null, "list() on a plain file null");
            check(f.listFiles() == null, "listFiles() on a plain file null");
            check(f.list((dir, name) -> true) == null, "list(FilenameFilter) on a plain file null");
            check(f.listFiles(x -> true) == null, "listFiles(FileFilter) on a plain file null");
            File missing = new File(d, "gone");
            check(missing.list() == null && missing.listFiles() == null, "list on missing null");
            File e = new File(d, "empty");
            check(e.mkdir(), "setup empty dir");
            check(e.list() != null && e.list().length == 0, "empty dir list length 0");
            check(e.listFiles() != null && e.listFiles().length == 0, "empty dir listFiles length 0");
            check(e.list((dir, name) -> true).length == 0, "empty dir filtered list length 0");
            File sub = new File(d, "sdir");
            check(sub.mkdir(), "setup subdir");
            check(d.list(null).length == d.list().length, "null FilenameFilter lists everything");
            check(d.listFiles((java.io.FilenameFilter) null).length == d.listFiles().length,
                  "null FilenameFilter listFiles everything");
            check(d.listFiles((java.io.FileFilter) null).length == d.listFiles().length,
                  "null FileFilter listFiles everything");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 17. renameTo matrix ----
    static void testRenameContracts() throws Exception {
        File d = newTempDir();
        try {
            File a = new File(d, "a.txt");
            check(a.createNewFile(), "setup a");
            File b = new File(d, "b.txt");
            check(a.renameTo(b) && b.exists() && !a.exists(), "same-dir rename true, source gone");
            mustThrow(NullPointerException.class, null, () -> a.renameTo(null), "renameTo(null)");
            check(!a.renameTo(new File(d, "c.txt")), "rename missing source false");
            check(!b.renameTo(new File(new File(d, "nodir"), "x.txt")), "rename to missing parent false");
            if (File.separatorChar == '/') {              // POSIX rename(2) replaces the target
                File victim = new File(d, "victim");
                check(victim.createNewFile(), "setup victim");
                check(b.renameTo(victim), "POSIX: rename onto existing target true");
                check(!b.exists() && victim.exists(), "target replaced, source gone");
                check(victim.renameTo(victim), "POSIX: rename onto itself true");
                check(victim.exists(), "still exists after self rename");
            }
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 18. toURI/toURL/URI ctor error matrix ----
    static void testUriContracts() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "u.txt");
            check(f.createNewFile(), "setup for URI");
            java.net.URI uri = f.toURI();
            check("file".equals(uri.getScheme()), "toURI scheme is file");
            check(uri.isAbsolute(), "toURI absolute");
            check(uri.toString().startsWith("file:"), "toURI string starts with file:");
            check(new File(uri).equals(f), "new File(toURI()) round-trip equals");
            java.net.URI absUri = f.getAbsoluteFile().toURI();
            check(new File(absUri).equals(f), "absolute URI round-trip equals");
            check(f.toURL().toString().startsWith("file:"), "toURL starts with file:");

            mustThrow(IllegalArgumentException.class, "URI is not absolute",
                      () -> new File(java.net.URI.create("a/b")), "relative URI");
            mustThrow(IllegalArgumentException.class, "URI scheme is not \"file\"",
                      () -> new File(java.net.URI.create("http://x/y")), "non-file scheme");
            mustThrow(IllegalArgumentException.class, "URI is not hierarchical",
                      () -> new File(java.net.URI.create("mailto:a@b")), "opaque URI");
            mustThrow(IllegalArgumentException.class, "URI has a fragment component",
                      () -> new File(java.net.URI.create("file:///a/b#f")), "fragment");
            mustThrow(IllegalArgumentException.class, "URI has a query component",
                      () -> new File(java.net.URI.create("file:///a/b?q")), "query");
            mustThrow(IllegalArgumentException.class, "URI has an authority component",
                      () -> new File(java.net.URI.create("file://host/a/b")), "authority");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 19. createTempFile contracts + deleteOnExit idempotence ----
    static void testTempFileContracts() throws Exception {
        File d = newTempDir();
        try {
            mustThrow(IllegalArgumentException.class, "too short",
                      () -> File.createTempFile("ab", null, d), "prefix shorter than 3");
            mustThrow(IllegalArgumentException.class, "too short",
                      () -> File.createTempFile("", null, d), "empty prefix");
            mustThrow(NullPointerException.class, null,
                      () -> File.createTempFile(null, null, d), "null prefix");
            File inTmp = File.createTempFile("jckioH", null, d);
            check(inTmp.exists() && inTmp.isFile(), "createTempFile exists");
            check(inTmp.getName().endsWith(".tmp"), "null suffix defaults to .tmp");
            check(inTmp.getName().startsWith("jckioH"), "prefix kept");
            check(inTmp.getParentFile().equals(d), "created inside explicit dir");
            mustThrow(java.io.IOException.class, null,
                      () -> File.createTempFile("jckioH", ".t", new File(d, "nodir")),
                      "createTempFile in missing dir");
            File def = File.createTempFile("jckioH", ".x");
            check(def.getParentFile().equals(
                      new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile()),
                  "null dir defaults to java.io.tmpdir");
            check(def.delete(), "cleanup default-dir temp file");
            inTmp.deleteOnExit();
            inTmp.deleteOnExit();                          // idempotent
            new File(d, "nope").deleteOnExit();            // missing file: no exception
            passed += 1;                                   // reaching here proves deleteOnExit safety
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 20. space queries + isHidden ----
    static void testSpacesAndHidden() throws Exception {
        File d = newTempDir();
        try {
            check(d.getTotalSpace() > 0, "getTotalSpace > 0 on real dir");
            check(d.getFreeSpace() > 0, "getFreeSpace > 0 on real dir");
            check(d.getUsableSpace() > 0, "getUsableSpace > 0 on real dir");
            File missing = new File(d, "nope");
            check(missing.getTotalSpace() == 0 && missing.getFreeSpace() == 0
                      && missing.getUsableSpace() == 0, "missing file spaces all 0");
            File plain = new File(d, "p.txt");
            check(plain.createNewFile(), "setup plain");
            check(!plain.isHidden(), "plain name not hidden");
            if (File.separatorChar == '/') {              // POSIX: hidden == name starts with '.'
                File hid = new File(d, ".hid");
                check(hid.createNewFile(), "setup dot file");
                check(hid.isHidden(), "dot file hidden on Linux");
                check(new File(d, ".missing").isHidden(), "dot name hidden even when missing");
            }
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 21. serialization round-trip + stable serialVersionUID ----
    static void testFileSerialization() throws Exception {
        File d = newTempDir();
        try {
            File f = new File(d, "ser.txt");
            check(f.createNewFile(), "setup serializable file");
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
                oos.writeObject(f);
            }
            Object back = new java.io.ObjectInputStream(
                    new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check(back instanceof File, "round-trip yields File");
            check(back.equals(f), "round-trip equals");
            check(((File) back).compareTo(f) == 0, "round-trip compareTo 0");
            check(((File) back).getPath().equals(f.getPath()), "round-trip path preserved");
            check(java.io.ObjectStreamClass.lookup(File.class).getSerialVersionUID()
                      == 301077366599181567L, "File serialVersionUID stable (both JDKs)");
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
        testCtorNullsAndMatrix();
        testPathNormalization();
        testSpecialNames();
        testCaseAndCompare();
        testConstantsAndToString();
        testPermissionsMatrix();
        testLastModifiedContracts();
        testCreateDeleteEdges();
        testMkdirMatrix();
        testListContracts();
        testRenameContracts();
        testUriContracts();
        testTempFileContracts();
        testSpacesAndHidden();
        testFileSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
