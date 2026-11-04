/*
 * @test
 * @summary javax.sound.sampled.CompoundControl.Type conformance: behavioral.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        CompType c = new CompType("Test");
        check("Test".equals(c.toString()), "toString");
        check(c instanceof javax.sound.sampled.Control.Type, "instanceof");
        check(c.equals(c), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class CompType extends javax.sound.sampled.CompoundControl.Type {
    CompType(String n) { super(n); }
}
