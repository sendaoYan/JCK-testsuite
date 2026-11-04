/*
 * @test
 * @summary sun.print.PSPrinterJob conformance: reflection (no init, native/display-heavy).
 * @run main PSPrinterJobTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PSPrinterJobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PSPrinterJob", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.PSPrinterJob".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.print.RasterPrinterJob".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#trunc(float);#void#convertToPSPath(java.awt.geom.PathIterator);private static#int#swapBGRtoRGB([B,int,[B);private static#java.lang.String#escapeParens(java.lang.String);private static#java.util.Properties#initProps();private#[B#ascii85Encode([B);private#[B#rlEncode([B);private#[I#getPSFontIndexArray(java.awt.Font,[Lsun.awt.CharsetString;);private#[Ljava.lang.String;#printExecCmd(java.lang.String,java.lang.String,boolean,java.lang.String,int,java.lang.String);private#boolean#isOuterGState();private#java.lang.String#makeCharsetName(java.lang.String,[C);private#sun.print.PSPrinterJob$GState#getGState();private#void#grestore();private#void#gsave();private#void#prepDrawing();protected#boolean#setFont(java.awt.Font);protected#boolean#textOut(java.awt.Graphics,java.lang.String,float,float,java.awt.Font,java.awt.font.FontRenderContext,float);protected#double#getPhysicalPageHeight(java.awt.print.Paper);protected#double#getPhysicalPageWidth(java.awt.print.Paper);protected#double#getPhysicalPrintableHeight(java.awt.print.Paper);protected#double#getPhysicalPrintableWidth(java.awt.print.Paper);protected#double#getPhysicalPrintableX(java.awt.print.Paper);protected#double#getPhysicalPrintableY(java.awt.print.Paper);protected#double#getXRes();protected#double#getYRes();protected#float#getPenX();protected#float#getPenY();protected#int#getCollatedCopies();protected#int#getNoncollatedCopies();protected#int#platformFontCount(java.awt.Font,java.lang.String);protected#java.awt.Graphics2D#createPathGraphics(sun.print.PeekGraphics,java.awt.print.PrinterJob,java.awt.print.Printable,java.awt.print.PageFormat,int);protected#void#abortDoc();protected#void#beginPath();protected#void#bezierTo(float,float,float,float,float,float);protected#void#closeSubpath();protected#void#deviceFill(java.awt.geom.PathIterator,java.awt.Color,java.awt.geom.AffineTransform,java.awt.Shape);protected#void#drawImageBGR([B,float,float,float,float,float,float,float,float,int,int);protected#void#endDoc();protected#void#endPage(java.awt.print.PageFormat,java.awt.print.Printable,int);protected#void#fillPath();protected#void#lineTo(float,float);protected#void#moveTo(float,float);protected#void#printBand([B,int,int,int,int);protected#void#selectClipPath();protected#void#setClip(java.awt.Shape);protected#void#setColor(java.awt.Color);protected#void#setFillMode(int);protected#void#setTransform(java.awt.geom.AffineTransform);protected#void#startDoc();protected#void#startPage(java.awt.print.PageFormat,java.awt.print.Printable,int,boolean);public#boolean#printDialog()".equals(methods(c)), "methods");
        check("#java.awt.FontMetrics#mCurMetrics;#java.io.File#spoolFile;#java.io.PrintStream#mPSStream;#java.util.ArrayList#mGStateStack;private static final#[B#hexDigits;private static final#int#BLUE_MASK;private static final#int#BLUE_SHIFT;private static final#int#GREEN_MASK;private static final#int#GREEN_SHIFT;private static final#int#HINIBBLE_MASK;private static final#int#HINIBBLE_SHIFT;private static final#int#LOWNIBBLE_MASK;private static final#int#MAX_PSSTR;private static final#int#PS_XRES;private static final#int#PS_YRES;private static final#int#RED_MASK;private static final#int#RED_SHIFT;private static final#java.lang.String#ADOBE_PS_STR;private static final#java.lang.String#CLOSEPATH_STR;private static final#java.lang.String#COORD_PREP;private static final#java.lang.String#COPIES;private static final#java.lang.String#CURVETO_STR;private static final#java.lang.String#DrawStringName;private static final#java.lang.String#EOF_COMMENT;private static final#java.lang.String#EVEN_ODD_CLIP_STR;private static final#java.lang.String#EVEN_ODD_FILL_STR;private static final#java.lang.String#GRESTORE_STR;private static final#java.lang.String#GSAVE_STR;private static final#java.lang.String#IMAGE_RESTORE;private static final#java.lang.String#IMAGE_SAVE;private static final#java.lang.String#IMAGE_STR;private static final#java.lang.String#LINETO_STR;private static final#java.lang.String#MOVETO_STR;private static final#java.lang.String#NEWPATH_STR;private static final#java.lang.String#PAGE_COMMENT;private static final#java.lang.String#PAGE_RESTORE;private static final#java.lang.String#PAGE_SAVE;private static final#java.lang.String#READIMAGEPROC;private static final#java.lang.String#SETGRAY_STR;private static final#java.lang.String#SETRGBCOLOR_STR;private static final#java.lang.String#SHOWPAGE;private static final#java.lang.String#SetFontName;private static final#java.lang.String#WINDING_CLIP_STR;private static final#java.lang.String#WINDING_FILL_STR;private static#boolean#isMac;private static#java.util.Properties#mFontProps;private#boolean#mNoJobSheet;private#float#mPenX;private#float#mPenY;private#float#mStartPathX;private#float#mStartPathY;private#int#mDestType;private#java.awt.Color#mLastColor;private#java.awt.Font#mLastFont;private#java.awt.Shape#mLastClip;private#java.awt.geom.AffineTransform#mLastTransform;private#java.lang.String#mClipOpStr;private#java.lang.String#mDestination;private#java.lang.String#mFillOpStr;private#java.lang.String#mOptions;private#sun.print.PSPrinterJob$EPSPrinter#epsPrinter;protected static final#int#FILL_EVEN_ODD;protected static final#int#FILL_WINDING".equals(fields(c)), "fields");
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
