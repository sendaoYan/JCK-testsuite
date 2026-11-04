/*
 * @test
 * @summary javax.sound.sampled.Mixer.Info conformance: behavioral.
 * @run main InfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        MixInfo i1 = new MixInfo("n", "v", "d", "1.0");
        check("n".equals(i1.getName()), "getName");
        check("v".equals(i1.getVendor()), "getVendor");
        check("d".equals(i1.getDescription()), "getDescription");
        check("1.0".equals(i1.getVersion()), "getVersion");
        check(i1.toString() != null, "toString");
        MixInfo i2 = new MixInfo("n", "v", "d", "1.0");
        check(i1.equals(i1), "equals self");
        check(!i1.equals(i2), "not equals distinct");
        check(i1.hashCode() == i1.hashCode(), "hashCode stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class MixInfo extends javax.sound.sampled.Mixer.Info {
    MixInfo(String a, String b, String c, String d) { super(a, b, c, d); }
}
