/*
 * @test
 * @summary sun.awt.image.ImageRepresentation conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.image:+open
 * @run main ImageRepresentationTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ImageRepresentationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.ImageRepresentation", false, ImageRepresentationTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.image.ImageRepresentation".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.image.ImageWatched".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.image.ImageConsumer".equals(interfaces(c)), "interfaces");
        check("#java.awt.image.BufferedImage#getBufferedImage();#java.awt.image.ColorModel#getColorModel();#void#createBufferedImage();#void#startProduction();private native#boolean#setDiffICM(int,int,int,int,[I,int,int,java.awt.image.IndexColorModel,[B,int,int,sun.awt.image.ByteComponentRaster,int);private native#boolean#setICMpixels(int,int,int,int,[I,[B,int,int,sun.awt.image.IntegerComponentRaster);private static native#void#initIDs();private synchronized#void#checkConsumption();private synchronized#void#decrementWaiters();private#void#convertToRGB();protected#java.awt.image.BufferedImage#createImage(java.awt.image.ColorModel,java.awt.image.WritableRaster,boolean,java.util.Hashtable);public synchronized#void#notifyWatcherListEmpty();public synchronized#void#reconstruct(int);public#boolean#drawToBufImage(java.awt.Graphics,sun.awt.image.ToolkitImage,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawToBufImage(java.awt.Graphics,sun.awt.image.ToolkitImage,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawToBufImage(java.awt.Graphics,sun.awt.image.ToolkitImage,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawToBufImage(java.awt.Graphics,sun.awt.image.ToolkitImage,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public#boolean#prepare(java.awt.image.ImageObserver);public#int#check(java.awt.image.ImageObserver);public#int#getHeight();public#int#getWidth();public#java.awt.image.BufferedImage#getOpaqueRGBImage();public#void#imageComplete(int);public#void#setAccelerationPriority(float);public#void#setColorModel(java.awt.image.ColorModel);public#void#setDimensions(int,int);public#void#setHints(int);public#void#setPixels(int,int,int,int,java.awt.image.ColorModel,[B,int,int);public#void#setPixels(int,int,int,int,java.awt.image.ColorModel,[I,int,int);public#void#setProperties(java.util.Hashtable);synchronized#void#abort();synchronized#void#dispose()".equals(methods(c)), "methods");
        check("#[I#srcLUT;#boolean#forceCMhint;#boolean#isDefaultBI;#boolean#isSameCM;#int#availinfo;#int#height;#int#hints;#int#numSrcLUT;#int#srcLUTtransIndex;#int#sstride;#int#tag;#int#width;#java.awt.Rectangle#newbits;#java.awt.image.BufferedImage#bimage;#java.awt.image.ColorModel#srcModel;#java.awt.image.WritableRaster#biRaster;#long#pData;#sun.awt.image.InputStreamImageSource#src;#sun.awt.image.ToolkitImage#image;private#boolean#consuming;private#int#numWaiters;protected#java.awt.image.ColorModel#cmodel;static#boolean#s_useNative".equals(fields(c)), "fields");
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
