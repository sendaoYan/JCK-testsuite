/*
 * @test
 * @summary java.sql.SQLException API conformance: full contract coverage.
 *          all 8 constructors and their message/SQLState/vendorCode/cause
 *          defaults, getters, setNextException appends-to-end chain semantics,
 *          Iterable<Throwable> iterator ordering (self, cause chain, next
 *          exception, next cause chain, ...) + remove() UOE + next() NSEE,
 *          serialization round-trip + serialVersionUID, and hierarchy
 *          (extends Exception, implements Iterable<Throwable>).
 * @run main SQLExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SQLExceptionTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.SQLException");
        check(c != null, "loads");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "class kind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == java.lang.Exception.class, "superclass");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0] == java.lang.Iterable.class, "implements Iterable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testGettersDefaults();
        testCauseConstructors();
        testSetNextExceptionChain();
        testIterator();
        testHierarchy();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstructors() {
        java.sql.SQLException e0 = new java.sql.SQLException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getSQLState() == null, "no-arg SQLState null");
        check(e0.getErrorCode() == 0, "no-arg errorCode 0");

        java.sql.SQLException e1 = new java.sql.SQLException("boom");
        check("boom".equals(e1.getMessage()), "(String) message");
        check(e1.getSQLState() == null, "(String) SQLState null");
        check(e1.getErrorCode() == 0, "(String) errorCode 0");

        java.sql.SQLException e2 = new java.sql.SQLException("boom", "42S02");
        check("boom".equals(e2.getMessage()), "(String,String) message");
        check("42S02".equals(e2.getSQLState()), "(String,String) SQLState");
        check(e2.getErrorCode() == 0, "(String,String) errorCode 0");

        java.sql.SQLException e3 = new java.sql.SQLException("boom", "42S02", 7);
        check("boom".equals(e3.getMessage()), "(String,String,int) message");
        check("42S02".equals(e3.getSQLState()), "(String,String,int) SQLState");
        check(e3.getErrorCode() == 7, "(String,String,int) errorCode");
        check(e3.getNextException() == null, "no next exception");
    }

    static void testGettersDefaults() {
        java.sql.SQLException e = new java.sql.SQLException("x");
        check(e.getCause() == null, "getCause null by default");
        check(e.getNextException() == null, "getNextException null by default");
    }

    static void testCauseConstructors() {
        RuntimeException cause = new RuntimeException("boom");
        java.sql.SQLException e = new java.sql.SQLException(cause);
        check(e.getCause() == cause, "(Throwable) cause set");
        check(cause.toString().equals(e.getMessage()), "(Throwable) message = cause.toString()");
        check(e.getSQLState() == null, "(Throwable) SQLState null");
        check(e.getErrorCode() == 0, "(Throwable) errorCode 0");

        java.sql.SQLException e2 = new java.sql.SQLException("reason", cause);
        check("reason".equals(e2.getMessage()), "(String,Throwable) message");
        check(e2.getCause() == cause, "(String,Throwable) cause");
        check(e2.getSQLState() == null, "(String,Throwable) SQLState null");
        check(e2.getErrorCode() == 0, "(String,Throwable) errorCode 0");

        java.sql.SQLException e3 = new java.sql.SQLException("reason", "42S02", cause);
        check("reason".equals(e3.getMessage()), "(String,String,Throwable) message");
        check("42S02".equals(e3.getSQLState()), "(String,String,Throwable) SQLState");
        check(e3.getErrorCode() == 0, "(String,String,Throwable) errorCode 0");
        check(e3.getCause() == cause, "(String,String,Throwable) cause");

        java.sql.SQLException e4 = new java.sql.SQLException("reason", "42S02", 9, cause);
        check("reason".equals(e4.getMessage()), "(String,String,int,Throwable) message");
        check("42S02".equals(e4.getSQLState()), "(String,String,int,Throwable) SQLState");
        check(e4.getErrorCode() == 9, "(String,String,int,Throwable) errorCode");
        check(e4.getCause() == cause, "(String,String,int,Throwable) cause");
    }

    static void testSetNextExceptionChain() {
        java.sql.SQLException head = new java.sql.SQLException("head");
        java.sql.SQLException n1 = new java.sql.SQLException("n1");
        java.sql.SQLException n2 = new java.sql.SQLException("n2");
        head.setNextException(n1);
        head.setNextException(n2);
        check(head.getNextException() == n1, "first setNextException is head.next");
        check(n1.getNextException() == n2, "second setNextException appends to end");
        check(n2.getNextException() == null, "tail has no next");
    }

    static void testIterator() {
        // Build a chain: root -> cause1 -> cause2, next=n1 -> n1cause, next=n2.
        java.sql.SQLException root = new java.sql.SQLException("root", "SQL1", 100);
        java.sql.SQLException cause1 = new java.sql.SQLException("c1", "SQLC1", 101);
        java.sql.SQLException cause2 = new java.sql.SQLException("c2", "SQLC2", 102);
        cause1.initCause(cause2);
        root.initCause(cause1);
        java.sql.SQLException n1 = new java.sql.SQLException("n1", "SQLN1", 200);
        java.sql.SQLException n2 = new java.sql.SQLException("n2", "SQLN2", 201);
        java.sql.SQLException n1cause = new java.sql.SQLException("n1c");
        n1.initCause(n1cause);
        root.setNextException(n1);
        n1.setNextException(n2);

        List<Throwable> expected = new ArrayList<Throwable>();
        expected.add(root);
        expected.add(cause1);
        expected.add(cause2);
        expected.add(n1);
        expected.add(n1cause);
        expected.add(n2);

        Iterator<Throwable> it = root.iterator();
        List<Throwable> actual = new ArrayList<Throwable>();
        while (it.hasNext()) {
            actual.add(it.next());
        }
        check(actual.equals(expected), "iterator order self/cause-chain/next/cause-chain");

        // remove() unsupported.
        final Iterator<Throwable> it2 = root.iterator();
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { it2.remove(); }
        }, "iterator.remove()");

        // Bare exception yields exactly one element then NSEE.
        java.sql.SQLException bare = new java.sql.SQLException("bare");
        final Iterator<Throwable> it3 = bare.iterator();
        check(it3.hasNext(), "bare iterator hasNext initially");
        check(it3.next() == bare, "bare iterator first == self");
        check(!it3.hasNext(), "bare iterator exhausted");
        checkThrows(java.util.NoSuchElementException.class, new ThrowingRunnable() {
            public void run() { it3.next(); }
        }, "bare iterator next() after end");
    }

    static void testHierarchy() {
        check(java.lang.Exception.class.isAssignableFrom(java.sql.SQLException.class),
                "extends Exception");
        check(Iterable.class.isAssignableFrom(java.sql.SQLException.class),
                "implements Iterable<Throwable>");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(java.sql.SQLException.class).getSerialVersionUID() == 2135244094396331484L,
                "serialVersionUID");
        java.sql.SQLException e = new java.sql.SQLException("boom", "42S02", 7);
        e.setNextException(new java.sql.SQLException("next"));
        java.sql.SQLException back = roundTrip(e);
        check("boom".equals(back.getMessage()), "round-trip message");
        check("42S02".equals(back.getSQLState()), "round-trip SQLState");
        check(back.getErrorCode() == 7, "round-trip errorCode");
        check(back.getNextException() != null
                && "next".equals(back.getNextException().getMessage()),
                "round-trip next exception chain");
    }

    static java.sql.SQLException roundTrip(java.sql.SQLException e) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.sql.SQLException back = (java.sql.SQLException) ois.readObject();
        ois.close();
        return back;
    }
}
