/*
 * @test
 * @summary sun.print.RasterPrinterJob conformance: reflection (no init, native/display-heavy).
 * @run main RasterPrinterJobTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RasterPrinterJobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.RasterPrinterJob", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.RasterPrinterJob".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.print.PrinterJob".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#java.awt.print.PageFormat#attributeToPageFormat(javax.print.PrintService,javax.print.attribute.PrintRequestAttributeSet);private#long#getParentWindowID();private#void#clearParentWindowID();private#void#setParentWindowID(javax.print.attribute.PrintRequestAttributeSet);private#void#throwPrintToFile();protected abstract#double#getPhysicalPageHeight(java.awt.print.Paper);protected abstract#double#getPhysicalPageWidth(java.awt.print.Paper);protected abstract#double#getPhysicalPrintableHeight(java.awt.print.Paper);protected abstract#double#getPhysicalPrintableWidth(java.awt.print.Paper);protected abstract#double#getPhysicalPrintableX(java.awt.print.Paper);protected abstract#double#getPhysicalPrintableY(java.awt.print.Paper);protected abstract#double#getXRes();protected abstract#double#getYRes();protected abstract#void#abortDoc();protected abstract#void#endDoc();protected abstract#void#endPage(java.awt.print.PageFormat,java.awt.print.Printable,int);protected abstract#void#printBand([B,int,int,int,int);protected abstract#void#startDoc();protected abstract#void#startPage(java.awt.print.PageFormat,java.awt.print.Printable,int,boolean);protected final#int#getFromPageAttrib();protected final#int#getMaxPageAttrib();protected final#int#getMinPageAttrib();protected final#int#getSelectAttrib();protected final#int#getToPageAttrib();protected static#javax.print.PrintService#lookupDefaultPrintService();protected#boolean#isCollated();protected#boolean#isSupportedValue(javax.print.attribute.Attribute,javax.print.attribute.PrintRequestAttributeSet);protected#int#getCollatedCopies();protected#int#getCopiesInt();protected#int#getFirstPage();protected#int#getLastPage();protected#int#getNoncollatedCopies();protected#int#printPage(java.awt.print.Pageable,int);protected#java.awt.Graphics2D#createPathGraphics(sun.print.PeekGraphics,java.awt.print.PrinterJob,java.awt.print.Printable,java.awt.print.PageFormat,int);protected#java.awt.print.PageFormat#getPageFormatFromAttributes();protected#java.awt.print.Pageable#getPageable();protected#java.lang.String#getJobNameInt();protected#java.lang.String#getUserNameInt();protected#java.lang.String#removeControlChars(java.lang.String);protected#javax.print.attribute.standard.MediaPrintableArea#getDefaultPrintableArea(java.awt.print.PageFormat,double,double);protected#javax.print.attribute.standard.MediaSize#getMediaSize(javax.print.attribute.standard.Media,javax.print.PrintService,java.awt.print.PageFormat);protected#sun.print.PeekGraphics#createPeekGraphics(java.awt.Graphics2D,java.awt.print.PrinterJob);protected#void#cancelDoc();protected#void#debug_println(java.lang.String);protected#void#initPrinter();protected#void#initPrinterGraphics(java.awt.Graphics2D,java.awt.geom.Rectangle2D);protected#void#setAttributes(javax.print.attribute.PrintRequestAttributeSet);protected#void#setCollated(boolean);protected#void#setPageRange(int,int);protected#void#spoolToService(javax.print.PrintService,javax.print.attribute.PrintRequestAttributeSet);protected#void#updateAttributesWithPageFormat(javax.print.PrintService,java.awt.print.PageFormat,javax.print.attribute.PrintRequestAttributeSet);protected#void#updatePageAttributes(javax.print.PrintService,java.awt.print.PageFormat);protected#void#validateDestination(java.lang.String);protected#void#validatePaper(java.awt.print.Paper,java.awt.print.Paper);public#boolean#checkAllowedToPrintToFile();public#boolean#isCancelled();public#boolean#printDialog();public#boolean#printDialog(javax.print.attribute.PrintRequestAttributeSet);public#int#getCopies();public#java.awt.print.PageFormat#defaultPage(java.awt.print.PageFormat);public#java.awt.print.PageFormat#pageDialog(java.awt.print.PageFormat);public#java.awt.print.PageFormat#pageDialog(javax.print.attribute.PrintRequestAttributeSet);public#java.awt.print.PageFormat#validatePage(java.awt.print.PageFormat);public#java.lang.String#getJobName();public#java.lang.String#getUserName();public#javax.print.PrintService#getPrintService();public#void#cancel();public#void#print();public#void#print(javax.print.attribute.PrintRequestAttributeSet);public#void#saveState(java.awt.geom.AffineTransform,java.awt.Shape,java.awt.geom.Rectangle2D,double,double);public#void#setCopies(int);public#void#setJobName(java.lang.String);public#void#setPageable(java.awt.print.Pageable);public#void#setPrintService(javax.print.PrintService);public#void#setPrintable(java.awt.print.Printable);public#void#setPrintable(java.awt.print.Printable,java.awt.print.PageFormat);synchronized#sun.print.PrinterGraphicsConfig#getPrinterGraphicsConfig();synchronized#void#setGraphicsConfigInfo(java.awt.geom.AffineTransform,double,double)".equals(methods(c)), "methods");
        check("private static final#float#DPI;private static final#int#MAX_BAND_SIZE;private static final#java.lang.String#FORCE_PDL;private static final#java.lang.String#FORCE_PIPE_PROP;private static final#java.lang.String#FORCE_RASTER;private static final#java.lang.String#SHAPE_TEXT_PROP;private#boolean#mCollate;private#int#cachedBandHeight;private#int#cachedBandWidth;private#int#copiesAttr;private#int#deviceHeight;private#int#deviceWidth;private#int#mFirstPage;private#int#mLastPage;private#int#mNumCopies;private#java.awt.geom.AffineTransform#defaultDeviceTransform;private#java.awt.image.BufferedImage#cachedBand;private#java.awt.print.Paper#previousPaper;private#java.io.FilePermission#printToFilePermission;private#java.lang.String#jobNameAttr;private#java.lang.String#mDocName;private#java.lang.String#userNameAttr;private#java.util.ArrayList#redrawList;private#javax.print.attribute.standard.PageRanges#pageRangesAttr;private#long#parentWindowID;private#sun.print.DialogOnTop#onTop;private#sun.print.PrinterGraphicsConfig#pgConfig;protected static final#int#FILE;protected static final#int#MAX_UNKNOWN_PAGES;protected static final#int#PD_ALLPAGES;protected static final#int#PD_NOSELECTION;protected static final#int#PD_PAGENUMS;protected static final#int#PD_SELECTION;protected static final#int#PRINTER;protected static final#int#STREAM;protected#boolean#collateAttReq;protected#boolean#landscapeRotates270;protected#boolean#noJobSheet;protected#boolean#performingPrinting;protected#boolean#userCancelled;protected#int#mDestType;protected#java.awt.print.Pageable#mDocument;protected#java.lang.String#destinationAttr;protected#java.lang.String#mDestination;protected#javax.print.PrintService#myService;protected#javax.print.attribute.PrintRequestAttributeSet#attributes;protected#javax.print.attribute.standard.Sides#sidesAttr;public static#boolean#debugPrint;public static#boolean#forcePDL;public static#boolean#forceRaster;public static#boolean#shapeTextProp".equals(fields(c)), "fields");
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
