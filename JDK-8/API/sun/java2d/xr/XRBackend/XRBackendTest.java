/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.xr.XRBackend conformance: reflection (no init, native/display-heavy).
 * @run main XRBackendTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XRBackendTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.xr.XRBackend", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.xr.XRBackend".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.getSuperclass() == null, "superclass null");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#int#XRenderCreateGlyphSet(int);public abstract#int#createLinearGradient(java.awt.geom.Point2D,java.awt.geom.Point2D,[F,[I,int);public abstract#int#createPicture(int,int);public abstract#int#createPixmap(int,int,int,int);public abstract#int#createRadialGradient(float,float,float,float,[F,[I,int);public abstract#long#createGC(int);public abstract#void#GCRectangles(int,long,sun.java2d.xr.GrowableRectArray);public abstract#void#XRenderAddGlyphs(int,sun.font.GlyphList,java.util.List,[B);public abstract#void#XRenderCompositeText(byte,int,int,int,int,int,int,int,int,sun.java2d.xr.GrowableEltArray);public abstract#void#XRenderFreeGlyphs(int,[I);public abstract#void#copyArea(int,int,long,int,int,int,int,int,int);public abstract#void#freeGC(long);public abstract#void#freePicture(int);public abstract#void#freePixmap(int);public abstract#void#putMaskImage(int,long,[B,int,int,int,int,int,int,int,int,float);public abstract#void#renderComposite(byte,int,int,int,int,int,int,int,int,int,int,int);public abstract#void#renderCompositeTrapezoids(byte,int,int,int,int,int,sun.java2d.jules.TrapezoidList);public abstract#void#renderRectangle(int,byte,sun.java2d.xr.XRColor,int,int,int,int);public abstract#void#renderRectangles(int,byte,sun.java2d.xr.XRColor,sun.java2d.xr.GrowableRectArray);public abstract#void#setClipRectangles(int,sun.java2d.pipe.Region);public abstract#void#setFilter(int,int);public abstract#void#setGCClipRectangles(long,sun.java2d.pipe.Region);public abstract#void#setGCExposures(long,boolean);public abstract#void#setGCForeground(long,int);public abstract#void#setGCMode(long,boolean);public abstract#void#setPictureRepeat(int,int);public abstract#void#setPictureTransform(int,java.awt.geom.AffineTransform)".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 0, "ctors");
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
