/*
 * @test
 * @summary java.sql.BatchUpdateException API conformance: full contract coverage.
 *          extends SQLException, all 11 constructors with message/SQLState/
 *          vendorCode/cause defaults, defensive copy of int[] and long[] update
 *          counts (source array mutation and returned-array mutation do not leak),
 *          cross-population of getUpdateCounts()/getLargeUpdateCounts(), null
 *          counts -> both null, cause constructors, serialization round-trip +
 *          serialVersionUID.
 * @run main BatchUpdateExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;

public class BatchUpdateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.BatchUpdateException");
        check(c != null, "loads");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "class kind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == java.sql.SQLException.class, "superclass");
        check(c.getInterfaces().length == 0, "no direct interface");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testDefensiveCopy();
        testCrossPopulation();
        testCauseConstructors();
        testHierarchy();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstructors() {
        java.sql.BatchUpdateException e0 = new java.sql.BatchUpdateException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getSQLState() == null, "no-arg SQLState null");
        check(e0.getErrorCode() == 0, "no-arg errorCode 0");
        check(e0.getUpdateCounts() == null, "no-arg updateCounts null");
        check(e0.getLargeUpdateCounts() == null, "no-arg largeUpdateCounts null");

        java.sql.BatchUpdateException e1 = new java.sql.BatchUpdateException(new int[]{1, 2});
        check(e1.getMessage() == null, "(int[]) message null");
        check(e1.getSQLState() == null, "(int[]) SQLState null");
        check(e1.getUpdateCounts().length == 2 && e1.getUpdateCounts()[1] == 2, "(int[]) counts");

        java.sql.BatchUpdateException e2 = new java.sql.BatchUpdateException("boom", new int[]{3});
        check("boom".equals(e2.getMessage()), "(String,int[]) message");
        check(e2.getSQLState() == null, "(String,int[]) SQLState null");
        check(e2.getErrorCode() == 0, "(String,int[]) errorCode 0");

        java.sql.BatchUpdateException e3 = new java.sql.BatchUpdateException("boom", "42S02", new int[]{4});
        check("42S02".equals(e3.getSQLState()), "(String,String,int[]) SQLState");
        check(e3.getErrorCode() == 0, "(String,String,int[]) errorCode 0");

        java.sql.BatchUpdateException e4 = new java.sql.BatchUpdateException("boom", "42S02", 7, new int[]{5});
        check("42S02".equals(e4.getSQLState()), "(String,String,int,int[]) SQLState");
        check(e4.getErrorCode() == 7, "(String,String,int,int[]) errorCode");
        check(e4.getUpdateCounts()[0] == 5, "(String,String,int,int[]) counts");
    }

    static void testDefensiveCopy() {
        int[] src = {10, 20, 30};
        java.sql.BatchUpdateException e = new java.sql.BatchUpdateException(src);
        src[0] = 999;
        int[] got = e.getUpdateCounts();
        check(got[0] == 10, "constructor defensive-copies source array");
        got[0] = 888;
        check(e.getUpdateCounts()[0] == 10, "getUpdateCounts defensive-copies on return");

        long[] lsrc = {100L, 200L};
        java.sql.BatchUpdateException el = new java.sql.BatchUpdateException("r", "S", 1, lsrc, null);
        lsrc[0] = 999L;
        long[] lgot = el.getLargeUpdateCounts();
        check(lgot[0] == 100L, "long constructor defensive-copies source");
        lgot[0] = 888L;
        check(el.getLargeUpdateCounts()[0] == 100L, "getLargeUpdateCounts defensive-copies on return");
    }

    static void testCrossPopulation() {
        // int[] constructor also populates the long[] view.
        java.sql.BatchUpdateException e = new java.sql.BatchUpdateException("r", new int[]{1, 2, 3});
        check(Arrays.equals(e.getLargeUpdateCounts(), new long[]{1L, 2L, 3L}),
                "int[] ctor populates largeUpdateCounts");

        // long[] constructor also populates the int[] view (no overflow check).
        java.sql.BatchUpdateException el =
                new java.sql.BatchUpdateException("r", "S", 1, new long[]{4L, 5L}, null);
        check(Arrays.equals(el.getUpdateCounts(), new int[]{4, 5}),
                "long[] ctor populates updateCounts");

        // null counts -> both null (regardless of which constructor flavor).
        java.sql.BatchUpdateException n = new java.sql.BatchUpdateException("r", (int[]) null);
        check(n.getUpdateCounts() == null && n.getLargeUpdateCounts() == null,
                "null counts -> both null");
    }

    static void testCauseConstructors() {
        RuntimeException cause = new RuntimeException("c");
        java.sql.BatchUpdateException e = new java.sql.BatchUpdateException(cause);
        check(e.getCause() == cause, "(Throwable) cause");
        check(cause.toString().equals(e.getMessage()), "(Throwable) message = cause.toString()");

        java.sql.BatchUpdateException e2 = new java.sql.BatchUpdateException(new int[]{1}, cause);
        check(e2.getCause() == cause, "(int[],Throwable) cause");
        check(e2.getUpdateCounts()[0] == 1, "(int[],Throwable) counts");

        java.sql.BatchUpdateException e3 = new java.sql.BatchUpdateException("r", new int[]{2}, cause);
        check("r".equals(e3.getMessage()), "(String,int[],Throwable) message");
        check(e3.getCause() == cause, "(String,int[],Throwable) cause");

        java.sql.BatchUpdateException e4 = new java.sql.BatchUpdateException("r", "S", new int[]{3}, cause);
        check("S".equals(e4.getSQLState()), "(String,String,int[],Throwable) SQLState");
        check(e4.getCause() == cause, "(String,String,int[],Throwable) cause");

        java.sql.BatchUpdateException e5 =
                new java.sql.BatchUpdateException("r", "S", 9, new int[]{4}, cause);
        check(e5.getErrorCode() == 9, "(String,String,int,int[],Throwable) errorCode");
        check(e5.getCause() == cause, "(String,String,int,int[],Throwable) cause");

        java.sql.BatchUpdateException e6 =
                new java.sql.BatchUpdateException("r", "S", 9, new long[]{5L}, cause);
        check(e6.getErrorCode() == 9, "long[] ctor errorCode");
        check(e6.getCause() == cause, "long[] ctor cause");
        check(e6.getLargeUpdateCounts()[0] == 5L, "long[] ctor counts");
    }

    static void testHierarchy() {
        check(java.sql.SQLException.class.isAssignableFrom(java.sql.BatchUpdateException.class),
                "extends SQLException");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(java.sql.BatchUpdateException.class).getSerialVersionUID() == 5977529877145521757L,
                "serialVersionUID");
        java.sql.BatchUpdateException e = new java.sql.BatchUpdateException("boom", "42S02", 7, new int[]{1, 2, 3});
        java.sql.BatchUpdateException back = roundTrip(e);
        check("boom".equals(back.getMessage()), "round-trip message");
        check("42S02".equals(back.getSQLState()), "round-trip SQLState");
        check(back.getErrorCode() == 7, "round-trip errorCode");
        check(Arrays.equals(back.getUpdateCounts(), new int[]{1, 2, 3}), "round-trip counts");
    }

    static java.sql.BatchUpdateException roundTrip(java.sql.BatchUpdateException e) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.sql.BatchUpdateException back = (java.sql.BatchUpdateException) ois.readObject();
        ois.close();
        return back;
    }
}
