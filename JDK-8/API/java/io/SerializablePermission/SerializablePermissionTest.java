/*
 * @test
 * @summary java.io.SerializablePermission API conformance: name/actions and inheritance.
 * @run main SerializablePermissionTest
 */

import java.io.SerializablePermission;
import java.security.BasicPermission;

public class SerializablePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSerializablePermission() {
        SerializablePermission p = new SerializablePermission("enableSubstitution");
        check(p instanceof BasicPermission, "extends BasicPermission");
        check("enableSubstitution".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions is empty");
    }

    public static void main(String[] args) {
        testSerializablePermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
