/*
 * @test
 * @summary java.sql.DataTruncation API conformance: full contract coverage.
 *          extends SQLWarning, getSQLState "01004" (read) / "22001" (write),
 *          message "Data truncation", vendor code 0, index/parameter/read/
 *          dataSize/transferSize fields (incl. -1 size sentinels), cause
 *          constructor, serialization round-trip + serialVersionUID.
 * @run main DataTruncationTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public class DataTruncationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testReadTruncation();
        testWriteTruncation();
        testCauseConstructor();
        testHierarchy();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testReadTruncation() {
        java.sql.DataTruncation e = new java.sql.DataTruncation(3, true, true, 10, 4);
        check("01004".equals(e.getSQLState()), "read SQLState 01004");
        check("Data truncation".equals(e.getMessage()), "message Data truncation");
        check(e.getErrorCode() == 0, "vendor code 0");
        check(e.getIndex() == 3, "getIndex");
        check(e.getParameter(), "getParameter");
        check(e.getRead(), "getRead");
        check(e.getDataSize() == 10, "getDataSize");
        check(e.getTransferSize() == 4, "getTransferSize");
    }

    static void testWriteTruncation() {
        java.sql.DataTruncation e = new java.sql.DataTruncation(-1, false, false, -1, -1);
        check("22001".equals(e.getSQLState()), "write SQLState 22001");
        check(!e.getParameter(), "getParameter false");
        check(!e.getRead(), "getRead false");
        check(e.getIndex() == -1, "getIndex -1 sentinel");
        check(e.getDataSize() == -1, "getDataSize -1 unknown");
        check(e.getTransferSize() == -1, "getTransferSize -1 unknown");
    }

    static void testCauseConstructor() {
        Throwable cause = new Throwable("truncated");
        java.sql.DataTruncation e = new java.sql.DataTruncation(5, true, false, 8, 2, cause);
        check(e.getCause() == cause, "cause set");
        check("22001".equals(e.getSQLState()), "write SQLState via cause ctor");
        check(e.getIndex() == 5, "getIndex via cause ctor");
        check(e.getParameter(), "getParameter via cause ctor");
        check(!e.getRead(), "getRead via cause ctor");
        check(e.getDataSize() == 8 && e.getTransferSize() == 2, "sizes via cause ctor");
    }

    static void testHierarchy() {
        check(java.sql.SQLWarning.class.isAssignableFrom(java.sql.DataTruncation.class),
                "extends SQLWarning");
        check(java.sql.SQLException.class.isAssignableFrom(java.sql.DataTruncation.class),
                "extends SQLException (transitively)");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(java.sql.DataTruncation.class).getSerialVersionUID() == 6464298989504059473L,
                "serialVersionUID");
        java.sql.DataTruncation e = new java.sql.DataTruncation(3, true, true, 10, 4);
        java.sql.DataTruncation back = roundTrip(e);
        check("01004".equals(back.getSQLState()), "round-trip SQLState");
        check(back.getIndex() == 3, "round-trip index");
        check(back.getParameter() && back.getRead(), "round-trip flags");
        check(back.getDataSize() == 10 && back.getTransferSize() == 4, "round-trip sizes");
        check("Data truncation".equals(back.getMessage()), "round-trip message");
    }

    static java.sql.DataTruncation roundTrip(java.sql.DataTruncation e) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.sql.DataTruncation back = (java.sql.DataTruncation) ois.readObject();
        ois.close();
        return back;
    }
}
