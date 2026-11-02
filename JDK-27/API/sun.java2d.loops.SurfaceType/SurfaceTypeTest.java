/*
 * @test
 * @summary sun.java2d.loops.SurfaceType conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.loops:+open
 * @run main SurfaceTypeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SurfaceTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.loops.SurfaceType", false, SurfaceTypeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.loops.SurfaceType".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static synchronized#int#makeUniqueID(java.lang.String);public#boolean#equals(java.lang.Object);public#int#getAlphaMask();public#int#getUniqueID();public#int#hashCode();public#int#pixelFor(int,java.awt.image.ColorModel);public#int#rgbFor(int,java.awt.image.ColorModel);public#java.lang.String#getDescriptor();public#java.lang.String#toString();public#sun.awt.image.PixelConverter#getPixelConverter();public#sun.java2d.loops.SurfaceType#deriveSubType(java.lang.String);public#sun.java2d.loops.SurfaceType#deriveSubType(java.lang.String,sun.awt.image.PixelConverter);public#sun.java2d.loops.SurfaceType#getSuperType()".equals(methods(c)), "methods");
        check("private static#int#unusedUID;private static#java.util.HashMap#surfaceUIDMap;private#int#uniqueID;private#java.lang.String#desc;private#sun.java2d.loops.SurfaceType#next;protected#sun.awt.image.PixelConverter#pixelConverter;public static final#java.lang.String#DESC_3BYTE_BGR;public static final#java.lang.String#DESC_3BYTE_RGB;public static final#java.lang.String#DESC_4BYTE_ABGR;public static final#java.lang.String#DESC_4BYTE_ABGR_PRE;public static final#java.lang.String#DESC_ANY;public static final#java.lang.String#DESC_ANY_3BYTE;public static final#java.lang.String#DESC_ANY_4BYTE;public static final#java.lang.String#DESC_ANY_BYTE;public static final#java.lang.String#DESC_ANY_COLOR;public static final#java.lang.String#DESC_ANY_INT;public static final#java.lang.String#DESC_ANY_INT_DCM;public static final#java.lang.String#DESC_ANY_PAINT;public static final#java.lang.String#DESC_ANY_SHORT;public static final#java.lang.String#DESC_BYTE_BINARY;public static final#java.lang.String#DESC_BYTE_BINARY_1BIT;public static final#java.lang.String#DESC_BYTE_BINARY_2BIT;public static final#java.lang.String#DESC_BYTE_BINARY_4BIT;public static final#java.lang.String#DESC_BYTE_GRAY;public static final#java.lang.String#DESC_BYTE_INDEXED;public static final#java.lang.String#DESC_BYTE_INDEXED_BM;public static final#java.lang.String#DESC_BYTE_INDEXED_OPAQUE;public static final#java.lang.String#DESC_GRADIENT_PAINT;public static final#java.lang.String#DESC_INDEX12_GRAY;public static final#java.lang.String#DESC_INDEX8_GRAY;public static final#java.lang.String#DESC_INT_ARGB;public static final#java.lang.String#DESC_INT_ARGB_BM;public static final#java.lang.String#DESC_INT_ARGB_PRE;public static final#java.lang.String#DESC_INT_BGR;public static final#java.lang.String#DESC_INT_BGRx;public static final#java.lang.String#DESC_INT_RGB;public static final#java.lang.String#DESC_INT_RGBx;public static final#java.lang.String#DESC_LINEAR_GRADIENT_PAINT;public static final#java.lang.String#DESC_OPAQUE_COLOR;public static final#java.lang.String#DESC_OPAQUE_GRADIENT_PAINT;public static final#java.lang.String#DESC_OPAQUE_LINEAR_GRADIENT_PAINT;public static final#java.lang.String#DESC_OPAQUE_RADIAL_GRADIENT_PAINT;public static final#java.lang.String#DESC_OPAQUE_TEXTURE_PAINT;public static final#java.lang.String#DESC_RADIAL_GRADIENT_PAINT;public static final#java.lang.String#DESC_TEXTURE_PAINT;public static final#java.lang.String#DESC_USHORT_4444_ARGB;public static final#java.lang.String#DESC_USHORT_555_RGB;public static final#java.lang.String#DESC_USHORT_555_RGBx;public static final#java.lang.String#DESC_USHORT_565_RGB;public static final#java.lang.String#DESC_USHORT_GRAY;public static final#java.lang.String#DESC_USHORT_INDEXED;public static final#sun.java2d.loops.SurfaceType#Any;public static final#sun.java2d.loops.SurfaceType#Any3Byte;public static final#sun.java2d.loops.SurfaceType#Any4Byte;public static final#sun.java2d.loops.SurfaceType#AnyByte;public static final#sun.java2d.loops.SurfaceType#AnyByteBinary;public static final#sun.java2d.loops.SurfaceType#AnyColor;public static final#sun.java2d.loops.SurfaceType#AnyDcm;public static final#sun.java2d.loops.SurfaceType#AnyInt;public static final#sun.java2d.loops.SurfaceType#AnyPaint;public static final#sun.java2d.loops.SurfaceType#AnyShort;public static final#sun.java2d.loops.SurfaceType#ByteBinary1Bit;public static final#sun.java2d.loops.SurfaceType#ByteBinary2Bit;public static final#sun.java2d.loops.SurfaceType#ByteBinary4Bit;public static final#sun.java2d.loops.SurfaceType#ByteGray;public static final#sun.java2d.loops.SurfaceType#ByteIndexed;public static final#sun.java2d.loops.SurfaceType#ByteIndexedBm;public static final#sun.java2d.loops.SurfaceType#ByteIndexedOpaque;public static final#sun.java2d.loops.SurfaceType#Custom;public static final#sun.java2d.loops.SurfaceType#FourByteAbgr;public static final#sun.java2d.loops.SurfaceType#FourByteAbgrPre;public static final#sun.java2d.loops.SurfaceType#GradientPaint;public static final#sun.java2d.loops.SurfaceType#Index12Gray;public static final#sun.java2d.loops.SurfaceType#Index8Gray;public static final#sun.java2d.loops.SurfaceType#IntArgb;public static final#sun.java2d.loops.SurfaceType#IntArgbBm;public static final#sun.java2d.loops.SurfaceType#IntArgbPre;public static final#sun.java2d.loops.SurfaceType#IntBgr;public static final#sun.java2d.loops.SurfaceType#IntBgrx;public static final#sun.java2d.loops.SurfaceType#IntRgb;public static final#sun.java2d.loops.SurfaceType#IntRgbx;public static final#sun.java2d.loops.SurfaceType#LinearGradientPaint;public static final#sun.java2d.loops.SurfaceType#OpaqueColor;public static final#sun.java2d.loops.SurfaceType#OpaqueGradientPaint;public static final#sun.java2d.loops.SurfaceType#OpaqueLinearGradientPaint;public static final#sun.java2d.loops.SurfaceType#OpaqueRadialGradientPaint;public static final#sun.java2d.loops.SurfaceType#OpaqueTexturePaint;public static final#sun.java2d.loops.SurfaceType#RadialGradientPaint;public static final#sun.java2d.loops.SurfaceType#TexturePaint;public static final#sun.java2d.loops.SurfaceType#ThreeByteBgr;public static final#sun.java2d.loops.SurfaceType#ThreeByteRgb;public static final#sun.java2d.loops.SurfaceType#Ushort4444Argb;public static final#sun.java2d.loops.SurfaceType#Ushort555Rgb;public static final#sun.java2d.loops.SurfaceType#Ushort555Rgbx;public static final#sun.java2d.loops.SurfaceType#Ushort565Rgb;public static final#sun.java2d.loops.SurfaceType#UshortGray;public static final#sun.java2d.loops.SurfaceType#UshortIndexed".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
