/*
 * @test
 * @summary sun.awt.image.PNGImageDecoder conformance: reflection (no init, native/display-heavy).
 * @run main PNGImageDecoderTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PNGImageDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.PNGImageDecoder", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.image.PNGImageDecoder".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.image.ImageDecoder".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#getData();private final#boolean#getChunk();private final#int#getByte(int);private final#int#getInt(int);private final#int#getShort(int);private final#void#pngassert(boolean);private static#int#crc([B,int,int);private static#int#update_crc(int,[B,int,int);private#boolean#need(int);private#boolean#sendPixels(int,int,int,int,[B,int,int);private#boolean#sendPixels(int,int,int,int,[I,int,int);private#void#fill();private#void#filterRow([B,[B,int,int,int);private#void#property(java.lang.String,float);private#void#property(java.lang.String,java.lang.Object);private#void#readAll();protected#boolean#handleChunk(int,[B,int,int);protected#void#wrc(int);protected#void#wrk(int);public static#boolean#getCheckCRC();public static#void#setCheckCRC(boolean);public#void#print();public#void#produceImage()".equals(methods(c)), "methods");
        check("#[B#inbuf;#boolean#seenEOF;#int#chunkCRC;#int#chunkKey;#int#chunkLength;#int#chunkStart;#int#limit;#int#pos;#java.io.InputStream#underlyingInputStream;#sun.awt.image.PNGFilterInputStream#inputStream;private static final#[B#blockHeight;private static final#[B#blockWidth;private static final#[B#colIncrement;private static final#[B#rowIncrement;private static final#[B#signature;private static final#[B#startingCol;private static final#[B#startingRow;private static final#[I#crc_table;private static final#int#ALPHA;private static final#int#COLOR;private static final#int#GRAY;private static final#int#IDATChunk;private static final#int#IENDChunk;private static final#int#IHDRChunk;private static final#int#PALETTE;private static final#int#PLTEChunk;private static final#int#bKGDChunk;private static final#int#cHRMChunk;private static final#int#gAMAChunk;private static final#int#hISTChunk;private static final#int#pHYsChunk;private static final#int#sBITChunk;private static final#int#tEXtChunk;private static final#int#tIMEChunk;private static final#int#tRNSChunk;private static final#int#zTXtChunk;private static#[Ljava.awt.image.ColorModel;#greyModels;private static#boolean#checkCRC;private#[B#alpha_map;private#[B#blue_map;private#[B#green_map;private#[B#red_map;private#[B#transparentPixel_16;private#int#bitDepth;private#int#colorType;private#int#compressionMethod;private#int#filterMethod;private#int#gamma;private#int#height;private#int#interlaceMethod;private#int#transparentPixel;private#int#width;private#java.awt.image.ColorModel#cm;private#java.util.Hashtable#properties".equals(fields(c)), "fields");
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
