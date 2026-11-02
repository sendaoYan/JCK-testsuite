/*
 * @test
 * @summary com.sun.media.sound.SF2Modulator conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.media.sound:+open
 * @run main SF2ModulatorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SF2ModulatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SF2Modulator", false, SF2ModulatorTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.media.sound.SF2Modulator".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("getAmount;getAmountSourceOperator;getDestinationOperator;getSourceOperator;getTransportOperator;setAmount;setAmountSourceOperator;setDestinationOperator;setSourceOperator;setTransportOperator".equals(methodNames(c)), "public/protected methods");
        check("SOURCE_CHANNEL_PRESSURE;SOURCE_DIRECTION_MAX_MIN;SOURCE_DIRECTION_MIN_MAX;SOURCE_MIDI_CONTROL;SOURCE_NONE;SOURCE_NOTE_ON_KEYNUMBER;SOURCE_NOTE_ON_VELOCITY;SOURCE_PITCH_SENSITIVITY;SOURCE_PITCH_WHEEL;SOURCE_POLARITY_BIPOLAR;SOURCE_POLARITY_UNIPOLAR;SOURCE_POLY_PRESSURE;SOURCE_TYPE_CONCAVE;SOURCE_TYPE_CONVEX;SOURCE_TYPE_LINEAR;SOURCE_TYPE_SWITCH;TRANSFORM_ABSOLUTE;TRANSFORM_LINEAR".equals(fieldNames(c)), "public/protected fields");
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
