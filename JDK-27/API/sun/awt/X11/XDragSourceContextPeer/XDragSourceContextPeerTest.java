/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XDragSourceContextPeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XDragSourceContextPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XDragSourceContextPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XDragSourceContextPeer", false, XDragSourceContextPeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XDragSourceContextPeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.awt.dnd.SunDragSourceContextPeer".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.X11.XDragSourceProtocolListener".equals(interfaces(c)), "interfaces");
        check("private static#boolean#isXWaylandDndAwareWindow(long);private static#long#findClientWindow(long);private#boolean#doProcessEvent(sun.awt.X11.XEvent);private#boolean#processClientMessage(sun.awt.X11.XClientMessageEvent);private#boolean#processProxyModeEvent(sun.awt.X11.XEvent);private#boolean#updateSourceAction(int);private#void#cleanupTargetInfo();private#void#doUpdateTargetWindow(long,long);private#void#processDrop(sun.awt.X11.XButtonEvent);private#void#processMouseMove(sun.awt.X11.XMotionEvent);private#void#removeDnDGrab(long);private#void#setProxyModeSourceWindowImpl(long);private#void#throwGrabFailureException(java.lang.String,int);private#void#updateTargetWindow(sun.awt.X11.XMotionEvent);protected#boolean#needsBogusExitBeforeDrop();protected#void#setNativeCursor(long,java.awt.Cursor,int);protected#void#startDrag(java.awt.datatransfer.Transferable,[J,java.util.Map);public static#void#setProxyModeSourceWindow(long);public#int#scaleDown(int);public#int#scaleUp(int);public#long#getProxyModeSourceWindow();public#void#cleanup(long);public#void#handleDragFinished();public#void#handleDragFinished(boolean);public#void#handleDragFinished(boolean,int);public#void#handleDragFinished(boolean,int,int,int);public#void#handleDragReply(int);public#void#handleDragReply(int,int,int);public#void#handleDragReply(int,int,int,int);public#void#setCursor(java.awt.Cursor);static#boolean#processEvent(sun.awt.X11.XEvent);static#sun.awt.X11.XDragSourceContextPeer#createDragSourceContextPeer(java.awt.dnd.DragGestureEvent);static#sun.awt.X11.XDragSourceProtocolListener#getXDragSourceProtocolListener()".equals(methods(c)), "methods");
        check("#int#windowScale;private static final#int#GRAB_EVENT_MASK;private static final#int#ROOT_EVENT_MASK;private static final#sun.awt.X11.XDragSourceContextPeer#theInstance;private static final#sun.util.logging.PlatformLogger#logger;private#[J#sourceFormats;private#boolean#dndInProgress;private#boolean#dragInProgress;private#int#eventState;private#int#sourceAction;private#int#sourceActions;private#int#targetAction;private#int#xRoot;private#int#yRoot;private#long#dragRootWindow;private#long#proxyModeSourceWindow;private#long#rootEventMask;private#long#targetRootSubwindow;private#sun.awt.X11.XDragSourceProtocol#dragProtocol".equals(fields(c)), "fields");
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
