/*
 * @test
 * @summary java.nio.channels.MembershipKey conformance: abstract surface.
 * @run main MembershipKeyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.MembershipKey;
import java.util.HashSet;
import java.util.Set;

public class MembershipKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMembershipKey() {
        check(Modifier.isAbstract(MembershipKey.class.getModifiers()), "abstract");
        Set<String> names = new HashSet<String>();
        for (Method m : MembershipKey.class.getMethods()) names.add(m.getName());
        String[] required = {"isValid", "drop", "block", "unblock", "channel", "group",
                             "networkInterface", "sourceAddress"};
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testMembershipKey();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
