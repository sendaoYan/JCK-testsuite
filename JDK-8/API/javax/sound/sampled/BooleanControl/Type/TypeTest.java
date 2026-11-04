/*
 * @test
 * @summary javax.sound.sampled.BooleanControl.Type conformance: behavioral.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.BooleanControl.Type mute = javax.sound.sampled.BooleanControl.Type.MUTE;
        check(mute != null, "MUTE non-null");
        check(mute instanceof javax.sound.sampled.Control.Type, "instanceof Control.Type");
        check(mute.toString() != null, "toString");
        check(javax.sound.sampled.BooleanControl.Type.APPLY_REVERB != null, "APPLY_REVERB");
        check(mute.equals(mute), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
