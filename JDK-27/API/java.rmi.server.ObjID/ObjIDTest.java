/*
 * @test
 * @summary java.rmi.server.ObjID conformance: equality and serialization round trip.
 * @run main ObjIDTest
 */

import java.rmi.server.ObjID;
import java.io.*;

public class ObjIDTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        ObjID id = new ObjID(42);
        check(id.hashCode() == id.hashCode(), "hashcode");
        ObjID id2 = new ObjID(42);
        check(id.equals(id2), "equals same num");
        check(!id.equals(new ObjID(43)), "not equals diff");
        check(id.toString() != null, "toString");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        id.write(oout); oout.flush();
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ObjID id3 = ObjID.read(oin);
        check(id.equals(id3), "read round trip");
        check(ObjID.REGISTRY_ID != ObjID.ACTIVATOR_ID && ObjID.ACTIVATOR_ID != ObjID.DGC_ID, "distinct constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
