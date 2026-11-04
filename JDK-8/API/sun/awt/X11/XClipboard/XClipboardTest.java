/*
 * @test
 * @summary sun.awt.X11.XClipboard conformance: reflection (no init, native/display-heavy).
 * @run main XClipboardTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XClipboardTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XClipboard", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XClipboard".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.datatransfer.SunClipboard".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.X11.OwnershipListener".equals(interfaces(c)), "interfaces");
        check("private static#int#getPollInterval();private#sun.awt.X11.XAtom#getTargetsPropertyAtom();private#void#checkChange(sun.awt.X11.XSelectionEvent);private#void#checkChangeHere(java.awt.datatransfer.Transferable);private#void#getTargetsDelayed();protected synchronized#void#setContentsNative(java.awt.datatransfer.Transferable);protected#[B#getClipboardData(long);protected#[J#getClipboardFormats();protected#void#clearNativeContext();protected#void#registerClipboardViewerChecked();protected#void#unregisterClipboardViewerChecked();public synchronized#java.awt.datatransfer.Transferable#getContents(java.lang.Object);public#long#getID();public#void#ownershipChanged(boolean)".equals(methods(c)), "methods");
        check("private final#sun.awt.X11.XSelection#selection;private static final#int#defaultPollInterval;private static final#java.lang.Object#classLock;private static#int#pollInterval;private static#java.util.Map#targetsAtom2Clipboard;private volatile#boolean#isSelectionNotifyProcessed;private volatile#sun.awt.X11.XAtom#targetsPropertyAtom;private#long#convertSelectionTime".equals(fields(c)), "fields");
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
