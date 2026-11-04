/*
 * @test
 * @summary java.io.ObjectInputStream.GetField conformance: defaulted() and get() overloads.
 * @run main GetFieldTest
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class GetFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGetField() throws Exception {
        ObjectInputStream.GetField gf = new ObjectInputStream.GetField() {
            public ObjectStreamClass getObjectStreamClass() {
                return null;
            }
            public boolean defaulted(String name) {
                return name.equals("def");
            }
            public boolean get(String name, boolean val) {
                return val;
            }
            public byte get(String name, byte val) {
                return val;
            }
            public char get(String name, char val) {
                return val;
            }
            public short get(String name, short val) {
                return val;
            }
            public int get(String name, int val) {
                return val;
            }
            public long get(String name, long val) {
                return val;
            }
            public float get(String name, float val) {
                return val;
            }
            public double get(String name, double val) {
                return val;
            }
            public Object get(String name, Object val) {
                return val;
            }
        };
        check(gf.defaulted("def"), "defaulted true");
        check(!gf.defaulted("other"), "defaulted false");
        check(gf.get("a", true), "get boolean");
        check(gf.get("a", (byte) 7) == 7, "get byte");
        check(gf.get("a", 'c') == 'c', "get char");
        check(gf.get("a", (short) 9) == 9, "get short");
        check(gf.get("a", 11) == 11, "get int");
        check(gf.get("a", 13L) == 13L, "get long");
        check(gf.get("a", 1.5f) == 1.5f, "get float");
        check(gf.get("a", 2.5d) == 2.5d, "get double");
        check("obj".equals(gf.get("a", "obj")), "get Object");
    }

    public static void main(String[] args) throws Exception {
        testGetField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
