/*
 * @test
 * @summary sun.print.PeekGraphics conformance: reflection (no init, native/display-heavy).
 * @run main PeekGraphicsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PeekGraphicsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PeekGraphics", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.PeekGraphics".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.Graphics2D".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.image.ImageObserver;java.awt.print.PrinterGraphics;java.lang.Cloneable".equals(interfaces(c)), "interfaces");
        check("private synchronized#int#getImageHeight(java.awt.Image);private synchronized#int#getImageWidth(java.awt.Image);private#void#addDrawingRect(float,float,float,float);private#void#addDrawingRect(java.awt.geom.Rectangle2D);private#void#addDrawingRect(java.awt.geom.Rectangle2D,float,float);private#void#addStrokeShape(java.awt.Shape);public synchronized#boolean#imageUpdate(java.awt.Image,int,int,int,int,int);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public#boolean#getAWTDrawingOnly();public#boolean#hit(java.awt.Rectangle,java.awt.Shape,boolean);public#boolean#hitsDrawingArea(java.awt.Rectangle);public#java.awt.Color#getBackground();public#java.awt.Color#getColor();public#java.awt.Composite#getComposite();public#java.awt.Font#getFont();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Graphics#create();public#java.awt.Graphics2D#getDelegate();public#java.awt.GraphicsConfiguration#getDeviceConfiguration();public#java.awt.Paint#getPaint();public#java.awt.Rectangle#getClipBounds();public#java.awt.RenderingHints#getRenderingHints();public#java.awt.Shape#getClip();public#java.awt.Stroke#getStroke();public#java.awt.font.FontRenderContext#getFontRenderContext();public#java.awt.geom.AffineTransform#getTransform();public#java.awt.print.PrinterJob#getPrinterJob();public#java.lang.Object#getRenderingHint(java.awt.RenderingHints$Key);public#sun.java2d.Spans#getDrawingArea();public#sun.print.PeekMetrics#getMetrics();public#void#addRenderingHints(java.util.Map);public#void#clearRect(int,int,int,int);public#void#clip(java.awt.Shape);public#void#clipRect(int,int,int,int);public#void#copyArea(int,int,int,int,int,int);public#void#dispose();public#void#draw(java.awt.Shape);public#void#drawArc(int,int,int,int,int,int);public#void#drawGlyphVector(java.awt.font.GlyphVector,float,float);public#void#drawImage(java.awt.image.BufferedImage,java.awt.image.BufferedImageOp,int,int);public#void#drawLine(int,int,int,int);public#void#drawOval(int,int,int,int);public#void#drawPolygon([I,[I,int);public#void#drawPolyline([I,[I,int);public#void#drawRenderableImage(java.awt.image.renderable.RenderableImage,java.awt.geom.AffineTransform);public#void#drawRenderedImage(java.awt.image.RenderedImage,java.awt.geom.AffineTransform);public#void#drawRoundRect(int,int,int,int,int,int);public#void#drawString(java.lang.String,float,float);public#void#drawString(java.lang.String,int,int);public#void#drawString(java.text.AttributedCharacterIterator,float,float);public#void#drawString(java.text.AttributedCharacterIterator,int,int);public#void#fill(java.awt.Shape);public#void#fillArc(int,int,int,int,int,int);public#void#fillOval(int,int,int,int);public#void#fillPolygon([I,[I,int);public#void#fillRect(int,int,int,int);public#void#fillRoundRect(int,int,int,int,int,int);public#void#finalize();public#void#rotate(double);public#void#rotate(double,double,double);public#void#scale(double,double);public#void#setAWTDrawingOnly();public#void#setBackground(java.awt.Color);public#void#setClip(int,int,int,int);public#void#setClip(java.awt.Shape);public#void#setColor(java.awt.Color);public#void#setComposite(java.awt.Composite);public#void#setDelegate(java.awt.Graphics2D);public#void#setFont(java.awt.Font);public#void#setPaint(java.awt.Paint);public#void#setPaintMode();public#void#setRenderingHint(java.awt.RenderingHints$Key,java.lang.Object);public#void#setRenderingHints(java.util.Map);public#void#setStroke(java.awt.Stroke);public#void#setTransform(java.awt.geom.AffineTransform);public#void#setXORMode(java.awt.Color);public#void#shear(double,double);public#void#transform(java.awt.geom.AffineTransform);public#void#translate(double,double);public#void#translate(int,int)".equals(methods(c)), "methods");
        check("#java.awt.Graphics2D#mGraphics;#java.awt.print.PrinterJob#mPrinterJob;private#boolean#mAWTDrawingOnly;private#sun.java2d.Spans#mDrawingArea;private#sun.print.PeekMetrics#mPrintMetrics".equals(fields(c)), "fields");
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
