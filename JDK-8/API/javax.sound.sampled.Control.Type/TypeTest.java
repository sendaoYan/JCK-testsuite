/*
 * @test
 * @summary javax.sound.sampled.Control.Type conformance: behavioral.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        CtrlType c = new CtrlType("Test");
        check("Test".equals(c.toString()), "toString");
        check(c.equals(c), "equals");
        check(c.hashCode() == c.hashCode(), "hashCode stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class CtrlType extends javax.sound.sampled.Control.Type {
    CtrlType(String n) { super(n); }
}
