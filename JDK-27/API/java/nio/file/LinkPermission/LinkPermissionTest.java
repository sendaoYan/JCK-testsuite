/*
 * @test
 * @summary java.nio.file.LinkPermission conformance: white-box structure + black-box name/actions/equality.
 * @run main LinkPermissionTest
 */

import java.lang.reflect.Modifier;
import java.nio.file.LinkPermission;
import java.security.BasicPermission;

public class LinkPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.LinkPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == BasicPermission.class, "superclass is BasicPermission");
        check(c.getConstructor(String.class) != null, "String constructor present");
        check(c.getConstructor(String.class, String.class) != null, "String,String constructor present");
    }

    static void testBlackBox() {
        LinkPermission p = new LinkPermission("hard");
        check("hard".equals(p.getName()), "getName hard");
        check("".equals(p.getActions()), "getActions");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new LinkPermission("hard")), "equals hard");
        check(!p.equals(new LinkPermission("symbolic")), "hard != symbolic");
        check(p.hashCode() == new LinkPermission("hard").hashCode(), "equal hashCode");
        check(p.implies(new LinkPermission("hard")), "implies hard");

        LinkPermission sym = new LinkPermission("symbolic");
        check("symbolic".equals(sym.getName()), "getName symbolic");
        check(sym.implies(new LinkPermission("symbolic")), "implies symbolic");
        check(!sym.implies(p), "symbolic does not imply hard");

        // Two-arg constructor: empty or null actions are accepted.
        LinkPermission hardEmpty = new LinkPermission("hard", "");
        check("hard".equals(hardEmpty.getName()), "two-arg name hard");
        check("".equals(hardEmpty.getActions()), "two-arg empty actions");
        check(hardEmpty.equals(p), "two-arg empty actions equals one-arg");

        LinkPermission symbolicNull = new LinkPermission("symbolic", null);
        check("symbolic".equals(symbolicNull.getName()), "two-arg null actions name");
        check("".equals(symbolicNull.getActions()), "two-arg null actions empty");

        expect(IllegalArgumentException.class, () -> new LinkPermission("foo"), "invalid name throws");
        expect(IllegalArgumentException.class, () -> new LinkPermission("hard", "read"), "non-empty actions throws");
    }

    // ---- appended: computed suid + serialization round-trip + collection ----
    static void testSerializationAndCollection() throws Exception {
        // computed serialVersionUID, identical on JDK-8 and JDK-27 (measured)
        check(java.io.ObjectStreamClass.lookup(java.nio.file.LinkPermission.class)
              .getSerialVersionUID() == -1441492453772213220L,
              "serialVersionUID stable value");

        java.nio.file.LinkPermission hard = new java.nio.file.LinkPermission("hard");
        check(java.io.Serializable.class.isAssignableFrom(java.nio.file.LinkPermission.class),
              "Serializable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (var oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(hard); }
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof java.nio.file.LinkPermission, "round-trip class preserved");
        check(back.equals(hard) && hard.equals(back), "round-trip equals");
        check(((java.nio.file.LinkPermission) back).getName().equals("hard"),
              "round-trip name");
        check(((java.nio.file.LinkPermission) back).getActions().equals(""),
              "round-trip actions");

        java.security.PermissionCollection pc = hard.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        pc.add(hard);
        check(pc.implies(new java.nio.file.LinkPermission("hard")),
              "collection implies held permission");
        check(!pc.implies(new java.nio.file.LinkPermission("symbolic")),
              "collection does not imply unheld");
        check(!hard.implies(null), "implies(null) -> false");
        check(!hard.equals(new java.lang.RuntimePermission("hard")),
              "cross-class equals -> false");
        check(hard.toString().contains("hard"), "toString carries the name");
    }

    // ---- appended: extra negative matrix ----
    static void testExtraNegatives() {
        expect(IllegalArgumentException.class, () -> new LinkPermission("basic"),
              "unknown name 'basic' -> IllegalArgumentException");
        expect(IllegalArgumentException.class, () -> new LinkPermission(""),
              "empty name -> IllegalArgumentException");
        expect(IllegalArgumentException.class, () -> new LinkPermission("", ""),
              "two-arg empty name -> IllegalArgumentException");
        expect(NullPointerException.class, () -> new LinkPermission((String) null),
              "null name -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerializationAndCollection();
        testExtraNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
