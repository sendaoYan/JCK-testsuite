/*
 * @test
 * @summary sun.awt.image.ImageDecoder conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.image:+open
 * @run main ImageDecoderTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ImageDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.ImageDecoder", false, ImageDecoderTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.image.ImageDecoder".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("protected#int#imageComplete(int,boolean);protected#int#setColorModel(java.awt.image.ColorModel);protected#int#setDimensions(int,int);protected#int#setHints(int);protected#int#setPixels(int,int,int,int,java.awt.image.ColorModel,[B,int,int);protected#int#setPixels(int,int,int,int,java.awt.image.ColorModel,[I,int,int);protected#int#setProperties(java.util.Hashtable);protected#sun.awt.image.ImageConsumerQueue#nextConsumer(sun.awt.image.ImageConsumerQueue);protected#void#headerComplete();public abstract#void#produceImage();public synchronized#void#close();public#boolean#isConsumer(java.awt.image.ImageConsumer);public#void#abort();public#void#removeConsumer(java.awt.image.ImageConsumer)".equals(methods(c)), "methods");
        check("#java.io.InputStream#input;#java.lang.Thread#feeder;#sun.awt.image.ImageConsumerQueue#queue;#sun.awt.image.ImageDecoder#next;#sun.awt.image.InputStreamImageSource#source;protected#boolean#aborted;protected#boolean#finished".equals(fields(c)), "fields");
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
