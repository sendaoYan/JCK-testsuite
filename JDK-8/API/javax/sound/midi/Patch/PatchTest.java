/*
 * @test
 * @summary javax.sound.midi.Patch conformance: behavioral.
 * @run main PatchTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class PatchTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.Patch p = new javax.sound.midi.Patch(1, 2);
        check(p.getBank() == 1, "bank");
        check(p.getProgram() == 2, "program");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
