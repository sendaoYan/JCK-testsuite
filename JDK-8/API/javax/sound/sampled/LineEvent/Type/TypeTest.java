/*
 * @test
 * @summary javax.sound.sampled.LineEvent.Type conformance: behavioral.
 * @run main TypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        check(javax.sound.sampled.LineEvent.Type.OPEN != null, "OPEN");
        check(javax.sound.sampled.LineEvent.Type.CLOSE != null, "CLOSE");
        check(javax.sound.sampled.LineEvent.Type.START != null, "START");
        check(javax.sound.sampled.LineEvent.Type.STOP != null, "STOP");
        check(javax.sound.sampled.LineEvent.Type.OPEN.toString() != null, "toString");
        check(javax.sound.sampled.LineEvent.Type.OPEN.equals(javax.sound.sampled.LineEvent.Type.OPEN), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
