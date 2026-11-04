/*
 * @test
 * @summary sun.awt.image.ByteInterleavedRaster conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.image:+open
 * @run main ByteInterleavedRasterTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ByteInterleavedRasterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.ByteInterleavedRaster", false, ByteInterleavedRasterTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.image.ByteInterleavedRaster".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.image.ByteComponentRaster".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#boolean#isInterleaved(java.awt.image.ComponentSampleModel);private#void#setDataElements(int,int,int,int,int,int,java.awt.image.Raster);public#[B#getByteData(int,int,int,int,[B);public#[B#getByteData(int,int,int,int,int,[B);public#[B#getDataStorage();public#[I#getDataOffsets();public#[I#getPixels(int,int,int,int,[I);public#[I#getSamples(int,int,int,int,int,[I);public#int#getDataOffset(int);public#int#getPixelStride();public#int#getSample(int,int,int);public#int#getScanlineStride();public#java.awt.image.Raster#createChild(int,int,int,int,int,int,[I);public#java.awt.image.WritableRaster#createCompatibleWritableRaster();public#java.awt.image.WritableRaster#createCompatibleWritableRaster(int,int);public#java.awt.image.WritableRaster#createWritableChild(int,int,int,int,int,int,[I);public#java.lang.Object#getDataElements(int,int,int,int,java.lang.Object);public#java.lang.Object#getDataElements(int,int,java.lang.Object);public#java.lang.String#toString();public#void#putByteData(int,int,int,int,[B);public#void#putByteData(int,int,int,int,int,[B);public#void#setDataElements(int,int,int,int,java.lang.Object);public#void#setDataElements(int,int,java.awt.image.Raster);public#void#setDataElements(int,int,java.lang.Object);public#void#setPixels(int,int,int,int,[I);public#void#setRect(int,int,java.awt.image.Raster);public#void#setSample(int,int,int,int);public#void#setSamples(int,int,int,int,int,[I)".equals(methods(c)), "methods");
        check("#[I#bitMasks;#[I#bitOffsets;#boolean#inOrder;#boolean#packed;#int#dbOffset;#int#dbOffsetPacked;private#int#maxX;private#int#maxY".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 3, "ctors");
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
