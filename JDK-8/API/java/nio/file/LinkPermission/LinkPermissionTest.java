/*
 * @test
 * @summary java.nio.file.LinkPermission conformance: name/actions.
 * @run main LinkPermissionTest
 */

import java.nio.file.LinkPermission;
import java.security.BasicPermission;

public class LinkPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLinkPermission() {
        LinkPermission p = new LinkPermission("hard");
        check("hard".equals(p.getName()), "getName hard");
        check("".equals(p.getActions()), "getActions");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new LinkPermission("hard")), "equals hard");
        check(!p.equals(new LinkPermission("symbolic")), "hard != symbolic");
    }

    public static void main(String[] args) {
        testLinkPermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
