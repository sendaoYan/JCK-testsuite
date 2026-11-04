/*
 * @test
 * @summary java.rmi.server.ObjID conformance: constants, uniqueness, write/read.
 * @run main ObjIDTest
 */

import java.rmi.server.*;
public class ObjIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(ObjID.REGISTRY_ID == 0, "REGISTRY_ID");
        check(ObjID.DGC_ID == 2, "DGC_ID");
        ObjID o1 = new ObjID();
        ObjID o2 = new ObjID();
        check(!o1.equals(o2), "unique");
        check(new ObjID(7).equals(new ObjID(7)), "explicit num equals");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        o1.write(oos);
        oos.flush();
        ObjID o3 = ObjID.read(new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())));
        check(o1.equals(o3), "write/read roundtrip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
