/*
 * @test
 * @summary javax.sql.rowset.spi.TransactionalWriter conformance: white-box structure + black-box behavior.
 * @run main TransactionalWriterTest
 */

import java.sql.Savepoint;
import javax.sql.RowSetInternal;
import javax.sql.RowSetWriter;
import javax.sql.rowset.spi.TransactionalWriter;

public class TransactionalWriterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.spi.TransactionalWriter");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSetWriter"), "superinterface 0");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] commits = new int[1];
        final int[] rollbacks = new int[1];
        final boolean[] wrote = new boolean[1];
        final Savepoint[] saved = new Savepoint[1];
        TransactionalWriter tw = new TransactionalWriter() {
            public boolean writeData(RowSetInternal caller) { wrote[0] = true; return true; }
            public void commit() { commits[0]++; }
            public void rollback() { rollbacks[0]++; }
            public void rollback(Savepoint s) { rollbacks[0]++; saved[0] = s; }
        };

        check(tw instanceof RowSetWriter, "TransactionalWriter is a RowSetWriter");

        // writeData from the inherited RowSetWriter contract.
        check(tw.writeData(null) == true, "writeData returns true");
        check(wrote[0], "writeData was invoked");

        // commit/rollback lifecycle.
        tw.commit();
        check(commits[0] == 1, "commit incremented");
        tw.rollback();
        check(rollbacks[0] == 1, "rollback incremented");

        // rollback(Savepoint) carries the supplied savepoint through.
        Savepoint sp = new Savepoint() {
            public int getSavepointId() { return 7; }
            public String getSavepointName() { return "sp1"; }
        };
        check(sp.getSavepointId() == 7, "savepoint id");
        check("sp1".equals(sp.getSavepointName()), "savepoint name");
        tw.rollback(sp);
        check(rollbacks[0] == 2, "rollback(Savepoint) incremented");
        check(saved[0] == sp, "rollback(Savepoint) received the same savepoint");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
