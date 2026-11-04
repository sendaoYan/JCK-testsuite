/*
 * @test
 * @summary sun.swing.SwingUtilities2 conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing:+open
 * @run main SwingUtilities2Test
 */

import java.util.*;
import java.lang.reflect.*;

public class SwingUtilities2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.SwingUtilities2", false, SwingUtilities2Test.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.SwingUtilities2".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#[B#getIconBytes(java.lang.Class,java.lang.Class,java.lang.String);private static#boolean#isFontRenderContextPrintCompatible(java.awt.font.FontRenderContext,java.awt.font.FontRenderContext);private static#boolean#pointIsInActualBounds(javax.swing.JList,int,java.awt.Point);private static#float#drawStringImpl(javax.swing.JComponent,java.awt.Graphics,java.text.AttributedCharacterIterator,float,float);private static#int#syncCharsBuffer(java.lang.String);private static#java.awt.font.FontRenderContext#getFRCFromCache(java.awt.geom.AffineTransform,java.lang.Object);private static#java.awt.font.FontRenderContext#getFRCProperty(javax.swing.JComponent);private static#java.awt.font.FontRenderContext#getFontRenderContext(java.awt.Component,java.awt.FontMetrics);private static#java.awt.font.TextLayout#createTextLayout(javax.swing.JComponent,java.lang.String,java.awt.Font,java.awt.font.FontRenderContext);private static#java.text.AttributedCharacterIterator#getTrimmedTrailingSpacesIterator(java.text.AttributedCharacterIterator);private static#sun.swing.SwingUtilities2$Section#liesIn(java.awt.Rectangle,java.awt.Point,boolean,boolean,boolean);private static#void#execute(java.lang.Runnable);public static final#boolean#isComplexLayout([C,int,int);public static#boolean#getBoolean(javax.swing.JComponent,java.lang.String);public static#boolean#isFloatingPointScale(java.awt.geom.AffineTransform);public static#boolean#isLocalDisplay();public static#boolean#isPrinting(java.awt.Graphics);public static#boolean#isScaleChanged(java.beans.PropertyChangeEvent);public static#boolean#isScaleChanged(java.lang.String,java.lang.Object,java.lang.Object);public static#boolean#isScaledGraphics(java.awt.Graphics);public static#boolean#pointOutsidePrefSize(javax.swing.JTable,int,int,java.awt.Point);public static#boolean#shouldIgnore(java.awt.event.MouseEvent,javax.swing.JComponent);public static#boolean#tabbedPaneChangeFocusTo(java.awt.Component);public static#boolean#useSelectedTextColor(javax.swing.text.Highlighter$Highlight,javax.swing.text.JTextComponent);public static#float#drawChars(javax.swing.JComponent,java.awt.Graphics,[C,int,int,float,float);public static#float#drawChars(javax.swing.JComponent,java.awt.Graphics,[C,int,int,float,float,boolean);public static#float#drawString(javax.swing.JComponent,java.awt.Graphics,java.text.AttributedCharacterIterator,float,float);public static#float#drawString(javax.swing.JComponent,java.awt.Graphics,java.text.AttributedCharacterIterator,int,int);public static#float#getFontCharWidth(char,java.awt.FontMetrics,boolean);public static#float#getFontCharsWidth([C,int,int,java.awt.FontMetrics,boolean);public static#float#getFontStringWidth(java.lang.String,java.awt.FontMetrics,boolean);public static#float#stringWidth(javax.swing.JComponent,java.awt.FontMetrics,java.lang.String,boolean);public static#int#convertColumnIndexToModel(javax.swing.table.TableColumnModel,int);public static#int#convertColumnIndexToView(javax.swing.table.TableColumnModel,int);public static#int#drawChars(javax.swing.JComponent,java.awt.Graphics,[C,int,int,int,int);public static#int#getAdjustedClickCount(javax.swing.text.JTextComponent,java.awt.event.MouseEvent);public static#int#getLeftSideBearing(javax.swing.JComponent,java.awt.FontMetrics,char);public static#int#getLeftSideBearing(javax.swing.JComponent,java.awt.FontMetrics,java.lang.String);public static#int#getSystemMnemonicKeyMask();public static#int#getUIDefaultsInt(java.lang.Object);public static#int#getUIDefaultsInt(java.lang.Object,int);public static#int#getUIDefaultsInt(java.lang.Object,java.util.Locale);public static#int#getUIDefaultsInt(java.lang.Object,java.util.Locale,int);public static#int#loc2IndexFileList(javax.swing.JList,java.awt.Point);public static#int#setAltGraphMask(int);public static#int#stringWidth(javax.swing.JComponent,java.awt.FontMetrics,java.lang.String);public static#java.awt.Component#compositeRequestFocus(java.awt.Component);public static#java.awt.FontMetrics#getFontMetrics(javax.swing.JComponent,java.awt.Font);public static#java.awt.FontMetrics#getFontMetrics(javax.swing.JComponent,java.awt.Graphics);public static#java.awt.FontMetrics#getFontMetrics(javax.swing.JComponent,java.awt.Graphics,java.awt.Font);public static#java.awt.Graphics2D#getGraphics2D(java.awt.Graphics);public static#java.awt.font.FontRenderContext#getFontRenderContext(java.awt.Component);public static#java.lang.Object#getAndSetAntialisingHintForScaledGraphics(java.awt.Graphics);public static#java.lang.Object#makeIcon(java.lang.Class,java.lang.Class,java.lang.String);public static#java.lang.String#clipString(javax.swing.JComponent,java.awt.FontMetrics,java.lang.String,int);public static#java.lang.String#clipStringIfNecessary(javax.swing.JComponent,java.awt.FontMetrics,java.lang.String,int);public static#java.lang.String#displayPropertiesToCSS(java.awt.Font,java.awt.Color);public static#java.util.concurrent.Future#submit(java.lang.Runnable,java.lang.Object);public static#java.util.concurrent.Future#submit(java.util.concurrent.Callable);public static#javax.swing.tree.TreePath#getTreePath(javax.swing.event.TreeModelEvent,javax.swing.tree.TreeModel);public static#sun.swing.SwingUtilities2$Section#liesInHorizontal(java.awt.Rectangle,java.awt.Point,boolean,boolean);public static#sun.swing.SwingUtilities2$Section#liesInVertical(java.awt.Rectangle,java.awt.Point,boolean);public static#void#adjustFocus(javax.swing.JComponent);public static#void#drawHLine(java.awt.Graphics,int,int,int);public static#void#drawRect(java.awt.Graphics,int,int,int,int);public static#void#drawString(javax.swing.JComponent,java.awt.Graphics,java.lang.String,float,float,boolean);public static#void#drawString(javax.swing.JComponent,java.awt.Graphics,java.lang.String,int,int);public static#void#drawStringUnderlineCharAt(javax.swing.JComponent,java.awt.Graphics,java.lang.String,int,float,float,boolean);public static#void#drawStringUnderlineCharAt(javax.swing.JComponent,java.awt.Graphics,java.lang.String,int,int,int);public static#void#drawVLine(java.awt.Graphics,int,int,int);public static#void#putAATextInfo(boolean,java.util.Map);public static#void#setAntialiasingHintForScaledGraphics(java.awt.Graphics,java.lang.Object);public static#void#setLeadAnchorWithoutSelection(javax.swing.ListSelectionModel,int,int);public static#void#setSkipClickCount(java.awt.Component,int)".equals(methods(c)), "methods");
        check("private static final#int#CACHE_SIZE;private static final#int#CHAR_BUFFER_SIZE;private static final#int#MAX_CHAR_INDEX;private static final#int#MIN_CHAR_INDEX;private static final#java.lang.Object#charsBufferLock;private static final#java.lang.StringBuilder#SKIP_CLICK_COUNT;private static final#java.util.Map#cache;private static#[C#charsBuffer;private static#[Lsun.swing.SwingUtilities2$LSBCacheEntry;#fontCache;private static#int#nextIndex;private static#sun.swing.SwingUtilities2$LSBCacheEntry#searchKey;public static final#java.awt.font.FontRenderContext#DEFAULT_FRC;public static final#java.lang.String#IMPLIED_CR;public static final#sun.swing.StringUIClientPropertyKey#BASICMENUITEMUI_MAX_TEXT_OFFSET".equals(fields(c)), "fields");
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
