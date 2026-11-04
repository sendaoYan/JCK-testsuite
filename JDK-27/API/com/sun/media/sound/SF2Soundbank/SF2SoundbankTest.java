/*
 * @test
 * @summary com.sun.media.sound.SF2Soundbank conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.media.sound:+open
 * @run main SF2SoundbankTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SF2SoundbankTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SF2Soundbank", false, SF2SoundbankTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.media.sound.SF2Soundbank".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("javax.sound.midi.Soundbank".equals(ifaces(c)), "interfaces");
        check("addInstrument;addResource;getCreationDate;getDescription;getInstrument;getInstruments;getLayers;getName;getProduct;getResources;getRomName;getRomVersionMajor;getRomVersionMinor;getSamples;getTargetEngine;getTools;getVendor;getVersion;removeInstrument;removeResource;save;setCreationDate;setDescription;setName;setProduct;setRomName;setRomVersionMajor;setRomVersionMinor;setTargetEngine;setTools;setVendor".equals(methodNames(c)), "public/protected methods");
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
