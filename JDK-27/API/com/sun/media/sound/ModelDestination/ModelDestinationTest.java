/*
 * @test
 * @summary com.sun.media.sound.ModelDestination conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.media.sound:+open
 * @run main ModelDestinationTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ModelDestinationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelDestination", false, ModelDestinationTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.media.sound.ModelDestination".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("getIdentifier;getTransform;setIdentifier;setTransform".equals(methodNames(c)), "public/protected methods");
        check("DESTINATION_CHORUS;DESTINATION_EG1_ATTACK;DESTINATION_EG1_DECAY;DESTINATION_EG1_DELAY;DESTINATION_EG1_HOLD;DESTINATION_EG1_RELEASE;DESTINATION_EG1_SHUTDOWN;DESTINATION_EG1_SUSTAIN;DESTINATION_EG2_ATTACK;DESTINATION_EG2_DECAY;DESTINATION_EG2_DELAY;DESTINATION_EG2_HOLD;DESTINATION_EG2_RELEASE;DESTINATION_EG2_SHUTDOWN;DESTINATION_EG2_SUSTAIN;DESTINATION_FILTER_FREQ;DESTINATION_FILTER_Q;DESTINATION_GAIN;DESTINATION_KEYNUMBER;DESTINATION_LFO1_DELAY;DESTINATION_LFO1_FREQ;DESTINATION_LFO2_DELAY;DESTINATION_LFO2_FREQ;DESTINATION_NONE;DESTINATION_PAN;DESTINATION_PITCH;DESTINATION_REVERB;DESTINATION_VELOCITY".equals(fieldNames(c)), "public/protected fields");
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
