/*
 * @test
 * @summary javax.sound.midi.Soundbank conformance: interface hierarchy and methods.
 * @run main SoundbankTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SoundbankTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Soundbank");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getName"), "method getName");
        check(dm.contains("getVersion"), "method getVersion");
        check(dm.contains("getVendor"), "method getVendor");
        check(dm.contains("getDescription"), "method getDescription");
        check(dm.contains("getResources"), "method getResources");
        check(dm.contains("getInstruments"), "method getInstruments");
        check(dm.contains("getInstrument"), "method getInstrument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
