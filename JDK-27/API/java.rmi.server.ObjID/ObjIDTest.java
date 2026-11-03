/*
 * @test
 * @summary java.rmi.server.ObjID conformance: white-box structure + black-box behavior.
 * @run main ObjIDTest
 */

import java.io.*;
import java.lang.reflect.Modifier;
import java.rmi.server.ObjID;

public class ObjIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = ObjID.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()), "public final");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(ObjID.REGISTRY_ID == 0, "REGISTRY_ID == 0");
        check(ObjID.ACTIVATOR_ID == 1, "ACTIVATOR_ID == 1");
        check(ObjID.DGC_ID == 2, "DGC_ID == 2");
    }

    static void testBlackBox() throws Exception {
        // unique no-arg instances never equal
        ObjID a = new ObjID();
        ObjID b = new ObjID();
        check(a.equals(a), "equals self");
        check(!a.equals(b), "unique no-arg distinct");
        check(!a.equals(null), "not equals null");
        check(!a.equals("string"), "not equals other type");

        // well-known ObjID(int) has a fixed value and hash
        ObjID w1 = new ObjID(42);
        ObjID w2 = new ObjID(42);
        check(w1.equals(w2), "well-known equals same num");
        check(w1.hashCode() == 42, "well-known hashCode == objNum");
        check(!w1.equals(new ObjID(43)), "well-known not equals diff num");
        check(w1.toString().equals("[0:0:0, 42]"), "well-known toString");

        // well-known vs no-arg distinct
        check(!w1.equals(a), "well-known vs no-arg distinct");

        // marshal round trip (write/read ObjectOutput/ObjectInput)
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        a.write(oout); oout.flush();
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ObjID a2 = ObjID.read(oin);
        check(a.equals(a2), "marshal round trip equals");
        check(a.toString().equals(a2.toString()), "marshal round trip toString");

        // Java serialization round trip
        ByteArrayOutputStream sbout = new ByteArrayOutputStream();
        ObjectOutputStream soout = new ObjectOutputStream(sbout);
        soout.writeObject(a); soout.flush();
        ObjectInputStream soin = new ObjectInputStream(new ByteArrayInputStream(sbout.toByteArray()));
        ObjID a3 = (ObjID) soin.readObject();
        check(a.equals(a3), "java serialization round trip equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
