/*
 * @test
 * @summary java.util.concurrent.StructureViolationException conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main StructureViolationExceptionTest
 */

import java.io.*;
import java.util.concurrent.*;

public class StructureViolationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasCtor(Class<?> c, Class<?>... p) {
        try { c.getConstructor(p); } catch (NoSuchMethodException e) { throw new AssertionError("missing ctor on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.StructureViolationException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        hasCtor(c);
        hasCtor(c, String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StructureViolationException e = new StructureViolationException("m");
        check(e.getMessage().equals("m"), "message");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(new StructureViolationException().getMessage() == null, "no-arg null message");
    }

    // ---- Part 3: Throwable-level contract ----
    static void testThrowableContract() {
        StructureViolationException e = new StructureViolationException("boom");
        check("java.util.concurrent.StructureViolationException: boom".equals(e.toString()),
              "toString is ClassName: message");
        check(e.getCause() == null, "no cause unless explicitly given");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");
        StructureViolationException e0 = new StructureViolationException();
        check("java.util.concurrent.StructureViolationException".equals(e0.toString()),
              "toString with null message is just the class name");
        check(e0.initCause(new Exception()) == e0, "initCause returns this");
        check(e0.getCause() != null, "initCause sets the cause");
    }

    // ---- Part 4: serialization contract (suid lock + round-trip) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(StructureViolationException.class).getSerialVersionUID()
                  == -7705327650798235468L,
              "serialVersionUID locked (-7705327650798235468)");

        StructureViolationException orig = new StructureViolationException("boom");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        StructureViolationException copy = (StructureViolationException) ois.readObject();
        ois.close();
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() == null, "round-trip null cause stays null");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");
    }

    // ---- Part 5: deterministic StructuredTaskScope nesting violation ----
    static void testScopeNestingViolation() throws Exception {
        // Two scopes opened by the same thread nest: parent first, then child.
        // Closing the PARENT while the child is still open violates the tree
        // structure and must throw StructureViolationException (message null).
        var parent = StructuredTaskScope.open();
        var child = StructuredTaskScope.open();
        try {
            parent.close();
            check(false, "closing parent with open child should throw");
        } catch (StructureViolationException expected) {
            check(expected.getMessage() == null, "scope-violation message is null");
            check(expected.getCause() == null, "scope-violation has no cause");
            check(expected.getStackTrace().length > 0, "stack trace captured in the scope");
        }
        // unwind the leaked scopes (best effort; state after violation is unspecified)
        try { child.close(); } catch (Throwable ignore) { }
        try { parent.close(); } catch (Throwable ignore) { }

        // well-formed nesting never throws: child closed before parent
        var ok1 = StructuredTaskScope.open();
        var ok2 = StructuredTaskScope.open();
        ok2.close();
        ok1.close();
        check(true, "well-formed nested scopes close without exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableContract();
        testSerializationContract();
        testScopeNestingViolation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
