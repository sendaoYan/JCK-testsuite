/*
 * @test
 * @summary com.sun.media.sound.SF2Region conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.media.sound:+open
 * @run main SF2RegionTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SF2RegionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SF2Region", false, SF2RegionTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.media.sound.SF2Region".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("contains;getBytes;getDefaultValue;getGenerators;getInteger;getModulators;getShort;putBytes;putInteger;putShort".equals(methodNames(c)), "public/protected methods");
        check("GENERATOR_ATTACKMODENV;GENERATOR_ATTACKVOLENV;GENERATOR_CHORUSEFFECTSSEND;GENERATOR_COARSETUNE;GENERATOR_DECAYMODENV;GENERATOR_DECAYVOLENV;GENERATOR_DELAYMODENV;GENERATOR_DELAYMODLFO;GENERATOR_DELAYVIBLFO;GENERATOR_DELAYVOLENV;GENERATOR_ENDADDRSCOARSEOFFSET;GENERATOR_ENDADDRSOFFSET;GENERATOR_ENDLOOPADDRSCOARSEOFFSET;GENERATOR_ENDLOOPADDRSOFFSET;GENERATOR_ENDOPR;GENERATOR_EXCLUSIVECLASS;GENERATOR_FINETUNE;GENERATOR_FREQMODLFO;GENERATOR_FREQVIBLFO;GENERATOR_HOLDMODENV;GENERATOR_HOLDVOLENV;GENERATOR_INITIALATTENUATION;GENERATOR_INITIALFILTERFC;GENERATOR_INITIALFILTERQ;GENERATOR_INSTRUMENT;GENERATOR_KEYNUM;GENERATOR_KEYNUMTOMODENVDECAY;GENERATOR_KEYNUMTOMODENVHOLD;GENERATOR_KEYNUMTOVOLENVDECAY;GENERATOR_KEYNUMTOVOLENVHOLD;GENERATOR_KEYRANGE;GENERATOR_MODENVTOFILTERFC;GENERATOR_MODENVTOPITCH;GENERATOR_MODLFOTOFILTERFC;GENERATOR_MODLFOTOPITCH;GENERATOR_MODLFOTOVOLUME;GENERATOR_OVERRIDINGROOTKEY;GENERATOR_PAN;GENERATOR_RELEASEMODENV;GENERATOR_RELEASEVOLENV;GENERATOR_RESERVED1;GENERATOR_RESERVED2;GENERATOR_RESERVED3;GENERATOR_REVERBEFFECTSSEND;GENERATOR_SAMPLEID;GENERATOR_SAMPLEMODES;GENERATOR_SCALETUNING;GENERATOR_STARTADDRSCOARSEOFFSET;GENERATOR_STARTADDRSOFFSET;GENERATOR_STARTLOOPADDRSCOARSEOFFSET;GENERATOR_STARTLOOPADDRSOFFSET;GENERATOR_SUSTAINMODENV;GENERATOR_SUSTAINVOLENV;GENERATOR_UNUSED1;GENERATOR_UNUSED2;GENERATOR_UNUSED3;GENERATOR_UNUSED4;GENERATOR_UNUSED5;GENERATOR_VELOCITY;GENERATOR_VELRANGE;GENERATOR_VIBLFOTOPITCH;generators;modulators".equals(fieldNames(c)), "public/protected fields");
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
