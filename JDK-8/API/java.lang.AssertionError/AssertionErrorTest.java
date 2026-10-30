/*
 * @test
 * @summary java.lang.AssertionError API conformance: Error hierarchy, all constructors
 *          (no-arg, primitive/Object, and (Object,Throwable)), and the assert statement
 *          (run with -ea) throwing AssertionError carrying the detail message.
 * @run main/othervm -ea AssertionErrorTest
 */

public class AssertionErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        check(new AssertionError() instanceof Error, "AssertionError is an Error");
        check(new AssertionError() instanceof Throwable, "AssertionError is a Throwable");
    }

    static void testConstructors() {
        check(new AssertionError().getMessage() == null, "no-arg null message");
        check(new AssertionError(true).getMessage().equals("true"), "(boolean) message");
        check(new AssertionError('x').getMessage().equals("x"), "(char) message");
        check(new AssertionError(42).getMessage().equals("42"), "(int) message");
        check(new AssertionError(42L).getMessage().equals("42"), "(long) message");
        check(new AssertionError(1.5f).getMessage().equals("1.5"), "(float) message");
        check(new AssertionError(1.5).getMessage().equals("1.5"), "(double) message");
        check(new AssertionError((Object) "msg").getMessage().equals("msg"), "(Object) message");
        check(new AssertionError((Object) null).getMessage().equals("null"),
              "(Object null) message is \"null\"");
        Throwable cause = new Throwable("c");
        AssertionError e = new AssertionError("msg", cause);
        check("msg".equals(e.getMessage()) && e.getCause() == cause, "(Object,Throwable) ctor");
    }

    static void testAssertStatement() {
        try {
            assert false : "boom";
            throw new AssertionError("assert not enabled");
        } catch (AssertionError e) {
            check("boom".equals(e.getMessage()), "assert false throws AssertionError with message");
        }
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testAssertStatement();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
