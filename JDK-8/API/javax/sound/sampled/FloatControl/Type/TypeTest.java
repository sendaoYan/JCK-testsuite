/*
 * @test
 * @summary javax.sound.sampled.FloatControl.Type conformance: behavioral.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        check(javax.sound.sampled.FloatControl.Type.MASTER_GAIN != null, "MASTER_GAIN");
        check(javax.sound.sampled.FloatControl.Type.VOLUME != null, "VOLUME");
        check(javax.sound.sampled.FloatControl.Type.PAN != null, "PAN");
        check(javax.sound.sampled.FloatControl.Type.BALANCE != null, "BALANCE");
        check(javax.sound.sampled.FloatControl.Type.SAMPLE_RATE != null, "SAMPLE_RATE");
        check(javax.sound.sampled.FloatControl.Type.MASTER_GAIN instanceof javax.sound.sampled.Control.Type, "instanceof");
        check(javax.sound.sampled.FloatControl.Type.MASTER_GAIN.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
