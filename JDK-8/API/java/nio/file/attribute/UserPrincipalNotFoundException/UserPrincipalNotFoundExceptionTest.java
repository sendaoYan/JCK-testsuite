/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipalNotFoundException conformance: name.
 * @run main UserPrincipalNotFoundExceptionTest
 */

import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.nio.file.attribute.UserPrincipalLookupService;

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

    public static void main(String[] args) throws Exception {
        testUserPrincipalNotFoundException();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===== A12 contract deepening (appended) =====
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    interface IORunnable { void run() throws java.io.IOException; }

    static void checkThrowsIO(Class<? extends Throwable> type, IORunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void cleanup(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                         java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) cleanup(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    static void testContract() throws Exception {
        // constructor and getters
        UserPrincipalNotFoundException e = new UserPrincipalNotFoundException("alice");
        check("alice".equals(e.getName()), "ctor name round-trip");
        check(e.getMessage() == null, "getMessage is null (name is not the message)");
        check(e.getCause() == null, "getCause null");
        check(e instanceof java.io.IOException, "is an IOException");
        check(java.io.IOException.class.isAssignableFrom(UserPrincipalNotFoundException.class),
              "extends IOException");
        check(new UserPrincipalNotFoundException(null).getName() == null, "ctor(null) name is null");
        // toString has no detail suffix because the message is null
        check(e.toString().equals("java.nio.file.attribute.UserPrincipalNotFoundException"), "toString exact");
        // identity equality (exception not a value type)
        check(!e.equals(new UserPrincipalNotFoundException("alice")), "not equal to same-name twin");
        check(e.equals(e), "equal to self");
        check(e.hashCode() == e.hashCode(), "hashCode stable");
        // cause management inherited from Throwable
        RuntimeException cause = new RuntimeException("why");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "initCause visible via getCause");
        // serialization contract: fixed serialVersionUID + name survives a round trip
        check(java.io.ObjectStreamClass.lookup(UserPrincipalNotFoundException.class).getSerialVersionUID()
                == -5369283889045833024L, "serialVersionUID fixed");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new UserPrincipalNotFoundException("bob"));
        }
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))
                .readObject();
        check(back instanceof UserPrincipalNotFoundException, "round-trip class");
        check("bob".equals(((UserPrincipalNotFoundException) back).getName()), "round-trip name preserved");
        // thrown by the default lookup service for unknown principals
        UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
        try {
            ls.lookupPrincipalByName("nosuchuser_a12");
            check(false, "unknown user must throw UserPrincipalNotFoundException");
        } catch (UserPrincipalNotFoundException ex) {
            check("nosuchuser_a12".equals(ex.getName()), "service UPNFE carries requested name");
        }
        try {
            try { ls.lookupPrincipalByName("nosuchuser_a12_b"); }
            catch (java.io.IOException en) { throw new java.io.IOException(en); }
            check(false, "unknown user must throw");
        } catch (java.io.IOException en) {
            check(en.getCause() instanceof UserPrincipalNotFoundException,
                  "UPNFE is catchable as IOException (wrapped here)");
        }
    }

}
