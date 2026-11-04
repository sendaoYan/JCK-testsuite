/*
 * @test
 * @summary java.sql.SQLWarning API conformance: full contract coverage.
 *          extends SQLException, all 8 constructors and message/SQLState/
 *          vendorCode/cause defaults, setNextWarning/getNextWarning chain
 *          append semantics, the getNextWarning() Error thrown when the
 *          chained value is a non-SQLWarning SQLException, warning-chaining
 *          via inherited setNextException, Iterable<Throwable> iterator,
 *          serialization round-trip + serialVersionUID.
 * @run main SQLWarningTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SQLWarningTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testChain();
        testGetNextWarningErrorOnNonWarning();
        testIterator();
        testHierarchy();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstructors() {
        java.sql.SQLWarning w0 = new java.sql.SQLWarning();
        check(w0.getMessage() == null, "no-arg message null");
        check(w0.getSQLState() == null, "no-arg SQLState null");
        check(w0.getErrorCode() == 0, "no-arg errorCode 0");

        java.sql.SQLWarning w1 = new java.sql.SQLWarning("boom");
        check("boom".equals(w1.getMessage()), "(String) message");
        check(w1.getSQLState() == null, "(String) SQLState null");

        java.sql.SQLWarning w2 = new java.sql.SQLWarning("boom", "01000");
        check("01000".equals(w2.getSQLState()), "(String,String) SQLState");
        check(w2.getErrorCode() == 0, "(String,String) errorCode 0");

        java.sql.SQLWarning w3 = new java.sql.SQLWarning("boom", "01000", 5);
        check("01000".equals(w3.getSQLState()), "(String,String,int) SQLState");
        check(w3.getErrorCode() == 5, "(String,String,int) errorCode");

        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLWarning w4 = new java.sql.SQLWarning(cause);
        check(w4.getCause() == cause, "(Throwable) cause");
        check(cause.toString().equals(w4.getMessage()), "(Throwable) message = cause.toString()");

        java.sql.SQLWarning w5 = new java.sql.SQLWarning("r", cause);
        check("r".equals(w5.getMessage()), "(String,Throwable) message");
        check(w5.getCause() == cause, "(String,Throwable) cause");

        java.sql.SQLWarning w6 = new java.sql.SQLWarning("r", "01000", cause);
        check("01000".equals(w6.getSQLState()), "(String,String,Throwable) SQLState");
        check(w6.getCause() == cause, "(String,String,Throwable) cause");
        check(w6.getErrorCode() == 0, "(String,String,Throwable) errorCode 0");

        java.sql.SQLWarning w7 = new java.sql.SQLWarning("r", "01000", 5, cause);
        check("01000".equals(w7.getSQLState()), "(String,String,int,Throwable) SQLState");
        check(w7.getErrorCode() == 5, "(String,String,int,Throwable) errorCode");
        check(w7.getCause() == cause, "(String,String,int,Throwable) cause");
    }

    static void testChain() {
        java.sql.SQLWarning head = new java.sql.SQLWarning("head");
        java.sql.SQLWarning w1 = new java.sql.SQLWarning("w1");
        java.sql.SQLWarning w2 = new java.sql.SQLWarning("w2");
        head.setNextWarning(w1);
        head.setNextWarning(w2);
        check(head.getNextWarning() == w1, "first setNextWarning is head.next");
        check(w1.getNextWarning() == w2, "second setNextWarning appends to end");
        check(w2.getNextWarning() == null, "tail has no next");
        check(head.getNextException() == w1, "getNextException aliases getNextWarning");
    }

    static void testGetNextWarningErrorOnNonWarning() {
        final java.sql.SQLWarning head = new java.sql.SQLWarning("head");
        head.setNextException(new java.sql.SQLException("plain exception"));
        checkThrows(Error.class, new ThrowingRunnable() {
            public void run() { head.getNextWarning(); }
        }, "getNextWarning on non-warning chain");
    }

    static void testIterator() {
        java.sql.SQLWarning head = new java.sql.SQLWarning("head");
        java.sql.SQLWarning w1 = new java.sql.SQLWarning("w1");
        java.sql.SQLWarning w2 = new java.sql.SQLWarning("w2");
        head.setNextWarning(w1);
        w1.setNextWarning(w2);

        List<Throwable> expected = new ArrayList<Throwable>();
        expected.add(head);
        expected.add(w1);
        expected.add(w2);

        Iterator<Throwable> it = head.iterator();
        List<Throwable> actual = new ArrayList<Throwable>();
        while (it.hasNext()) actual.add(it.next());
        check(actual.equals(expected), "iterator order over warning chain");
    }

    static void testHierarchy() {
        check(java.sql.SQLException.class.isAssignableFrom(java.sql.SQLWarning.class),
                "extends SQLException");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(java.sql.SQLWarning.class).getSerialVersionUID() == 3917336774604784856L,
                "serialVersionUID");
        java.sql.SQLWarning w = new java.sql.SQLWarning("boom", "01000", 5);
        w.setNextWarning(new java.sql.SQLWarning("next"));
        java.sql.SQLWarning back = roundTrip(w);
        check("boom".equals(back.getMessage()), "round-trip message");
        check("01000".equals(back.getSQLState()), "round-trip SQLState");
        check(back.getErrorCode() == 5, "round-trip errorCode");
        check(back.getNextWarning() != null && "next".equals(back.getNextWarning().getMessage()),
                "round-trip warning chain");
    }

    static java.sql.SQLWarning roundTrip(java.sql.SQLWarning w) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(w);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.sql.SQLWarning back = (java.sql.SQLWarning) ois.readObject();
        ois.close();
        return back;
    }
}
