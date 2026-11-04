/*
 * @test
 * @summary java.rmi.server.UID conformance: full contract.
 *          final + Serializable, no-arg uniqueness, well-known constructor
 *          (explicit count), equals/hashCode (self, same count, different
 *          count, null, wrong type), toString hex format for well-known UIDs,
 *          and DataOutput/DataInput write/read round-trip.
 * @run main UIDTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.rmi.server.UID;

public class UIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testStructure() {
        Class<?> c = UID.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testUniqueness() {
        UID u1 = new UID();
        UID u2 = new UID();
        check(!u1.equals(u2), "no-arg UIDs unique");
        check(u1.equals(u1), "self equals");
        check(u1.hashCode() == u1.hashCode(), "hashCode stable");
    }

    static void testWellKnown() {
        UID a = new UID((short) 5);
        UID b = new UID((short) 5);
        check(a.equals(b) && b.equals(a), "same count equal");
        check(a.hashCode() == b.hashCode(), "same count hash equal");
        check(!a.equals(new UID((short) 6)), "different count not equal");
        check(!a.equals(null), "not equals null");
        check(!a.equals("5"), "not equals non-UID");
        check("0:0:5".equals(a.toString()), "well-known toString hex format");
        check("0:0:7".equals(new UID((short) 7).toString()), "well-known toString hex format 7");
    }

    static void testRoundTrip() throws Exception {
        UID u1 = new UID();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        u1.write(dos);
        dos.flush();
        UID u2 = UID.read(new DataInputStream(new ByteArrayInputStream(bos.toByteArray())));
        check(u1.equals(u2), "write/read round-trip");

        UID wk = new UID((short) 9);
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        DataOutputStream d2 = new DataOutputStream(b2);
        wk.write(d2);
        d2.flush();
        UID wk2 = UID.read(new DataInputStream(new ByteArrayInputStream(b2.toByteArray())));
        check(wk.equals(wk2), "well-known write/read round-trip");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testUniqueness();
        testWellKnown();
        testRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
