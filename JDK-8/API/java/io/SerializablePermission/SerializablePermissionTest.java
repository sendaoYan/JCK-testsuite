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

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
