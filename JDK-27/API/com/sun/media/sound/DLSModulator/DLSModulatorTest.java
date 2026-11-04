/*
 * @test
 * @summary com.sun.media.sound.DLSModulator conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.media.sound:+open
 * @run main DLSModulatorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DLSModulatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.DLSModulator", false, DLSModulatorTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.media.sound.DLSModulator".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("getControl;getDestination;getDestinationFormat;getDestinationName;getScale;getSource;getSourceName;getTransform;getVersion;setControl;setDestination;setScale;setSource;setTransform;setVersion".equals(methodNames(c)), "public/protected methods");
        check("CONN_DST_CENTER;CONN_DST_CHORUS;CONN_DST_EG1_ATTACKTIME;CONN_DST_EG1_DECAYTIME;CONN_DST_EG1_DELAYTIME;CONN_DST_EG1_HOLDTIME;CONN_DST_EG1_RELEASETIME;CONN_DST_EG1_SHUTDOWNTIME;CONN_DST_EG1_SUSTAINLEVEL;CONN_DST_EG2_ATTACKTIME;CONN_DST_EG2_DECAYTIME;CONN_DST_EG2_DELAYTIME;CONN_DST_EG2_HOLDTIME;CONN_DST_EG2_RELEASETIME;CONN_DST_EG2_SUSTAINLEVEL;CONN_DST_FILTER_CUTOFF;CONN_DST_FILTER_Q;CONN_DST_GAIN;CONN_DST_KEYNUMBER;CONN_DST_LEFT;CONN_DST_LEFTREAR;CONN_DST_LFE_CHANNEL;CONN_DST_LFO_FREQUENCY;CONN_DST_LFO_STARTDELAY;CONN_DST_NONE;CONN_DST_PAN;CONN_DST_PITCH;CONN_DST_REVERB;CONN_DST_RIGHT;CONN_DST_RIGHTREAR;CONN_DST_VIB_FREQUENCY;CONN_DST_VIB_STARTDELAY;CONN_SRC_CC1;CONN_SRC_CC10;CONN_SRC_CC11;CONN_SRC_CC7;CONN_SRC_CC91;CONN_SRC_CC93;CONN_SRC_CHANNELPRESSURE;CONN_SRC_EG1;CONN_SRC_EG2;CONN_SRC_KEYNUMBER;CONN_SRC_KEYONVELOCITY;CONN_SRC_LFO;CONN_SRC_MONOPRESSURE;CONN_SRC_NONE;CONN_SRC_PITCHWHEEL;CONN_SRC_POLYPRESSURE;CONN_SRC_RPN0;CONN_SRC_RPN1;CONN_SRC_RPN2;CONN_SRC_VIBRATO;CONN_TRN_CONCAVE;CONN_TRN_CONVEX;CONN_TRN_NONE;CONN_TRN_SWITCH;DST_FORMAT_CB;DST_FORMAT_CENT;DST_FORMAT_PERCENT;DST_FORMAT_TIMECENT".equals(fieldNames(c)), "public/protected fields");
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
