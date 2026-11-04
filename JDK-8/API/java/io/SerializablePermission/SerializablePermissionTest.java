/*
 * @test
 * @summary java.io.SerializablePermission conformance (pure black-box): named
 *          permissions, BasicPermission name validation, implies by exact name,
 *          empty actions, ignored actions argument, equals/hashCode,
 *          serialization round-trip.
 * @run main SerializablePermissionTest
 */

import java.io.SerializablePermission;

public class SerializablePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. the two documented target names construct cleanly
        SerializablePermission sub = new SerializablePermission("enableSubclassImplementation");
        SerializablePermission subst = new SerializablePermission("enableSubstitution");
        check("enableSubclassImplementation".equals(sub.getName()), "name 1");
        check("enableSubstitution".equals(subst.getName()), "name 2");
        check("".equals(sub.getActions()), "actions are empty");
        check(!sub.equals(subst), "distinct names are distinct permissions");

        // 2. implies is exact-name matching here, no wildcarding by suffix
        check(sub.implies(new SerializablePermission("enableSubclassImplementation")),
              "implies same name");
        check(!sub.implies(new SerializablePermission("enableSubstitution")),
              "does not imply a different name");
        check(!sub.implies(new java.security.AllPermission()), "not unrelated types");
        check(!sub.implies(null), "not null");

        // 3. BasicPermission name validation
        try {
            new SerializablePermission(null);
            check(false, "null name must throw");
        } catch (NullPointerException npe) {
            check("name can't be null".equals(npe.getMessage()), "message: " + npe.getMessage());
        }
        try {
            new SerializablePermission("");
            check(false, "empty name must throw");
        } catch (IllegalArgumentException iae) {
            check("name can't be empty".equals(iae.getMessage()), "message: " + iae.getMessage());
        }

        // 4. the two-arg ctor ignores the actions string entirely
        SerializablePermission two = new SerializablePermission("enableSubstitution", "read");
        check("".equals(two.getActions()), "actions argument ignored");
        check(two.equals(subst), "two-arg equals one-arg with the same name");

        // 5. equals/hashCode contract
        SerializablePermission sub2 = new SerializablePermission("enableSubclassImplementation");
        check(sub.equals(sub2) && sub.hashCode() == sub2.hashCode(), "equal with equal hash");
        check(!sub.equals(null), "not equal to null");
        check(sub.hashCode() == sub.hashCode(), "stable hashCode");

        // 6. wildcard names do not act as wildcards for implies in this class
        SerializablePermission star = new SerializablePermission("enable*");
        check(!star.implies(new SerializablePermission("enableSubstitution")),
              "'enable*' does not wildcard-match");
        check(star.implies(new SerializablePermission("enable*")), "implies itself only");

        // 7. serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(sub);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            Object back = ois.readObject();
            check(back.equals(sub), "serialization round-trip equality");
        }
    }


    // ================= deep additions (contract level) =================

    // ---- BasicPermission wildcard semantics (name grammar of the base class) ----
    static void testWildcardMatrix() {
        SerializablePermission star = new SerializablePermission("*");
        check(star.implies(new SerializablePermission("enableSubstitution")),
              "'*' implies any specific name");
        check(star.implies(new SerializablePermission("a.b.c")), "'*' implies dotted names");
        check(star.implies(star), "'*' implies itself");
        SerializablePermission dotted = new SerializablePermission("a.*");
        check(dotted.implies(new SerializablePermission("a.b")),
              "'a.*' implies a direct child");
        check(dotted.implies(new SerializablePermission("a.b.c")),
              "'a.*' implies deep children");
        check(!dotted.implies(new SerializablePermission("a")),
              "'a.*' does not imply the bare parent name");
        check(!dotted.implies(new SerializablePermission("b.a")),
              "'a.*' does not imply names outside the subtree");
        SerializablePermission midStar = new SerializablePermission("enable*ables");
        check(!midStar.implies(new SerializablePermission("enableXables")),
              "an internal '*' is a literal character, not a wildcard");
        check(midStar.implies(new SerializablePermission("enable*ables")),
              "the literal name implies only itself");
        check(!new SerializablePermission("a.b").implies(dotted),
              "a specific name does not imply the wildcard");
    }

    // ---- class hierarchy and finality ----
    static void testHierarchy() {
        check(java.security.BasicPermission.class.isAssignableFrom(SerializablePermission.class),
              "extends BasicPermission");
        check(java.security.Permission.class.isAssignableFrom(SerializablePermission.class),
              "therefore extends Permission");
        check(java.lang.reflect.Modifier.isFinal(SerializablePermission.class.getModifiers()),
              "the class is final");
        check(java.io.Serializable.class.isAssignableFrom(SerializablePermission.class),
              "permissions are serializable");
    }

    // ---- permission collections ----
    static void testCollection() {
        java.security.PermissionCollection pc =
                new SerializablePermission("enableSubstitution").newPermissionCollection();
        check(pc != null, "newPermissionCollection returns a collection");
        pc.add(new SerializablePermission("enableSubstitution"));
        check(pc.implies(new SerializablePermission("enableSubstitution")),
              "collection implies the held permission");
        check(!pc.implies(new SerializablePermission("enableSubstitution2")),
              "collection does not imply an unrelated name");
        check(!pc.isReadOnly(), "fresh collection is writable");
        pc.setReadOnly();
        check(pc.isReadOnly(), "setReadOnly flips the flag");
    }

    // ---- name acceptance and case sensitivity ----
    static void testNameAcceptance() {
        check(new SerializablePermission("a/b") != null, "'/' accepted in names");
        check(new SerializablePermission("a..b") != null, "'..' accepted in names");
        check(new SerializablePermission("serialFilter") != null,
              "'serialFilter' is a legal name on every JDK");
        check(!new SerializablePermission("X").implies(new SerializablePermission("x")),
              "names are case sensitive");
        check(!new SerializablePermission("enableSubstitution")
                  .equals(new SerializablePermission("EnableSubstitution")),
              "case difference breaks equals");
    }

    // ---- toString/getActions surface ----
    static void testSurface() {
        SerializablePermission p = new SerializablePermission("enableSubstitution", "read,write");
        String ts = p.toString();
        check(ts.contains("java.io.SerializablePermission"), "toString carries the class name");
        check(ts.contains("enableSubstitution"), "toString carries the name");
        check("".equals(p.getActions()), "getActions is empty regardless of the ctor argument");
        check(p.equals(new SerializablePermission("enableSubstitution")),
              "actions argument does not affect equality");
        check(new SerializablePermission("a", null) != null,
              "null actions are accepted and ignored");
    }

    // ---- deprecation status (JDK-8: not deprecated) ----
    static void testDeprecation() {
        check(SerializablePermission.class.getAnnotation(Deprecated.class) == null,
              "not deprecated on JDK-8");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testWildcardMatrix();
        testHierarchy();
        testCollection();
        testNameAcceptance();
        testSurface();
        testDeprecation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
