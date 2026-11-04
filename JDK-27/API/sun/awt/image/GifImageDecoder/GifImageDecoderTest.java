/*
 * @test
 * @summary sun.awt.image.GifImageDecoder conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.image:+open
 * @run main GifImageDecoderTest
 */

import java.util.*;
import java.lang.reflect.*;

public class GifImageDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.GifImageDecoder", false, GifImageDecoderTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.image.GifImageDecoder".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.image.ImageDecoder".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private native#boolean#parseImage(int,int,int,int,boolean,int,[B,[B,java.awt.image.IndexColorModel);private static final#int#ExtractByte([B,int);private static final#int#ExtractWord([B,int);private static native#void#initIDs();private static#void#error(java.lang.String);private#boolean#readImage(boolean,int,int);private#int#readBytes([B,int,int);private#int#sendPixels(int,int,int,int,[B,java.awt.image.ColorModel);private#void#readHeader();public static#[B#grow_colormap([B,int);public#void#produceImage()".equals(methods(c)), "methods");
        check("#[B#global_colormap;#[B#saved_image;#int#global_bgpixel;#int#global_height;#int#global_width;#int#num_global_colors;#int#trans_pixel;#java.awt.image.IndexColorModel#global_model;#java.awt.image.IndexColorModel#saved_model;#java.util.Hashtable#props;#sun.awt.image.GifFrame#curframe;private static final#boolean#verbose;private static final#int#COLORMAPMASK;private static final#int#EXBLOCK;private static final#int#EX_APPLICATION;private static final#int#EX_COMMENT;private static final#int#EX_GRAPHICS_CONTROL;private static final#int#IMAGESEP;private static final#int#INTERLACEMASK;private static final#int#TERMINATOR;private static final#int#TRANSPARENCYMASK;private static final#int#interlaceflags;private static final#int#normalflags;private#[B#outCode;private#[B#suffix;private#[S#prefix;private#boolean#isSavedModelReliable".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
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
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
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
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
