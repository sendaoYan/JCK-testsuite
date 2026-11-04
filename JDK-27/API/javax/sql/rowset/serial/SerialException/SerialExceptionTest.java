/*
 * @test
 * @summary javax.sql.rowset.serial.SerialException conformance: white-box structure + black-box behavior.
 * @run main SerialExceptionTest
 */

public class SerialExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialException");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.sql.SQLException"), "superclass java.sql.SQLException");
        check(c.getInterfaces().length == 0, "0 direct interface(s)");
        check(c.getDeclaredMethods().length == 0, "declared method count 0");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.sql.rowset.serial.SerialException e0 = new javax.sql.rowset.serial.SerialException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getSQLState() == null, "no-arg SQLState null");
        check(e0.getErrorCode() == 0, "no-arg error code 0");
        check(e0 instanceof java.sql.SQLException, "extends SQLException");

        javax.sql.rowset.serial.SerialException e1 = new javax.sql.rowset.serial.SerialException("boom");
        check("boom".equals(e1.getMessage()), "String ctor message");
        check(e1.getSQLState() == null, "String ctor SQLState null (inherited default)");
        check(e1.getErrorCode() == 0, "String ctor error code 0");

        // The exception carries a cause/chain like any Throwable.
        java.sql.SQLException cause = new java.sql.SQLException("cause");
        e1.initCause(cause);
        check(e1.getCause() == cause, "getCause reflects initCause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
