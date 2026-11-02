/*
 * @test
 * @summary sun.swing.MenuItemLayoutHelper conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing:+open
 * @run main MenuItemLayoutHelperTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MenuItemLayoutHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.MenuItemLayoutHelper", false, MenuItemLayoutHelperTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.MenuItemLayoutHelper".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private transient#void#calcXPositionsLTR(int,int,int,[Ljava.awt.Rectangle;);private transient#void#calcXPositionsRTL(int,int,int,[Ljava.awt.Rectangle;);private#int#getAfterCheckIconGap(java.lang.String);private#int#getCheckOffset(java.lang.String);private#int#getLeadingGap(java.lang.String);private#int#getLeftExtraWidth(java.lang.String);private#int#getMinTextOffset(java.lang.String);private#java.lang.String#getAccText(java.lang.String);private#javax.swing.Icon#getIcon(java.lang.String);private#sun.swing.MenuItemLayoutHelper$LayoutResult#createLayoutResult();private#void#alignAccCheckAndArrowVertically(sun.swing.MenuItemLayoutHelper$LayoutResult);private#void#alignRect(java.awt.Rectangle,int,int);private#void#alignRects(sun.swing.MenuItemLayoutHelper$LayoutResult,sun.swing.MenuItemLayoutHelper$ColumnAlignment);private#void#calcExtraWidths();private#void#calcLabelYPosition(sun.swing.MenuItemLayoutHelper$LayoutResult);private#void#calcMaxTextOffset(java.awt.Rectangle);private#void#calcTextAndIconYPositions(sun.swing.MenuItemLayoutHelper$LayoutResult);private#void#doLTRColumnLayout(sun.swing.MenuItemLayoutHelper$LayoutResult,sun.swing.MenuItemLayoutHelper$ColumnAlignment);private#void#doLTRComplexLayout(sun.swing.MenuItemLayoutHelper$LayoutResult,sun.swing.MenuItemLayoutHelper$ColumnAlignment);private#void#doRTLColumnLayout(sun.swing.MenuItemLayoutHelper$LayoutResult,sun.swing.MenuItemLayoutHelper$ColumnAlignment);private#void#doRTLComplexLayout(sun.swing.MenuItemLayoutHelper$LayoutResult,sun.swing.MenuItemLayoutHelper$ColumnAlignment);private#void#fixVerticalAlignment(sun.swing.MenuItemLayoutHelper$LayoutResult,java.awt.Rectangle);private#void#setOriginalWidths();protected#int#calcMaxValue(java.lang.Object,int);protected#int#getParentIntProperty(java.lang.Object);protected#void#calcMaxWidth(sun.swing.MenuItemLayoutHelper$RectSize,java.lang.Object);protected#void#calcMaxWidths();protected#void#calcWidthsAndHeights();protected#void#layoutIconAndTextInLabelRect(sun.swing.MenuItemLayoutHelper$LayoutResult);protected#void#prepareForLayout(sun.swing.MenuItemLayoutHelper$LayoutResult);protected#void#reset(javax.swing.JMenuItem,javax.swing.Icon,javax.swing.Icon,java.awt.Rectangle,int,java.lang.String,boolean,java.awt.Font,java.awt.Font,boolean,java.lang.String);protected#void#setAccFont(java.awt.Font);protected#void#setAccFontMetrics(java.awt.FontMetrics);protected#void#setAccSize(sun.swing.MenuItemLayoutHelper$RectSize);protected#void#setAccText(java.lang.String);protected#void#setAfterCheckIconGap(int);protected#void#setArrowIcon(javax.swing.Icon);protected#void#setArrowSize(sun.swing.MenuItemLayoutHelper$RectSize);protected#void#setCheckIcon(javax.swing.Icon);protected#void#setCheckSize(sun.swing.MenuItemLayoutHelper$RectSize);protected#void#setColumnLayout(boolean);protected#void#setFont(java.awt.Font);protected#void#setFontMetrics(java.awt.FontMetrics);protected#void#setGap(int);protected#void#setHorizontalAlignment(int);protected#void#setHorizontalTextPosition(int);protected#void#setHtmlView(javax.swing.text.View);protected#void#setIcon(javax.swing.Icon);protected#void#setIconSize(sun.swing.MenuItemLayoutHelper$RectSize);protected#void#setLabelSize(sun.swing.MenuItemLayoutHelper$RectSize);protected#void#setLeadingGap(int);protected#void#setLeftToRight(boolean);protected#void#setMenuItem(javax.swing.JMenuItem);protected#void#setMenuItemParent(javax.swing.JComponent);protected#void#setMinTextOffset(int);protected#void#setText(java.lang.String);protected#void#setTextSize(sun.swing.MenuItemLayoutHelper$RectSize);protected#void#setTopLevelMenu(boolean);protected#void#setUseCheckAndArrow(boolean);protected#void#setVerticalAlignment(int);protected#void#setVerticalTextPosition(int);protected#void#setViewRect(java.awt.Rectangle);public static transient#int#max([I);public static#boolean#isColumnLayout(boolean,int,int,int);public static#boolean#isColumnLayout(boolean,javax.swing.JMenuItem);public static#boolean#useCheckAndArrow(javax.swing.JMenuItem);public static#java.awt.Rectangle#createMaxRect();public static#javax.swing.JComponent#getMenuItemParent(javax.swing.JMenuItem);public static#void#addMaxWidth(sun.swing.MenuItemLayoutHelper$RectSize,int,java.awt.Dimension);public static#void#addWidth(int,int,java.awt.Dimension);public static#void#clearUsedClientProperties(javax.swing.JComponent);public static#void#clearUsedParentClientProperties(javax.swing.JMenuItem);public#boolean#isColumnLayout();public#boolean#isLeftToRight();public#boolean#isTopLevelMenu();public#boolean#useCheckAndArrow();public#int#getAfterCheckIconGap();public#int#getGap();public#int#getHorizontalAlignment();public#int#getHorizontalTextPosition();public#int#getLeadingGap();public#int#getLeftTextExtraWidth();public#int#getMinTextOffset();public#int#getVerticalAlignment();public#int#getVerticalTextPosition();public#java.awt.Font#getAccFont();public#java.awt.Font#getFont();public#java.awt.FontMetrics#getAccFontMetrics();public#java.awt.FontMetrics#getFontMetrics();public#java.awt.Rectangle#getViewRect();public#java.lang.String#getAccText();public#java.lang.String#getText();public#javax.swing.Icon#getArrowIcon();public#javax.swing.Icon#getCheckIcon();public#javax.swing.Icon#getIcon();public#javax.swing.JComponent#getMenuItemParent();public#javax.swing.JMenuItem#getMenuItem();public#javax.swing.text.View#getHtmlView();public#sun.swing.MenuItemLayoutHelper$ColumnAlignment#getLTRColumnAlignment();public#sun.swing.MenuItemLayoutHelper$ColumnAlignment#getRTLColumnAlignment();public#sun.swing.MenuItemLayoutHelper$LayoutResult#layoutMenuItem();public#sun.swing.MenuItemLayoutHelper$RectSize#getAccSize();public#sun.swing.MenuItemLayoutHelper$RectSize#getArrowSize();public#sun.swing.MenuItemLayoutHelper$RectSize#getCheckSize();public#sun.swing.MenuItemLayoutHelper$RectSize#getIconSize();public#sun.swing.MenuItemLayoutHelper$RectSize#getLabelSize();public#sun.swing.MenuItemLayoutHelper$RectSize#getTextSize()".equals(methods(c)), "methods");
        check("private#boolean#isColumnLayout;private#boolean#isLeftToRight;private#boolean#isTopLevelMenu;private#boolean#useCheckAndArrow;private#int#afterCheckIconGap;private#int#gap;private#int#horizontalAlignment;private#int#horizontalTextPosition;private#int#leadingGap;private#int#leftTextExtraWidth;private#int#minTextOffset;private#int#verticalAlignment;private#int#verticalTextPosition;private#java.awt.Font#accFont;private#java.awt.Font#font;private#java.awt.FontMetrics#accFm;private#java.awt.FontMetrics#fm;private#java.awt.Rectangle#viewRect;private#java.lang.String#accText;private#java.lang.String#text;private#javax.swing.Icon#arrowIcon;private#javax.swing.Icon#checkIcon;private#javax.swing.Icon#icon;private#javax.swing.JComponent#miParent;private#javax.swing.JMenuItem#mi;private#javax.swing.text.View#htmlView;private#sun.swing.MenuItemLayoutHelper$RectSize#accSize;private#sun.swing.MenuItemLayoutHelper$RectSize#arrowSize;private#sun.swing.MenuItemLayoutHelper$RectSize#checkSize;private#sun.swing.MenuItemLayoutHelper$RectSize#iconSize;private#sun.swing.MenuItemLayoutHelper$RectSize#labelSize;private#sun.swing.MenuItemLayoutHelper$RectSize#textSize;public static final#sun.swing.StringUIClientPropertyKey#MAX_ACC_WIDTH;public static final#sun.swing.StringUIClientPropertyKey#MAX_ARROW_WIDTH;public static final#sun.swing.StringUIClientPropertyKey#MAX_CHECK_WIDTH;public static final#sun.swing.StringUIClientPropertyKey#MAX_ICON_WIDTH;public static final#sun.swing.StringUIClientPropertyKey#MAX_LABEL_WIDTH;public static final#sun.swing.StringUIClientPropertyKey#MAX_TEXT_WIDTH".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
