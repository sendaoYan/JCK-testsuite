/*
 * @test
 * @summary javax.sound.sampled.ReverbType conformance: behavioral.
 * @run main ReverbTypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ReverbTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        ReverbImpl r = new ReverbImpl("Hall", 10, 0.5f, 20, 0.3f, 1000);
        check("Hall".equals(r.getName()), "name");
        check(r.getEarlyReflectionDelay() == 10, "earlyReflectionDelay");
        check(r.getEarlyReflectionIntensity() == 0.5f, "earlyReflectionIntensity");
        check(r.getLateReflectionDelay() == 20, "lateReflectionDelay");
        check(r.getLateReflectionIntensity() == 0.3f, "lateReflectionIntensity");
        check(r.getDecayTime() == 1000, "decayTime");
        check(r.toString() != null, "toString");
        check(r.equals(r), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class ReverbImpl extends javax.sound.sampled.ReverbType {
    ReverbImpl(String n, int a, float b, int c, float d, int e) { super(n, a, b, c, d, e); }
}
