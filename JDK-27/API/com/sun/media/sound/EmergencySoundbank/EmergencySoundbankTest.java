/*
 * @test
 * @summary com.sun.media.sound.EmergencySoundbank conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.media.sound:+open
 * @run main EmergencySoundbankTest
 */

import java.util.*;
import java.lang.reflect.*;

public class EmergencySoundbankTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.EmergencySoundbank", false, EmergencySoundbankTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.media.sound.EmergencySoundbank".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("complexGaussianDist;createSoundbank;fadeUp;fft;ifft;imgPart;loopExtend;newInstrument;newLayer;newSimpleDrumSample;newSimpleFFTSample;newSimpleFFTSample_dist;new_bass1;new_bass2;new_bass_drum;new_bassoon;new_bell;new_brass_section;new_ch_organ;new_choir;new_clarinet;new_closed_hihat;new_crash_cymbal;new_epiano1;new_epiano2;new_flute;new_gpiano;new_gpiano2;new_guitar1;new_guitar_dist;new_guitar_pick;new_horn;new_melodic_toms;new_oboe;new_open_hihat;new_orchhit;new_organ;new_piano1;new_piano_hammer;new_reverse_cymbal;new_sax;new_side_stick;new_snare_drum;new_solostring;new_string2;new_synthbass;new_timpani;new_tom;new_trombone;new_trumpet;normalize;randomPhase;realPart;toBytes;toFloat".equals(methodNames(c)), "public/protected methods");
        check("".equals(fieldNames(c)), "public/protected fields");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String ifaces(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return String.join(";", s);
    }

    static String methodNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return String.join(";", s);
    }

    static String fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            int fm = f.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(f.getName());
        }
        return String.join(";", s);
    }

    static String enumConstants(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) s.add(f.getName());
        return String.join(";", s);
    }
}
