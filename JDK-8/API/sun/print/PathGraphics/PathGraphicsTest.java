/*
 * @test
 * @summary sun.print.PathGraphics conformance: reflection (no init, native/display-heavy).
 * @run main PathGraphicsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PathGraphicsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PathGraphics", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.PathGraphics".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.print.ProxyGraphics2D".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#printedSimpleGlyphVector(java.awt.font.GlyphVector,float,float);private static#[C#getGlyphToCharMapForFont(sun.font.Font2D);private#boolean#samePositions(java.awt.font.GlyphVector,[I,[I,[F);protected abstract#boolean#drawImageToPlatform(java.awt.Image,java.awt.geom.AffineTransform,java.awt.Color,int,int,int,int,boolean);protected abstract#void#deviceClip(java.awt.geom.PathIterator);protected abstract#void#deviceDrawLine(int,int,int,int,java.awt.Color);protected abstract#void#deviceFill(java.awt.geom.PathIterator,java.awt.Color);protected abstract#void#deviceFillRect(int,int,int,int,java.awt.Color);protected abstract#void#deviceFrameRect(int,int,int,int,java.awt.Color);protected#boolean#canDrawStringToWidth();protected#boolean#drawBitmaskImage(java.awt.image.BufferedImage,java.awt.geom.AffineTransform,java.awt.Color,int,int,int,int);protected#boolean#hasTransparentPixels(java.awt.image.BufferedImage);protected#boolean#isBitmaskTransparency(java.awt.image.BufferedImage);protected#boolean#printGlyphVector(java.awt.font.GlyphVector,float,float);protected#int#getPageIndex();protected#int#platformFontCount(java.awt.Font,java.lang.String);protected#java.awt.image.BufferedImage#getBufferedImage(java.awt.Image);protected#java.awt.print.PageFormat#getPageFormat();protected#java.awt.print.Printable#getPrintable();protected#void#drawString(java.lang.String,float,float,java.awt.Font,java.awt.font.FontRenderContext,float);public abstract#void#redrawRegion(java.awt.geom.Rectangle2D,double,double,java.awt.Shape,java.awt.geom.AffineTransform);public#boolean#canDoRedraws();public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public#void#clearRect(int,int,int,int);public#void#draw(java.awt.Shape);public#void#drawArc(int,int,int,int,int,int);public#void#drawGlyphVector(java.awt.font.GlyphVector,float,float);public#void#drawImage(java.awt.image.BufferedImage,java.awt.image.BufferedImageOp,int,int);public#void#drawLine(int,int,int,int);public#void#drawOval(int,int,int,int);public#void#drawPolygon([I,[I,int);public#void#drawPolygon(java.awt.Polygon);public#void#drawPolyline([I,[I,int);public#void#drawRect(int,int,int,int);public#void#drawRenderedImage(java.awt.image.RenderedImage,java.awt.geom.AffineTransform);public#void#drawRoundRect(int,int,int,int,int,int);public#void#drawString(java.lang.String,float,float);public#void#drawString(java.lang.String,int,int);public#void#drawString(java.text.AttributedCharacterIterator,float,float);public#void#drawString(java.text.AttributedCharacterIterator,int,int);public#void#fill(java.awt.Shape);public#void#fill(java.awt.Shape,java.awt.Color);public#void#fillArc(int,int,int,int,int,int);public#void#fillOval(int,int,int,int);public#void#fillPolygon([I,[I,int);public#void#fillPolygon(java.awt.Polygon);public#void#fillRect(int,int,int,int);public#void#fillRoundRect(int,int,int,int,int,int)".equals(methods(c)), "methods");
        check("private#boolean#mCanRedraw;private#int#mPageIndex;private#java.awt.print.PageFormat#mPageFormat;private#java.awt.print.Printable#mPainter;protected static#java.lang.ref.SoftReference#fontMapRef;protected#boolean#printingGlyphVector".equals(fields(c)), "fields");
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
