/*
 * @test
 * @summary java.rmi.server.ObjID conformance: full contract.
 *          final + Serializable, REGISTRY_ID/ACTIVATOR_ID/DGC_ID constants,
 *          no-arg uniqueness, well-known constructor (explicit objNum),
 *          hashCode == objNum, equals/hashCode (self, same num, different
 *          num, null, wrong type), toString format, and ObjectOutput/
 *          ObjectInput write/read round-trip.
 * @run main ObjIDTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.server.ObjID;

public class ObjIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testConstantsAndStructure() {
        Class<?> c = ObjID.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(ObjID.REGISTRY_ID == 0, "REGISTRY_ID == 0");
        check(ObjID.ACTIVATOR_ID == 1, "ACTIVATOR_ID == 1");
        check(ObjID.DGC_ID == 2, "DGC_ID == 2");
    }

    static void testUniqueness() {
        ObjID o1 = new ObjID();
        ObjID o2 = new ObjID();
        check(!o1.equals(o2), "no-arg ObjIDs unique");
        check(o1.equals(o1), "self equals");
    }

    static void testWellKnown() {
        ObjID a = new ObjID(7);
        ObjID b = new ObjID(7);
        check(a.equals(b) && b.equals(a), "same num equal");
        check(a.hashCode() == b.hashCode(), "same num hash equal");
        check(a.hashCode() == 7, "hashCode == objNum");
        check(!a.equals(new ObjID(8)), "different num not equal");
        check(!a.equals(null), "not equals null");
        check(!a.equals("7"), "not equals non-ObjID");
        check("[0:0:0, 7]".equals(a.toString()), "well-known toString format");
    }

    static void testRoundTrip() throws Exception {
        ObjID o1 = new ObjID();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        o1.write(oos);
        oos.flush();
        ObjID o2 = ObjID.read(new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())));
        check(o1.equals(o2), "write/read round-trip");

        ObjID wk = new ObjID(9);
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ObjectOutputStream o2s = new ObjectOutputStream(b2);
        wk.write(o2s);
        o2s.flush();
        ObjID wk2 = ObjID.read(new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray())));
        check(wk.equals(wk2), "well-known write/read round-trip");
    }

    public static void main(String[] args) throws Exception {
        testConstantsAndStructure();
        testUniqueness();
        testWellKnown();
        testRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
