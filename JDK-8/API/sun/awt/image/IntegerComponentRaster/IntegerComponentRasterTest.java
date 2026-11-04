/*
 * @test
 * @summary sun.awt.image.IntegerComponentRaster conformance: reflection (no init, native/display-heavy).
 * @run main IntegerComponentRasterTest
 */

import java.util.*;
import java.lang.reflect.*;

public class IntegerComponentRasterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.IntegerComponentRaster", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.image.IntegerComponentRaster".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.image.SunWritableRaster".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static native#void#initIDs();private#void#setDataElements(int,int,int,int,java.awt.image.Raster);protected final#void#verify();public#[I#getDataOffsets();public#[I#getDataStorage();public#int#getDataOffset(int);public#int#getPixelStride();public#int#getScanlineStride();public#java.awt.image.Raster#createChild(int,int,int,int,int,int,[I);public#java.awt.image.WritableRaster#createCompatibleWritableRaster();public#java.awt.image.WritableRaster#createCompatibleWritableRaster(int,int);public#java.awt.image.WritableRaster#createWritableChild(int,int,int,int,int,int,[I);public#java.lang.Object#getDataElements(int,int,int,int,java.lang.Object);public#java.lang.Object#getDataElements(int,int,java.lang.Object);public#java.lang.String#toString();public#void#setDataElements(int,int,int,int,java.lang.Object);public#void#setDataElements(int,int,java.awt.image.Raster);public#void#setDataElements(int,int,java.lang.Object)".equals(methods(c)), "methods");
        check("#int#type;private#int#maxX;private#int#maxY;protected#[I#data;protected#[I#dataOffsets;protected#int#bandOffset;protected#int#numDataElems;protected#int#pixelStride;protected#int#scanlineStride;static final#int#TYPE_BYTE_BANDED_SAMPLES;static final#int#TYPE_BYTE_BINARY_SAMPLES;static final#int#TYPE_BYTE_PACKED_SAMPLES;static final#int#TYPE_BYTE_SAMPLES;static final#int#TYPE_CUSTOM;static final#int#TYPE_INT_8BIT_SAMPLES;static final#int#TYPE_INT_BANDED_SAMPLES;static final#int#TYPE_INT_PACKED_SAMPLES;static final#int#TYPE_INT_SAMPLES;static final#int#TYPE_USHORT_BANDED_SAMPLES;static final#int#TYPE_USHORT_PACKED_SAMPLES;static final#int#TYPE_USHORT_SAMPLES".equals(fields(c)), "fields");
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
