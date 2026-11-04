/*
 * @test
 * @summary sun.awt.image.BufferedImageGraphicsConfig conformance: reflection (no init, native/display-heavy).
 * @run main BufferedImageGraphicsConfigTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedImageGraphicsConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.BufferedImageGraphicsConfig", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.image.BufferedImageGraphicsConfig".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.GraphicsConfiguration".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#sun.awt.image.BufferedImageGraphicsConfig#getConfig(java.awt.image.BufferedImage);public#java.awt.GraphicsDevice#getDevice();public#java.awt.Rectangle#getBounds();public#java.awt.geom.AffineTransform#getDefaultTransform();public#java.awt.geom.AffineTransform#getNormalizingTransform();public#java.awt.image.BufferedImage#createCompatibleImage(int,int);public#java.awt.image.ColorModel#getColorModel();public#java.awt.image.ColorModel#getColorModel(int)".equals(methods(c)), "methods");
        check("#int#height;#int#width;#java.awt.GraphicsDevice#gd;#java.awt.image.ColorModel#model;#java.awt.image.Raster#raster;private static final#int#numconfigs;private static#[Lsun.awt.image.BufferedImageGraphicsConfig;#configs".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
