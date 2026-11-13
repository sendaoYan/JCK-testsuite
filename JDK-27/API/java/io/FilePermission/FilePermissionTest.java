/*
 * @test
 * @summary java.io.FilePermission conformance: white-box structure (final
 *          Permission subclass, deprecated for removal in 25) + black-box
 *          behavior (canonical action ordering, duplicate actions, invalid
 *          actions, directory/- recursive implies, /* flat implies, <<ALL FILES>>,
 *          action-subset implies, equals/hashCode, permission collection,
 *          serialization round-trip).
 * @run main FilePermissionTest
 */

import java.io.FilePermission;

public class FilePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    @SuppressWarnings("removal")
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FilePermission");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        Deprecated dep = c.getAnnotation(Deprecated.class);
        check(dep != null && dep.forRemoval(), "deprecated for removal");
        check(c.getConstructor(String.class, String.class) != null, "ctor (String, String)");
        check(c.getMethod("implies", java.security.Permission.class) != null, "implies");
        check(c.getMethod("getActions").getReturnType() == String.class, "getActions");
        check(c.getMethod("newPermissionCollection").getReturnType()
                  == java.security.PermissionCollection.class, "newPermissionCollection");
    }

    // ---- Part 2: black-box (behavioral) ----
    @SuppressWarnings("removal")
    static void testBlackBox() throws Exception {
        // 1. actions normalize to the canonical order read,write,execute,delete
        check("read,write".equals(new FilePermission("/tmp/x", "write,read").getActions()),
              "reordered input canonicalized");
        check("read,write,execute,delete".equals(
                  new FilePermission("/tmp/x", "delete,execute,write,read").getActions()),
              "full set canonical order");
        check("read".equals(new FilePermission("/a", "read,read").getActions()),
              "duplicate actions collapse");

        // 2. invalid actions are rejected
        try {
            new FilePermission("/a", "read,swim");
            check(false, "invalid action must throw");
        } catch (IllegalArgumentException iae) {
            check("invalid permission: read,swim".equals(iae.getMessage()),
                  "message: " + iae.getMessage());
        }
        try {
            new FilePermission("/a", "");
            check(false, "empty actions must throw");
        } catch (IllegalArgumentException iae) {
            passed++;
        }

        // 3. directory "/-" implies any depth below it, but not siblings
        FilePermission rec = new FilePermission("/tmp/-", "read");
        check(rec.implies(new FilePermission("/tmp/a", "read")), "recursive implies child");
        check(rec.implies(new FilePermission("/tmp/a/b/c", "read")),
              "recursive implies deep descendant");
        check(!rec.implies(new FilePermission("/tmpx/a", "read")), "not a sibling directory");
        check(!rec.implies(new FilePermission("/tmp", "read")), "not the directory itself");
        check(!rec.implies(new FilePermission("/tmp/a", "write")),
              "actions must be a subset for implies");

        // 4. "/*" implies only the flat children
        FilePermission flat = new FilePermission("/tmp/*", "read");
        check(flat.implies(new FilePermission("/tmp/a", "read")), "flat implies direct child");
        check(!flat.implies(new FilePermission("/tmp/a/b", "read")),
              "flat does not imply deeper");

        // 5. "<<ALL FILES>>" implies any path
        check(new FilePermission("<<ALL FILES>>", "read")
                  .implies(new FilePermission("/anything/deep", "read")),
              "ALL FILES implies everything");

        // 6. exact file implies itself, supersets fail
        check(new FilePermission("/x", "read,write").implies(new FilePermission("/x", "read")),
              "superset of actions implies the subset");
        check(!new FilePermission("/x", "read").implies(new FilePermission("/x", "read,write")),
              "subset of actions does not imply the superset");
        check(!new FilePermission("/x", "read").implies(new FilePermission("/y", "read")),
              "different paths");

        // 7. implies never matches unrelated permission types or null
        check(!new FilePermission("/a", "read").implies(new java.security.AllPermission()),
              "not an unrelated Permission type");
        check(!new FilePermission("/a", "read").implies(null), "not null");

        // 8. equals and hashCode follow name+mask
        FilePermission a = new FilePermission("/tmp/x", "read,write");
        FilePermission b = new FilePermission("/tmp/x", "write,read");
        check(a.equals(b) && b.equals(a), "equal despite action order");
        check(a.hashCode() == b.hashCode(), "equal hash codes");
        check(!a.equals(new FilePermission("/tmp/y", "read,write")), "different path");
        check(!a.equals(new FilePermission("/tmp/x", "read")), "different actions");
        check(!a.equals(null), "not equal to null");

        // 9. wildcard * kept literal in a name: equals-matching semantics
        FilePermission star = new FilePermission("/tmp/a*", "read");
        check(star.implies(new FilePermission("/tmp/a*", "read")), "literal * implies itself");
        check(!star.implies(new FilePermission("/tmp/abc", "read")),
              "a lone trailing * is not a wildcard in the middle of a name");

        // 10. newPermissionCollection returns a usable collection
        java.security.PermissionCollection pc = a.newPermissionCollection();
        pc.add(a);
        check(pc.implies(new FilePermission("/tmp/x", "read")),
              "collection implies an added member");
        check(!pc.implies(new FilePermission("/tmp/z", "read")),
              "collection does not imply others");

        // 11. serialization round-trip preserves equality
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.readObject().equals(a), "serialization round-trip");
        }
    }

    // ================= 深化追加（契约级） =================
    // 以下行为均先以探针在 JDK-8 与 JDK-27 双侧实测锁定（见 DESIGN.md「深化追加」）。
    // 注意：null actions 双侧均为 IllegalArgumentException("invalid actions mask")，不是 NPE。

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

    // ---- 12. constructor contracts: null path / null actions / empty / junk ----
    static void testCtorContracts() {
        mustThrow(NullPointerException.class, "name can't be null",
                  () -> new FilePermission(null, "read"), "null path");
        mustThrow(IllegalArgumentException.class, "invalid actions mask",
                  () -> new FilePermission("/a", (String) null), "null actions is IAE, not NPE");
        mustThrow(IllegalArgumentException.class, "invalid actions mask",
                  () -> new FilePermission("/a", ""), "empty actions string");
        mustThrow(IllegalArgumentException.class, "invalid permission:",
                  () -> new FilePermission("/a", "   "), "whitespace-only actions");
        mustThrow(IllegalArgumentException.class, "invalid permission: readx",
                  () -> new FilePermission("/a", "readx"), "unknown action");
        mustThrow(IllegalArgumentException.class, "invalid permission: read,,write",
                  () -> new FilePermission("/a", "read,,write"), "empty token between commas");
        mustThrow(IllegalArgumentException.class, "invalid permission: read,",
                  () -> new FilePermission("/a", "read,"), "trailing comma");
    }

    // ---- 13. action string normalization: case-insensitive, whitespace tolerated ----
    static void testActionNormalization() {
        check("read".equals(new FilePermission("/a", "READ").getActions()), "uppercase input normalized");
        check("read".equals(new FilePermission("/a", "Read").getActions()), "mixed case normalized");
        check("read,write".equals(new FilePermission("/a", " read , WRITE ").getActions()),
              "whitespace around tokens tolerated");
        check("write".equals(new FilePermission("/a", "write").getActions()), "single write");
        check("execute".equals(new FilePermission("/a", "execute").getActions()), "single execute");
        check("delete".equals(new FilePermission("/a", "delete").getActions()), "single delete");
        check("read,write,execute,delete".equals(new FilePermission("/a", "  delete , Read , execute , WrItE ").getActions()),
              "full set from messy input");
        check("read,write".equals(new FilePermission("/a", "write,read,read").getActions()),
              "duplicates collapse after reordering");
    }

    // ---- 14. implies matrix: roots, cwd, flat vs recursive, case, action subsets ----
    static void testImpliesMatrix() {
        check(new FilePermission("/-", "read").implies(new FilePermission("/a/b", "read")),
              "root /- is recursive over everything");
        check(new FilePermission("/-", "read").implies(new FilePermission("/tmp/-", "read")),
              "root /- implies another recursive grant");
        check(new FilePermission("<<ALL FILES>>", "read").implies(new FilePermission("foo", "read")),
              "ALL FILES implies a relative path");
        check(new FilePermission("*", "read").implies(new FilePermission("foo", "read")),
              "cwd * implies a cwd file");
        check(!new FilePermission("*", "read").implies(new FilePermission("dir/foo", "read")),
              "cwd * does not imply a subdirectory file");
        FilePermission rec = new FilePermission("/tmp/-", "read");
        check(rec.implies(new FilePermission("/tmp/-", "read")), "recursive grant implies itself");
        check(!new FilePermission("/tmp/*", "read").implies(new FilePermission("/tmp/-", "read")),
              "flat does not imply a recursive grant for the same dir");
        check(!new FilePermission("/tmp/*", "read").implies(new FilePermission("/tmp/x/-", "read")),
              "flat does not imply recursive under a child");
        if (java.io.File.separatorChar == '/') {
            check(!new FilePermission("/tmp/A", "read").implies(new FilePermission("/tmp/a", "read")),
                  "Linux paths are case-sensitive in implies");
        } else {
            // Windows 文件系统大小写不敏感：FilePermission 用 WindowsPath 比较路径，
            // WindowsPath.compareTo 走 equalsIgnoreCase，故大小写不同仍互相蕴含。
            check(new FilePermission("/tmp/A", "read").implies(new FilePermission("/tmp/a", "read")),
                  "Windows: implies is case-insensitive");
        }
        FilePermission full = new FilePermission("/x", "read,write,execute,delete");
        check(full.implies(new FilePermission("/x", "read")), "full implies read");
        check(full.implies(new FilePermission("/x", "write")), "full implies write");
        check(full.implies(new FilePermission("/x", "execute")), "full implies execute");
        check(full.implies(new FilePermission("/x", "delete")), "full implies delete");
        check(!new FilePermission("/x", "execute").implies(new FilePermission("/x", "read")),
              "execute does not imply read");
        check(!new FilePermission("/tmp/x", "read").implies(new FilePermission("/tmp/xy", "read")),
              "path prefix must not match a longer name");
    }

    // ---- 15. equals/hashCode beyond name+mask ----
    static void testEqualsContracts() {
        check(new FilePermission("<<ALL FILES>>", "read")
                  .equals(new FilePermission("<<ALL FILES>>", "read")), "ALL FILES equals pair");
        check(!new FilePermission("<<ALL FILES>>", "read")
                  .equals(new FilePermission("<<ALL FILES>>", "read,write")), "ALL FILES actions differ");
        check(new FilePermission("/a", "read").hashCode()
                  == new FilePermission("/a", "read").hashCode(), "equal permissions hash equally");
        check(new FilePermission("/a", "read").equals(new FilePermission("/a", "read")), "reflexive");
    }

    // ---- 16. PermissionCollection aggregation ----
    static void testCollectionContracts() {
        java.security.PermissionCollection pc =
                new FilePermission("/a", "read").newPermissionCollection();
        pc.add(new FilePermission("/a", "read"));
        pc.add(new FilePermission("/a", "write"));
        check(pc.implies(new FilePermission("/a", "read,write")),
              "union of same-path grants implies the combined actions");
        pc.add(new FilePermission("/b/-", "read"));
        check(pc.implies(new FilePermission("/b/deep/file", "read")),
              "recursive grant aggregated into the collection");
        check(!pc.implies(new FilePermission("/c", "read")), "unrelated path not implied");
        check(!pc.implies(new FilePermission("/a", "execute")), "missing action not implied");
        check(!pc.implies(null), "implies(null) is false");
        int n = 0;
        java.util.Enumeration<java.security.Permission> en = pc.elements();
        while (en.hasMoreElements()) { n++; en.nextElement(); }
        check(n == 2, "JDK-27: same-path adds are merged into one combined element");
        java.security.PermissionCollection hetero =
                new FilePermission("/c", "read").newPermissionCollection();
        hetero.add(new FilePermission("/c", "read"));
        hetero.add(new FilePermission("/d", "write"));
        int m = 0;
        java.util.Enumeration<java.security.Permission> hen = hetero.elements();
        while (hen.hasMoreElements()) { m++; hen.nextElement(); }
        check(m == 2, "distinct paths enumerate one element each");
        java.security.PermissionCollection pc2 =
                new FilePermission("/a", "read").newPermissionCollection();
        check(!pc2.implies(new FilePermission("/a", "read")), "a fresh collection implies nothing");
    }

    // ---- 17. toString carries name and canonical actions ----
    static void testToStringContract() {
        FilePermission p = new FilePermission("/tmp/x", "write,read");
        String s = p.toString();
        // Permission.getName() 返回构造时传入的原始串（不随平台规范化），
        // 因此这里的非规范输入在 Windows 上同样原样出现在 toString 中。
        check(s.contains("/tmp/x"), "toString contains the path");
        check(s.contains("read,write"), "toString contains canonical actions");
        check(s.contains("java.io.FilePermission"), "toString contains the class name");
    }

    // ---- 18. serialization preserves implies/getActions/hashCode ----
    static void testSerializationDeep() throws Exception {
        FilePermission p = new FilePermission("/tmp/-", "write,read,delete");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(p);
        }
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof FilePermission, "round-trip type");
        FilePermission q = (FilePermission) back;
        check(q.equals(p) && q.hashCode() == p.hashCode(), "round-trip equals/hashCode");
        check("read,write,delete".equals(q.getActions()), "round-trip canonical actions kept");
        check(q.implies(new FilePermission("/tmp/kid", "read")), "round-trip implies child read");
        check(q.implies(new FilePermission("/tmp/d/e/f", "delete")), "round-trip implies deep delete");
        check(!q.implies(new FilePermission("/tmp/kid", "execute")), "round-trip does not imply execute");
        check(!q.implies(new FilePermission("/other", "read")), "round-trip keeps path scoping");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorContracts();
        testActionNormalization();
        testImpliesMatrix();
        testEqualsContracts();
        testCollectionContracts();
        testToStringContract();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
