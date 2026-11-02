/*
 * @test
 * @summary sun.awt.SunHints conformance: reflection (no init, native/display-heavy).
 * @run main SunHintsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunHintsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.SunHints", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.SunHints".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("private static final#int#NUM_KEYS;private static final#int#VALS_PER_KEY;public static final#int#INTKEY_AATEXT_LCD_CONTRAST;public static final#int#INTKEY_ALPHA_INTERPOLATION;public static final#int#INTKEY_ANTIALIASING;public static final#int#INTKEY_COLOR_RENDERING;public static final#int#INTKEY_DITHERING;public static final#int#INTKEY_FRACTIONALMETRICS;public static final#int#INTKEY_INTERPOLATION;public static final#int#INTKEY_RENDERING;public static final#int#INTKEY_RESOLUTION_VARIANT;public static final#int#INTKEY_STROKE_CONTROL;public static final#int#INTKEY_TEXT_ANTIALIASING;public static final#int#INTVAL_ALPHA_INTERPOLATION_DEFAULT;public static final#int#INTVAL_ALPHA_INTERPOLATION_QUALITY;public static final#int#INTVAL_ALPHA_INTERPOLATION_SPEED;public static final#int#INTVAL_ANTIALIAS_DEFAULT;public static final#int#INTVAL_ANTIALIAS_OFF;public static final#int#INTVAL_ANTIALIAS_ON;public static final#int#INTVAL_COLOR_RENDER_DEFAULT;public static final#int#INTVAL_COLOR_RENDER_QUALITY;public static final#int#INTVAL_COLOR_RENDER_SPEED;public static final#int#INTVAL_DITHER_DEFAULT;public static final#int#INTVAL_DITHER_DISABLE;public static final#int#INTVAL_DITHER_ENABLE;public static final#int#INTVAL_FRACTIONALMETRICS_DEFAULT;public static final#int#INTVAL_FRACTIONALMETRICS_OFF;public static final#int#INTVAL_FRACTIONALMETRICS_ON;public static final#int#INTVAL_INTERPOLATION_BICUBIC;public static final#int#INTVAL_INTERPOLATION_BILINEAR;public static final#int#INTVAL_INTERPOLATION_NEAREST_NEIGHBOR;public static final#int#INTVAL_RENDER_DEFAULT;public static final#int#INTVAL_RENDER_QUALITY;public static final#int#INTVAL_RENDER_SPEED;public static final#int#INTVAL_RESOLUTION_VARIANT_DEFAULT;public static final#int#INTVAL_RESOLUTION_VARIANT_OFF;public static final#int#INTVAL_RESOLUTION_VARIANT_ON;public static final#int#INTVAL_STROKE_DEFAULT;public static final#int#INTVAL_STROKE_NORMALIZE;public static final#int#INTVAL_STROKE_PURE;public static final#int#INTVAL_TEXT_ANTIALIAS_DEFAULT;public static final#int#INTVAL_TEXT_ANTIALIAS_GASP;public static final#int#INTVAL_TEXT_ANTIALIAS_LCD_HBGR;public static final#int#INTVAL_TEXT_ANTIALIAS_LCD_HRGB;public static final#int#INTVAL_TEXT_ANTIALIAS_LCD_VBGR;public static final#int#INTVAL_TEXT_ANTIALIAS_LCD_VRGB;public static final#int#INTVAL_TEXT_ANTIALIAS_OFF;public static final#int#INTVAL_TEXT_ANTIALIAS_ON;public static final#java.awt.RenderingHints$Key#KEY_TEXT_ANTIALIAS_LCD_CONTRAST;public static final#java.lang.Object#VALUE_ALPHA_INTERPOLATION_DEFAULT;public static final#java.lang.Object#VALUE_ALPHA_INTERPOLATION_QUALITY;public static final#java.lang.Object#VALUE_ALPHA_INTERPOLATION_SPEED;public static final#java.lang.Object#VALUE_ANTIALIAS_DEFAULT;public static final#java.lang.Object#VALUE_ANTIALIAS_OFF;public static final#java.lang.Object#VALUE_ANTIALIAS_ON;public static final#java.lang.Object#VALUE_COLOR_RENDER_DEFAULT;public static final#java.lang.Object#VALUE_COLOR_RENDER_QUALITY;public static final#java.lang.Object#VALUE_COLOR_RENDER_SPEED;public static final#java.lang.Object#VALUE_DITHER_DEFAULT;public static final#java.lang.Object#VALUE_DITHER_DISABLE;public static final#java.lang.Object#VALUE_DITHER_ENABLE;public static final#java.lang.Object#VALUE_FRACTIONALMETRICS_DEFAULT;public static final#java.lang.Object#VALUE_FRACTIONALMETRICS_OFF;public static final#java.lang.Object#VALUE_FRACTIONALMETRICS_ON;public static final#java.lang.Object#VALUE_INTERPOLATION_BICUBIC;public static final#java.lang.Object#VALUE_INTERPOLATION_BILINEAR;public static final#java.lang.Object#VALUE_INTERPOLATION_NEAREST_NEIGHBOR;public static final#java.lang.Object#VALUE_RENDER_DEFAULT;public static final#java.lang.Object#VALUE_RENDER_QUALITY;public static final#java.lang.Object#VALUE_RENDER_SPEED;public static final#java.lang.Object#VALUE_RESOLUTION_VARIANT_DEFAULT;public static final#java.lang.Object#VALUE_RESOLUTION_VARIANT_OFF;public static final#java.lang.Object#VALUE_RESOLUTION_VARIANT_ON;public static final#java.lang.Object#VALUE_STROKE_DEFAULT;public static final#java.lang.Object#VALUE_STROKE_NORMALIZE;public static final#java.lang.Object#VALUE_STROKE_PURE;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_DEFAULT;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_GASP;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_LCD_HBGR;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_LCD_HRGB;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_LCD_VBGR;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_LCD_VRGB;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_OFF;public static final#java.lang.Object#VALUE_TEXT_ANTIALIAS_ON;public static final#sun.awt.SunHints$Key#KEY_ALPHA_INTERPOLATION;public static final#sun.awt.SunHints$Key#KEY_ANTIALIASING;public static final#sun.awt.SunHints$Key#KEY_COLOR_RENDERING;public static final#sun.awt.SunHints$Key#KEY_DITHERING;public static final#sun.awt.SunHints$Key#KEY_FRACTIONALMETRICS;public static final#sun.awt.SunHints$Key#KEY_INTERPOLATION;public static final#sun.awt.SunHints$Key#KEY_RENDERING;public static final#sun.awt.SunHints$Key#KEY_RESOLUTION_VARIANT;public static final#sun.awt.SunHints$Key#KEY_STROKE_CONTROL;public static final#sun.awt.SunHints$Key#KEY_TEXT_ANTIALIASING".equals(fields(c)), "fields");
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
