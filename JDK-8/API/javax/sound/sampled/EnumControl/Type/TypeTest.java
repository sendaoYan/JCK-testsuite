/*
 * @test
 * @summary javax.sound.sampled.EnumControl.Type conformance: behavioral.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.EnumControl.Type reverb = javax.sound.sampled.EnumControl.Type.REVERB;
        check(reverb != null, "REVERB");
        check(reverb instanceof javax.sound.sampled.Control.Type, "instanceof");
        check(reverb.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
