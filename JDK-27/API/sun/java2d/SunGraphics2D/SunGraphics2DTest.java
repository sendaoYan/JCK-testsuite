/*
 * @test
 * @summary sun.java2d.SunGraphics2D conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d:+open
 * @run main SunGraphics2DTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunGraphics2DTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.SunGraphics2D", false, SunGraphics2DTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.SunGraphics2D".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.awt.Graphics2D".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Cloneable;sun.awt.ConstrainableGraphics;sun.java2d.DestSurfaceProvider".equals(interfaces(c)), "interfaces");
        check("#java.awt.RenderingHints#makeHints(java.util.Map);#java.awt.Shape#intersectByArea(java.awt.Shape,java.awt.Shape,boolean,boolean);#java.awt.Shape#intersectRectShape(java.awt.geom.Rectangle2D,java.awt.Shape,boolean,boolean);#java.awt.Shape#intersectShapes(java.awt.Shape,java.awt.Shape,boolean,boolean);#void#validateColor();private static#boolean#isIntegerTranslation(java.awt.geom.AffineTransform);private static#int#getTileIndex(int,int,int);private static#java.awt.Rectangle#getImageRegion(java.awt.image.RenderedImage,sun.java2d.pipe.Region,java.awt.geom.AffineTransform,java.awt.geom.AffineTransform,int,int);private static#void#fixRectangleOrientation([D,java.awt.geom.Rectangle2D);private#boolean#clipTo(java.awt.Rectangle,java.awt.Rectangle);private#boolean#scaleImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);private#boolean#transformImage(java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);private#java.awt.Image#getResolutionVariant(java.awt.image.MultiResolutionImage,int,int,int,int,int,int,int,int,int,int,java.awt.geom.AffineTransform);private#java.awt.geom.AffineTransform#getDefaultTransform();private#java.lang.Boolean#drawHiDPIImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver,java.awt.geom.AffineTransform);private#void#doCopyArea(int,int,int,int,int,int);private#void#drawTranslatedRenderedImage(java.awt.image.RenderedImage,java.awt.Rectangle,int,int);private#void#revalidateAll();private#void#validateBasicStroke(java.awt.BasicStroke);protected static#java.awt.Shape#cloneShape(java.awt.Shape);protected static#java.awt.Shape#transformShape(int,int,java.awt.Shape);protected static#java.awt.Shape#transformShape(java.awt.geom.AffineTransform,java.awt.Shape);protected#java.awt.Shape#transformShape(java.awt.Shape);protected#java.lang.Object#clone();protected#void#invalidatePipe();protected#void#invalidateTransform();protected#void#validateCompClip();public#boolean#copyImage(java.awt.Image,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public#boolean#hit(java.awt.Rectangle,java.awt.Shape,boolean);public#boolean#hitClip(int,int,int,int);public#java.awt.Color#getBackground();public#java.awt.Color#getColor();public#java.awt.Composite#getComposite();public#java.awt.Font#getFont();public#java.awt.FontMetrics#getFontMetrics();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Graphics#create();public#java.awt.GraphicsConfiguration#getDeviceConfiguration();public#java.awt.Paint#getPaint();public#java.awt.Rectangle#getClipBounds();public#java.awt.Rectangle#getClipBounds(java.awt.Rectangle);public#java.awt.RenderingHints#getRenderingHints();public#java.awt.Shape#getClip();public#java.awt.Shape#untransformShape(java.awt.Shape);public#java.awt.Stroke#getStroke();public#java.awt.font.FontRenderContext#getFontRenderContext();public#java.awt.geom.AffineTransform#cloneTransform();public#java.awt.geom.AffineTransform#getTransform();public#java.awt.image.ColorModel#getDeviceColorModel();public#java.lang.Object#getDestination();public#java.lang.Object#getRenderingHint(java.awt.RenderingHints$Key);public#sun.java2d.Surface#getDestSurface();public#sun.java2d.SurfaceData#getSurfaceData();public#sun.java2d.loops.FontInfo#checkFontInfo(sun.java2d.loops.FontInfo,java.awt.Font,java.awt.font.FontRenderContext);public#sun.java2d.loops.FontInfo#getFontInfo();public#sun.java2d.loops.FontInfo#getGVFontInfo(java.awt.Font,java.awt.font.FontRenderContext);public#sun.java2d.pipe.Region#getCompClip();public#void#addRenderingHints(java.util.Map);public#void#clearRect(int,int,int,int);public#void#clip(java.awt.Shape);public#void#clipRect(int,int,int,int);public#void#constrain(int,int,int,int);public#void#constrain(int,int,int,int,sun.java2d.pipe.Region);public#void#copyArea(int,int,int,int,int,int);public#void#dispose();public#void#draw(java.awt.Shape);public#void#drawArc(int,int,int,int,int,int);public#void#drawBytes([B,int,int,int,int);public#void#drawChars([C,int,int,int,int);public#void#drawGlyphVector(java.awt.font.GlyphVector,float,float);public#void#drawImage(java.awt.image.BufferedImage,java.awt.image.BufferedImageOp,int,int);public#void#drawLine(int,int,int,int);public#void#drawOval(int,int,int,int);public#void#drawPolygon([I,[I,int);public#void#drawPolyline([I,[I,int);public#void#drawRect(int,int,int,int);public#void#drawRenderableImage(java.awt.image.renderable.RenderableImage,java.awt.geom.AffineTransform);public#void#drawRenderedImage(java.awt.image.RenderedImage,java.awt.geom.AffineTransform);public#void#drawRoundRect(int,int,int,int,int,int);public#void#drawString(java.lang.String,float,float);public#void#drawString(java.lang.String,int,int);public#void#drawString(java.text.AttributedCharacterIterator,float,float);public#void#drawString(java.text.AttributedCharacterIterator,int,int);public#void#fill(java.awt.Shape);public#void#fillArc(int,int,int,int,int,int);public#void#fillOval(int,int,int,int);public#void#fillPolygon([I,[I,int);public#void#fillRect(int,int,int,int);public#void#fillRoundRect(int,int,int,int,int,int);public#void#finalize();public#void#rotate(double);public#void#rotate(double,double,double);public#void#scale(double,double);public#void#setBackground(java.awt.Color);public#void#setClip(int,int,int,int);public#void#setClip(java.awt.Shape);public#void#setColor(java.awt.Color);public#void#setComposite(java.awt.Composite);public#void#setDevClip(int,int,int,int);public#void#setDevClip(java.awt.Rectangle);public#void#setFont(java.awt.Font);public#void#setPaint(java.awt.Paint);public#void#setPaintMode();public#void#setRenderingHint(java.awt.RenderingHints$Key,java.lang.Object);public#void#setRenderingHints(java.util.Map);public#void#setStroke(java.awt.Stroke);public#void#setTransform(java.awt.geom.AffineTransform);public#void#setXORMode(java.awt.Color);public#void#shear(double,double);public#void#transform(java.awt.geom.AffineTransform);public#void#translate(double,double);public#void#translate(int,int);public#void#validatePipe()".equals(methods(c)), "methods");
        check("#java.awt.Composite#lastCAcomp;#sun.java2d.loops.Blit#lastCAblit;private static final#[D#IDENT_MATRIX;private static final#int#MINALLOCATED;private static final#int#TEXTARRSIZE;private static final#java.awt.Font#defaultFont;private static final#java.awt.geom.AffineTransform#IDENT_ATX;private static#[Ljava.awt.geom.AffineTransform;#textAtArr;private static#[[D#textTxArr;private static#int#lcdTextContrastDefaultValue;private#boolean#validFontInfo;private#int#interpolationHint;private#int#resolutionVariantHint;private#java.awt.font.FontRenderContext#cachedFRC;private#java.awt.font.FontRenderContext#glyphVectorFRC;private#sun.java2d.loops.FontInfo#fontInfo;private#sun.java2d.loops.FontInfo#glyphVectorFontInfo;protected static final#java.awt.Composite#defaultComposite;protected static final#java.awt.Stroke#defaultStroke;protected static#sun.java2d.pipe.ValidatePipe#invalidpipe;protected#int#fractionalMetricsHint;protected#java.awt.Font#font;protected#java.awt.FontMetrics#fontMetrics;protected#sun.java2d.pipe.Region#devClip;public static final#double#MinPenSizeAA;public static final#double#MinPenSizeAASquared;public static final#double#MinPenSizeSquared;public static final#int#CLIP_DEVICE;public static final#int#CLIP_RECTANGULAR;public static final#int#CLIP_SHAPE;public static final#int#COMP_ALPHA;public static final#int#COMP_CUSTOM;public static final#int#COMP_ISCOPY;public static final#int#COMP_XOR;public static final#int#PAINT_ALPHACOLOR;public static final#int#PAINT_CUSTOM;public static final#int#PAINT_GRADIENT;public static final#int#PAINT_LIN_GRADIENT;public static final#int#PAINT_OPAQUECOLOR;public static final#int#PAINT_RAD_GRADIENT;public static final#int#PAINT_TEXTURE;public static final#int#STROKE_CUSTOM;public static final#int#STROKE_THIN;public static final#int#STROKE_THINDASHED;public static final#int#STROKE_WIDE;public static final#int#TRANSFORM_ANY_TRANSLATE;public static final#int#TRANSFORM_GENERIC;public static final#int#TRANSFORM_INT_TRANSLATE;public static final#int#TRANSFORM_ISIDENT;public static final#int#TRANSFORM_TRANSLATESCALE;public#int#antialiasHint;public#int#clipState;public#int#compositeState;public#int#constrainX;public#int#constrainY;public#int#eargb;public#int#interpolationType;public#int#lcdTextContrast;public#int#paintState;public#int#pixel;public#int#renderHint;public#int#strokeHint;public#int#strokeState;public#int#textAntialiasHint;public#int#transX;public#int#transY;public#int#transformState;public#java.awt.Color#backgroundColor;public#java.awt.Color#foregroundColor;public#java.awt.Composite#composite;public#java.awt.Paint#paint;public#java.awt.RenderingHints#hints;public#java.awt.Shape#usrClip;public#java.awt.Stroke#stroke;public#java.awt.geom.AffineTransform#transform;public#sun.java2d.SurfaceData#surfaceData;public#sun.java2d.loops.CompositeType#imageComp;public#sun.java2d.loops.MaskFill#alphafill;public#sun.java2d.loops.RenderLoops#loops;public#sun.java2d.pipe.DrawImagePipe#imagepipe;public#sun.java2d.pipe.PixelDrawPipe#drawpipe;public#sun.java2d.pipe.PixelFillPipe#fillpipe;public#sun.java2d.pipe.Region#clipRegion;public#sun.java2d.pipe.Region#constrainClip;public#sun.java2d.pipe.ShapeDrawPipe#shapepipe;public#sun.java2d.pipe.TextPipe#textpipe;static final#int#NON_RECTILINEAR_TRANSFORM_MASK;static final#int#NON_UNIFORM_SCALE_MASK".equals(fields(c)), "fields");
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
