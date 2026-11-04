/*
 * @test
 * @summary java.nio.file.LinkPermission conformance: name/actions.
 * @run main LinkPermissionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.file.LinkPermission;
import java.security.BasicPermission;
import java.security.PermissionCollection;

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

    static void testLinkPermission() {
        LinkPermission p = new LinkPermission("hard");
        check("hard".equals(p.getName()), "getName hard");
        check("".equals(p.getActions()), "getActions");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new LinkPermission("hard")), "equals hard");
        check(!p.equals(new LinkPermission("symbolic")), "hard != symbolic");
    }

    // ---- appended: white-box structure ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.LinkPermission");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == BasicPermission.class, "superclass BasicPermission");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.security.Guard.class.isAssignableFrom(c), "Guard (via Permission)");
        check(c.getConstructor(String.class) != null, "single-String constructor");
        check(c.getConstructor(String.class, String.class) != null,
              "String,String constructor");
        // computed serialVersionUID, identical on JDK-8 and JDK-27 (measured)
        check(ObjectStreamClass.lookup(c).getSerialVersionUID()
              == -1441492453772213220L, "serialVersionUID stable value");
    }

    // ---- appended: actions/implies/collection/serialization contracts ----
    static void testPermissionContracts() throws Exception {
        LinkPermission hard = new LinkPermission("hard");
        LinkPermission symbolic = new LinkPermission("symbolic");

        // two-arg constructor: empty/null actions accepted, ignored
        LinkPermission hardEmpty = new LinkPermission("hard", "");
        check("hard".equals(hardEmpty.getName()), "two-arg empty actions name");
        check("".equals(hardEmpty.getActions()), "two-arg empty actions ignored");
        check(hardEmpty.equals(hard), "two-arg empty equals one-arg");
        LinkPermission symNull = new LinkPermission("symbolic", null);
        check("symbolic".equals(symNull.getName()), "two-arg null actions name");
        check("".equals(symNull.getActions()), "two-arg null actions -> empty string");

        // implies
        check(hard.implies(hard), "hard implies itself");
        check(hard.implies(new LinkPermission("hard")), "hard implies fresh hard");
        check(!hard.implies(symbolic), "hard does not imply symbolic");
        check(!symbolic.implies(hard), "symbolic does not imply hard");
        check(!hard.implies(null), "implies(null) -> false");
        check(!hard.implies(new java.util.PropertyPermission("hard", "read")),
              "cross-type implies -> false");

        // equals/hashCode contract
        check(hard.hashCode() == new LinkPermission("hard").hashCode(),
              "equal instances hash equally");
        check(!hard.equals(null), "equals(null) -> false");
        check(!hard.equals(new java.lang.RuntimePermission("hard")),
              "cross-class equals -> false");
        check(hard.toString().contains("hard"), "toString carries the name");

        // permission collection
        PermissionCollection pc = hard.newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        pc.add(hard);
        check(pc.implies(new LinkPermission("hard")), "collection implies held permission");
        check(!pc.implies(symbolic), "collection does not imply unheld");

        // serialization round-trip (BasicPermission family is Serializable)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(hard); }
        Object back = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof LinkPermission, "round-trip class preserved");
        check(back.equals(hard) && hard.equals(back), "round-trip equals");
        check(((LinkPermission) back).getName().equals("hard"), "round-trip name");
        check(((LinkPermission) back).getActions().equals(""), "round-trip actions");

        // negative cases with exact exception types
        expect(IllegalArgumentException.class, new Runnable() {
            public void run() { new LinkPermission("basic"); } },
              "unknown name 'basic' -> IllegalArgumentException");
        expect(IllegalArgumentException.class, new Runnable() {
            public void run() { new LinkPermission(""); } },
              "empty name -> IllegalArgumentException");
        expect(IllegalArgumentException.class, new Runnable() {
            public void run() { new LinkPermission("hard", "read"); } },
              "non-empty actions -> IllegalArgumentException");
        expect(IllegalArgumentException.class, new Runnable() {
            public void run() { new LinkPermission("", ""); } },
              "two-arg empty name -> IllegalArgumentException");
        expect(NullPointerException.class, new Runnable() {
            public void run() { new LinkPermission(null); } },
              "null name -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testLinkPermission();
        testWhiteBox();
        testPermissionContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
