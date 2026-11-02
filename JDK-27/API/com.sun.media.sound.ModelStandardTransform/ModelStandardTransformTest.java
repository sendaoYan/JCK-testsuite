/*
 * @test
 * @summary com.sun.media.sound.ModelStandardTransform conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.media.sound:+open
 * @run main ModelStandardTransformTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ModelStandardTransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelStandardTransform", false, ModelStandardTransformTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.media.sound.ModelStandardTransform".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("com.sun.media.sound.ModelTransform".equals(ifaces(c)), "interfaces");
        check("getDirection;getPolarity;getTransform;setDirection;setPolarity;setTransform;transform".equals(methodNames(c)), "public/protected methods");
        check("DIRECTION_MAX2MIN;DIRECTION_MIN2MAX;POLARITY_BIPOLAR;POLARITY_UNIPOLAR;TRANSFORM_ABSOLUTE;TRANSFORM_CONCAVE;TRANSFORM_CONVEX;TRANSFORM_LINEAR;TRANSFORM_SWITCH".equals(fieldNames(c)), "public/protected fields");
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
