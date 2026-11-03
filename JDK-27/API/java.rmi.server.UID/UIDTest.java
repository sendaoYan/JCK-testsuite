/*
 * @test
 * @summary java.rmi.server.UID conformance: white-box structure + black-box behavior.
 * @run main UIDTest
 */

import java.io.*;
import java.lang.reflect.Modifier;
import java.rmi.server.UID;

public class UIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = UID.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()), "public final");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testBlackBox() throws Exception {
        // unique no-arg instances
        UID a = new UID();
        UID b = new UID();
        check(a.equals(a), "equals self");
        check(!a.equals(b), "unique no-arg distinct");
        check(!a.equals(null), "not equals null");
        check(!a.equals("string"), "not equals other type");
        check(a.hashCode() == a.hashCode(), "hashCode stable");

        // well-known UID(short) fixed value
        UID w = new UID((short) 5);
        check(w.equals(new UID((short) 5)), "well-known equals same num");
        check(!w.equals(new UID((short) 6)), "well-known not equals diff num");
        check(w.toString().equals("0:0:5"), "well-known toString");
        check(w.hashCode() == 5, "well-known hashCode == count");

        // marshal round trip (DataOutput/DataInput)
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        a.write(dout); dout.flush();
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
        UID a2 = UID.read(din);
        check(a.equals(a2), "marshal round trip equals");
        check(a.toString().equals(a2.toString()), "marshal round trip toString");

        // well-known marshal round trip
        ByteArrayOutputStream wbout = new ByteArrayOutputStream();
        DataOutputStream wdout = new DataOutputStream(wbout);
        w.write(wdout); wdout.flush();
        UID w2 = UID.read(new DataInputStream(new ByteArrayInputStream(wbout.toByteArray())));
        check(w.equals(w2), "well-known marshal round trip equals");

        // Java serialization round trip
        ByteArrayOutputStream sbout = new ByteArrayOutputStream();
        ObjectOutputStream soout = new ObjectOutputStream(sbout);
        soout.writeObject(a); soout.flush();
        ObjectInputStream soin = new ObjectInputStream(new ByteArrayInputStream(sbout.toByteArray()));
        UID a3 = (UID) soin.readObject();
        check(a.equals(a3), "java serialization round trip equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
