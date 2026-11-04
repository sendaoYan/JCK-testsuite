/*
 * @test
 * @summary java.io.FilePermission conformance (pure black-box): canonical action
 *          ordering, duplicate actions, invalid actions, directory "/-"
 *          recursive implies, "/*" flat implies, "<<ALL FILES>>", action-subset
 *          implies, equals/hashCode, permission collection, serialization
 *          round-trip.
 * @run main FilePermissionTest
 */

import java.io.FilePermission;

public class FilePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
