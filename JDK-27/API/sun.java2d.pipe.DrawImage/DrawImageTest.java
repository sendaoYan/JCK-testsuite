/*
 * @test
 * @summary sun.java2d.pipe.DrawImage conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main DrawImageTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DrawImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.DrawImage", false, DrawImageTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.DrawImage".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.pipe.DrawImagePipe".equals(interfaces(c)), "interfaces");
        check("private static#void#blitSurfaceData(sun.java2d.SunGraphics2D,sun.java2d.pipe.Region,sun.java2d.SurfaceData,sun.java2d.SurfaceData,int,int,int,int,int,int,java.awt.Color);private#java.awt.image.BufferedImage#makeBufferedImage(java.awt.Image,java.awt.Color,int,int,int,int,int);private#java.awt.image.ColorModel#getTransformColorModel(sun.java2d.SunGraphics2D,java.awt.image.BufferedImage,java.awt.geom.AffineTransform);protected static#boolean#imageReady(sun.awt.image.ToolkitImage,java.awt.image.ImageObserver);protected static#boolean#isBgOperation(sun.java2d.SurfaceData,java.awt.Color);protected#boolean#renderImageCopy(sun.java2d.SunGraphics2D,java.awt.Image,java.awt.Color,int,int,int,int,int,int);protected#boolean#renderImageScale(sun.java2d.SunGraphics2D,java.awt.Image,java.awt.Color,int,int,int,int,int,double,double,double,double);protected#boolean#scaleSurfaceData(sun.java2d.SunGraphics2D,sun.java2d.pipe.Region,sun.java2d.SurfaceData,sun.java2d.SurfaceData,sun.java2d.loops.SurfaceType,sun.java2d.loops.SurfaceType,int,int,int,int,double,double,double,double);protected#boolean#tryCopyOrScale(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,java.awt.Color,int,[D);protected#java.awt.image.BufferedImage#getBufferedImage(java.awt.Image);protected#void#renderImageXform(sun.java2d.SunGraphics2D,java.awt.Image,java.awt.geom.AffineTransform,int,int,int,int,int,java.awt.Color);protected#void#transformImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,java.awt.geom.AffineTransform,int);protected#void#transformImage(sun.java2d.SunGraphics2D,java.awt.Image,java.awt.geom.AffineTransform,int,int,int,int,int,java.awt.Color);public static#boolean#closeToInteger(int,double);public static#boolean#isSimpleTranslate(sun.java2d.SunGraphics2D);public#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,java.awt.Color);public#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,java.awt.Color);public#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color);public#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,java.awt.Color);public#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#transformImage(sun.java2d.SunGraphics2D,java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public#void#transformImage(sun.java2d.SunGraphics2D,java.awt.image.BufferedImage,java.awt.image.BufferedImageOp,int,int)".equals(methods(c)), "methods");
        check("private static final#double#MAX_TX_ERROR".equals(fields(c)), "fields");
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
