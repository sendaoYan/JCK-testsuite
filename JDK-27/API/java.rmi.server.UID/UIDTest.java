/*
 * @test
 * @summary java.rmi.server.UID conformance: uniqueness and serialization round trip.
 * @run main UIDTest
 */

import java.rmi.server.UID;
import java.io.*;

public class UIDTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        UID u = new UID();
        UID u2 = new UID();
        check(u.equals(u), "equals self");
        check(!u.equals(u2), "unique");
        check(u.hashCode() == u.hashCode(), "hashcode stable");
        String s = u.toString();
        check(s != null && s.length() > 0, "toString");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        u.write(dout); dout.flush();
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
        UID u3 = UID.read(din);
        check(u.equals(u3), "read round trip");
        check(u3.toString().equals(u.toString()), "same toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
