/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.xr.XRBackendNative conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.xr:+open
 * @run main XRBackendNativeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XRBackendNativeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.xr.XRBackendNative", false, XRBackendNativeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.xr.XRBackendNative".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.xr.XRBackend".equals(interfaces(c)), "interfaces");
        check("private native#int#createPictureNative(int,long);private native#void#XRSetTransformNative(int,int,int,int,int,int,int);private native#void#renderRectangle(int,byte,short,short,short,short,int,int,int,int);private static native#int#XRCreateLinearGradientPaintNative([F,[S,int,int,int,int,int,int);private static native#int#XRCreateRadialGradientPaintNative([F,[S,int,int,int,int,int,int);private static native#int#XRenderCreateGlyphSetNative(long);private static native#void#GCRectanglesNative(int,long,[I,int);private static native#void#XRAddGlyphsNative(int,[J,int,[B,int);private static native#void#XRFreeGlyphsNative(int,[I,int);private static native#void#XRSetClipNative(long,int,int,int,int,sun.java2d.pipe.Region,boolean);private static native#void#XRenderCompositeTextNative(int,int,int,int,int,long,[I,[I,int,int);private static native#void#XRenderRectanglesNative(int,byte,short,short,short,short,[I,int);private static native#void#initIDs();private static native#void#padBlitNative(byte,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int);private static native#void#putMaskNative(int,long,[B,int,int,int,int,int,int,int,int,float,long);private static#[J#getGlyphInfoPtrs(java.util.List);private static#long#getFormatPtr(int);private#[S#getRenderColors([I);public native#int#createPixmap(int,int,int,int);public native#long#createGC(int);public native#void#copyArea(int,int,long,int,int,int,int,int,int);public native#void#freeGC(long);public native#void#freePicture(int);public native#void#freePixmap(int);public native#void#renderComposite(byte,int,int,int,int,int,int,int,int,int,int,int);public native#void#setFilter(int,int);public native#void#setGCExposures(long,boolean);public native#void#setGCForeground(long,int);public native#void#setGCMode(long,boolean);public native#void#setPictureRepeat(int,int);public#int#XRenderCreateGlyphSet(int);public#int#createLinearGradient(java.awt.geom.Point2D,java.awt.geom.Point2D,[F,[I,int);public#int#createPicture(int,int);public#int#createRadialGradient(float,float,float,float,[F,[I,int);public#void#GCRectangles(int,long,sun.java2d.xr.GrowableRectArray);public#void#XRenderAddGlyphs(int,sun.font.GlyphList,java.util.List,[B);public#void#XRenderCompositeText(byte,int,int,int,int,int,int,int,int,sun.java2d.xr.GrowableEltArray);public#void#XRenderFreeGlyphs(int,[I);public#void#padBlit(byte,int,int,int,java.awt.geom.AffineTransform,int,int,int,int,int,int,int,int,int,int);public#void#putMaskImage(int,long,[B,int,int,int,int,int,int,int,int,float);public#void#renderRectangle(int,byte,sun.java2d.xr.XRColor,int,int,int,int);public#void#renderRectangles(int,byte,sun.java2d.xr.XRColor,sun.java2d.xr.GrowableRectArray);public#void#setClipRectangles(int,sun.java2d.pipe.Region);public#void#setGCClipRectangles(long,sun.java2d.pipe.Region);public#void#setPictureTransform(int,java.awt.geom.AffineTransform)".equals(methods(c)), "methods");
        check("private static#long#FMTPTR_A8;private static#long#FMTPTR_ARGB32;private static#long#MASK_XIMG".equals(fields(c)), "fields");
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
