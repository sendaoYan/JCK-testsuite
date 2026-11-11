/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XTrayIconPeer conformance: reflection (no init, native/display-heavy).
 * @run main XTrayIconPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XTrayIconPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XTrayIconPeer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XTrayIconPeer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.TrayIconPeer;sun.awt.X11.InfoWindow$Balloon$LiveArguments;sun.awt.X11.InfoWindow$Tooltip$LiveArguments".equals(interfaces(c)), "interfaces");
        check("#long#getWindow();#void#addListeners();private#java.awt.Point#getLocationOnScreen();private#void#addXED(long,sun.awt.X11.XEventDispatcher,long);private#void#disposeOnEDT();private#void#removeXED(long,sun.awt.X11.XEventDispatcher);public static#void#suppressWarningString(java.awt.Window);public#boolean#isDisposed();public#java.awt.Rectangle#getBounds();public#java.lang.String#getActionCommand();public#java.lang.String#getTooltipString();public#void#displayMessage(java.lang.String,java.lang.String,java.lang.String);public#void#dispose();public#void#setToolTip(java.lang.String);public#void#showPopupMenu(int,int);public#void#updateImage()".equals(methods(c)), "methods");
        check("#boolean#isParentWindowLocated;#boolean#isTrayIconDisplayed;#int#ex_height;#int#ex_width;#int#old_x;#int#old_y;#java.awt.PopupMenu#popup;#java.awt.TrayIcon#target;#java.lang.String#tooltipString;#long#eframeParentID;#sun.awt.X11.InfoWindow$Balloon#balloon;#sun.awt.X11.InfoWindow$Tooltip#tooltip;#sun.awt.X11.XTrayIconPeer$TrayIconCanvas#canvas;#sun.awt.X11.XTrayIconPeer$TrayIconEventProxy#eventProxy;#sun.awt.X11.XTrayIconPeer$XTrayIconEmbeddedFrame#eframe;final#sun.awt.X11.XEventDispatcher#eframeXED;final#sun.awt.X11.XEventDispatcher#parentXED;private static final#sun.util.logging.PlatformLogger#ctrLog;static final#int#TRAY_ICON_HEIGHT;static final#int#TRAY_ICON_WIDTH;static final#sun.awt.X11.XEventDispatcher#dummyXED;volatile#boolean#isDisposed".equals(fields(c)), "fields");
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
