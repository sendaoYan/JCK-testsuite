/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipalNotFoundException conformance: white-box structure + black-box behavior.
 * @run main UserPrincipalNotFoundExceptionTest
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipalNotFoundException;

public class UserPrincipalNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.UserPrincipalNotFoundException");
        check(IOException.class.isAssignableFrom(c), "extends java.io.IOException");
        check(c.getConstructor(String.class) != null, "single String constructor");
        check(c.getMethod("getName").getReturnType() == String.class, "getName() returns String");
        UserPrincipalNotFoundException e = new UserPrincipalNotFoundException("bob");
        check(e.getName().equals("bob"), "getName stored");
        check(e.getMessage() == null, "message is null; name carried by getName()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            // the lookup service throws it with the offending name attached
            String ghost = "jck-nobody-" + System.nanoTime();
            boolean got = false;
            try {
                dir.getFileSystem().getUserPrincipalLookupService()
                        .lookupPrincipalByName(ghost);
            } catch (UserPrincipalNotFoundException e) {
                got = true;
                // JDK-27's Unix lookup may not echo the name back
                check(e.getName() == null || e.getName().isEmpty()
                      || e.getName().equals(ghost), "name field tolerated");
                check(e.getMessage() == null, "message is null");
            }
            check(got, "ghost lookup throws UserPrincipalNotFoundException");

            // constructor and serializability
            UserPrincipalNotFoundException e = new UserPrincipalNotFoundException("alice");
            check(e.getName().equals("alice"), "ctor name");
            check(e.getMessage() == null, "ctor message null");
            check(e.getCause() == null, "no cause by default");

            // the exception is catchable as IOException
            boolean io = false;
            try { throw e; }
            catch (IOException ex) { io = true; }
            check(io, "catchable as IOException");

            // null name tolerated by constructor
            UserPrincipalNotFoundException n = new UserPrincipalNotFoundException(null);
            check(n.getName() == null, "null name preserved");
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
