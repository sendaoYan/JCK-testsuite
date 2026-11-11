/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XBaseWindow conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XBaseWindowTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XBaseWindowTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XBaseWindow", false, XBaseWindowTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XBaseWindow".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#isDisposed();#boolean#isMapped();#boolean#isVisible();#int#getHeight();#int#getWidth();#int#getX();#int#getY();#java.awt.Point#toGlobal(int,int);#java.awt.Point#toGlobal(java.awt.Point);#java.awt.Point#toLocal(int,int);#java.awt.Point#toLocal(java.awt.Point);#java.awt.Rectangle#toGlobal(java.awt.Rectangle);#java.lang.Object#getStateLock();#long#getScreen();#long#getScreenNumber();#void#awtLock();#void#awtLockNotifyAll();#void#awtLockWait();#void#awtUnlock();#void#destroy();#void#flush();#void#instantPreInit(sun.awt.X11.XCreateWindowParams);#void#postInit(sun.awt.X11.XCreateWindowParams);#void#preInit(sun.awt.X11.XCreateWindowParams);#void#setDisposed(boolean);#void#setWMClass([Ljava.lang.String;);#void#ungrabInputImpl();#void#updateWMName();private#void#create(sun.awt.X11.XCreateWindowParams);protected final#void#init(long,java.awt.Rectangle);protected final#void#init(sun.awt.X11.XCreateWindowParams);protected final#void#postInit();protected final#void#preInit();protected#boolean#isEventDisabled(sun.awt.X11.XEvent);protected#int#getScale();protected#int#scaleDown(int);protected#int#scaleUp(int);protected#java.lang.String#getWMName();protected#void#checkParams(sun.awt.X11.XCreateWindowParams);protected#void#initClientLeader();public final#void#setWMHints(sun.awt.X11.XWMHints);public static#long#xGetInputFocus();public static#void#ungrabInput();public#boolean#checkInitialised();public#boolean#contains(int,int);public#boolean#containsGlobal(int,int);public#boolean#grabInput();public#boolean#isMinSizeSet();public#int#getAbsoluteX();public#int#getAbsoluteY();public#java.awt.Dimension#getSize();public#java.awt.Rectangle#getBounds();public#java.lang.String#toString();public#java.util.Set#getChildren();public#long#getContentWindow();public#long#getWindow();public#sun.awt.X11.XBaseWindow#getContentXWindow();public#sun.awt.X11.XBaseWindow#getParentWindow();public#sun.awt.X11.XCreateWindowParams#getDelayedParams();public#sun.awt.X11.XSizeHints#getHints();public#sun.awt.X11.XWMHints#getWMHints();public#sun.awt.X11.XWindowPeer#getToplevelXWindow();public#void#dispatchEvent(sun.awt.X11.XEvent);public#void#handleButtonPressRelease(sun.awt.X11.XEvent);public#void#handleClientMessage(sun.awt.X11.XEvent);public#void#handleConfigureNotifyEvent(sun.awt.X11.XEvent);public#void#handleCreateNotify(sun.awt.X11.XEvent);public#void#handleDestroyNotify(sun.awt.X11.XEvent);public#void#handleExposeEvent(sun.awt.X11.XEvent);public#void#handleKeyPress(sun.awt.X11.XEvent);public#void#handleKeyRelease(sun.awt.X11.XEvent);public#void#handleMapNotifyEvent(sun.awt.X11.XEvent);public#void#handleMotionNotify(sun.awt.X11.XEvent);public#void#handlePropertyNotify(sun.awt.X11.XEvent);public#void#handleReparentNotifyEvent(sun.awt.X11.XEvent);public#void#handleUnmapNotifyEvent(sun.awt.X11.XEvent);public#void#handleVisibilityEvent(sun.awt.X11.XEvent);public#void#handleXCrossingEvent(sun.awt.X11.XEvent);public#void#setSizeHints(long,int,int,int,int);public#void#toFront();public#void#xRequestFocus();public#void#xRequestFocus(long);public#void#xSetBounds(int,int,int,int);public#void#xSetBounds(java.awt.Rectangle);public#void#xSetVisible(boolean);static#boolean#isFullRelease(int,int);static#boolean#isGrabbedEvent(sun.awt.X11.XEvent,sun.awt.X11.XBaseWindow);static#java.awt.Point#toOtherWindow(long,long,int,int);static#long#getScreenOfWindow(long);static#sun.awt.X11.XRootWindow#getXAWTRootWindow();static#void#dispatchToWindow(sun.awt.X11.XEvent)".equals(methods(c)), "methods");
        check("#boolean#embedded;#boolean#mapped;#boolean#visible;#int#height;#int#width;#int#x;#int#y;#java.awt.Rectangle#maxBounds;#java.util.Set#children;#long#window;private static final#sun.util.logging.PlatformLogger#eventLog;private static final#sun.util.logging.PlatformLogger#focusLog;private static final#sun.util.logging.PlatformLogger#grabLog;private static final#sun.util.logging.PlatformLogger#insLog;private static final#sun.util.logging.PlatformLogger#log;private static#sun.awt.X11.XAtom#wm_client_leader;private#boolean#disposed;private#long#screen;private#sun.awt.X11.XBaseWindow$InitialiseState#initialising;private#sun.awt.X11.XCreateWindowParams#delayedParams;private#sun.awt.X11.XSizeHints#hints;private#sun.awt.X11.XWMHints#wmHints;protected#sun.awt.X11.XBaseWindow$StateLock#state_lock;public static final#java.lang.String#BACKGROUND_PIXMAP;public static final#java.lang.String#BACKING_STORE;public static final#java.lang.String#BIT_GRAVITY;public static final#java.lang.String#BORDER_PIXEL;public static final#java.lang.String#BOUNDS;public static final#java.lang.String#COLORMAP;public static final#java.lang.String#DELAYED;public static final#java.lang.String#DEPTH;public static final#java.lang.String#EMBEDDED;public static final#java.lang.String#EVENT_MASK;public static final#java.lang.String#OVERRIDE_REDIRECT;public static final#java.lang.String#PARENT;public static final#java.lang.String#PARENT_WINDOW;public static final#java.lang.String#SAVE_UNDER;public static final#java.lang.String#VALUE_MASK;public static final#java.lang.String#VISIBLE;public static final#java.lang.String#VISUAL;public static final#java.lang.String#VISUAL_CLASS;static final#int#DEF_LOCATION;static final#int#MIN_SIZE;volatile#sun.awt.X11.XBaseWindow#parentWindow".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 5, "ctors");
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
