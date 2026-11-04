/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipalNotFoundException conformance: name.
 * @run main UserPrincipalNotFoundExceptionTest
 */

import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;

public class UserPrincipalNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUserPrincipalNotFoundException() {
        check(IOException.class.isAssignableFrom(UserPrincipalNotFoundException.class),
              "extends IOException");
        UserPrincipalNotFoundException e = new UserPrincipalNotFoundException("alice");
        check("alice".equals(e.getName()), "getName");
    }

    public static void main(String[] args) {
        testUserPrincipalNotFoundException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
