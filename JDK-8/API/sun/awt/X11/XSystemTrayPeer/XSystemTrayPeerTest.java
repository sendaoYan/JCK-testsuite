/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XSystemTrayPeer conformance: reflection (no init, native/display-heavy).
 * @run main XSystemTrayPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XSystemTrayPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XSystemTrayPeer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XSystemTrayPeer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.SystemTrayPeer;sun.awt.X11.XMSelectionListener".equals(interfaces(c)), "interfaces");
        check("#boolean#isAvailable();#void#addTrayIcon(sun.awt.X11.XTrayIconPeer);#void#dispose();#void#sendMessage(long,long,long,long,long);private#void#createTrayPeers();private#void#firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object);private#void#invokeOnEachAppContext(java.lang.Runnable);private#void#removeTrayPeers();public#java.awt.Dimension#getTrayIconSize();public#void#ownerChanged(int,sun.awt.X11.XMSelection,long,long,long);public#void#ownerDeath(int,sun.awt.X11.XMSelection,long);public#void#selectionChanged(int,sun.awt.X11.XMSelection,long,sun.awt.X11.XPropertyEvent);static#sun.awt.X11.XSystemTrayPeer#getPeerInstance()".equals(methods(c)), "methods");
        check("#java.awt.SystemTray#target;private final#sun.awt.X11.XMSelection#selection;private static final#int#SCREEN;private static final#java.lang.String#SYSTEM_TRAY_PROPERTY_NAME;private static final#long#SYSTEM_TRAY_REQUEST_DOCK;private static final#sun.awt.X11.XAtom#_NET_SYSTEM_TRAY;private static final#sun.awt.X11.XAtom#_NET_SYSTEM_TRAY_OPCODE;private static final#sun.awt.X11.XAtom#_NET_WM_ICON;private static final#sun.awt.X11.XAtom#_XEMBED_INFO;private static final#sun.util.logging.PlatformLogger#log;private volatile#boolean#available;static#sun.awt.X11.XSystemTrayPeer#peerInstance".equals(fields(c)), "fields");
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
