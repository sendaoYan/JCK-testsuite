/*
 * @test
 * @summary java.rmi.server.UID conformance: uniqueness and write/read roundtrip.
 * @run main UIDTest
 */

import java.rmi.server.*;
public class UIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        UID u1 = new UID();
        UID u2 = new UID();
        check(!u1.equals(u2), "unique");
        check(u1.equals(u1), "self equals");
        check(new UID((short) 5).equals(new UID((short) 5)), "explicit count equals");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(bos);
        u1.write(dos);
        dos.flush();
        UID u3 = UID.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())));
        check(u1.equals(u3), "write/read roundtrip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
