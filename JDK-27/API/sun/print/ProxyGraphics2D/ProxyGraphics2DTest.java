/*
 * @test
 * @summary sun.print.ProxyGraphics2D conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.print:+open
 * @run main ProxyGraphics2DTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ProxyGraphics2DTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.ProxyGraphics2D", false, ProxyGraphics2DTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.print.ProxyGraphics2D".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.Graphics2D".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.print.PrinterGraphics".equals(interfaces(c)), "interfaces");
        check("private#boolean#needToCopyBgColorImage(java.awt.Image);private#java.awt.image.BufferedImage#getBufferedImageCopy(java.awt.Image,java.awt.Color);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public#boolean#hit(java.awt.Rectangle,java.awt.Shape,boolean);public#java.awt.Color#getBackground();public#java.awt.Color#getColor();public#java.awt.Composite#getComposite();public#java.awt.Font#getFont();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Graphics#create();public#java.awt.Graphics2D#getDelegate();public#java.awt.GraphicsConfiguration#getDeviceConfiguration();public#java.awt.Paint#getPaint();public#java.awt.Rectangle#getClipBounds();public#java.awt.RenderingHints#getRenderingHints();public#java.awt.Shape#getClip();public#java.awt.Stroke#getStroke();public#java.awt.font.FontRenderContext#getFontRenderContext();public#java.awt.geom.AffineTransform#getTransform();public#java.awt.print.PrinterJob#getPrinterJob();public#java.lang.Object#getRenderingHint(java.awt.RenderingHints$Key);public#void#addRenderingHints(java.util.Map);public#void#clearRect(int,int,int,int);public#void#clip(java.awt.Shape);public#void#clipRect(int,int,int,int);public#void#copyArea(int,int,int,int,int,int);public#void#dispose();public#void#draw(java.awt.Shape);public#void#drawArc(int,int,int,int,int,int);public#void#drawGlyphVector(java.awt.font.GlyphVector,float,float);public#void#drawImage(java.awt.image.BufferedImage,java.awt.image.BufferedImageOp,int,int);public#void#drawLine(int,int,int,int);public#void#drawOval(int,int,int,int);public#void#drawPolygon([I,[I,int);public#void#drawPolyline([I,[I,int);public#void#drawRenderableImage(java.awt.image.renderable.RenderableImage,java.awt.geom.AffineTransform);public#void#drawRenderedImage(java.awt.image.RenderedImage,java.awt.geom.AffineTransform);public#void#drawRoundRect(int,int,int,int,int,int);public#void#drawString(java.lang.String,float,float);public#void#drawString(java.lang.String,int,int);public#void#drawString(java.text.AttributedCharacterIterator,float,float);public#void#drawString(java.text.AttributedCharacterIterator,int,int);public#void#fill(java.awt.Shape);public#void#fillArc(int,int,int,int,int,int);public#void#fillOval(int,int,int,int);public#void#fillPolygon([I,[I,int);public#void#fillRect(int,int,int,int);public#void#fillRoundRect(int,int,int,int,int,int);public#void#finalize();public#void#rotate(double);public#void#rotate(double,double,double);public#void#scale(double,double);public#void#setBackground(java.awt.Color);public#void#setClip(int,int,int,int);public#void#setClip(java.awt.Shape);public#void#setColor(java.awt.Color);public#void#setComposite(java.awt.Composite);public#void#setDelegate(java.awt.Graphics2D);public#void#setFont(java.awt.Font);public#void#setPaint(java.awt.Paint);public#void#setPaintMode();public#void#setRenderingHint(java.awt.RenderingHints$Key,java.lang.Object);public#void#setRenderingHints(java.util.Map);public#void#setStroke(java.awt.Stroke);public#void#setTransform(java.awt.geom.AffineTransform);public#void#setXORMode(java.awt.Color);public#void#shear(double,double);public#void#transform(java.awt.geom.AffineTransform);public#void#translate(double,double);public#void#translate(int,int)".equals(methods(c)), "methods");
        check("#java.awt.Graphics2D#mGraphics;#java.awt.print.PrinterJob#mPrinterJob".equals(fields(c)), "fields");
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
