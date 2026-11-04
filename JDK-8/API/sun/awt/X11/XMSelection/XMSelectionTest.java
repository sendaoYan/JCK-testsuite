/*
 * @test
 * @summary sun.awt.X11.XMSelection conformance: reflection (no init, native/display-heavy).
 * @run main XMSelectionTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XMSelectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XMSelection", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XMSelection".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#dispatchSelectionEvent(sun.awt.X11.XEvent,int);#void#resetOwner(long,int);#void#select(long);#void#selectPerScreen(int,long);public synchronized#void#addSelectionListener(sun.awt.X11.XMSelectionListener);public synchronized#void#removeSelectionListener(sun.awt.X11.XMSelectionListener);public#int#getNumberOfScreens();static#boolean#processClientMessage(sun.awt.X11.XEvent,int);static#boolean#processRootEvent(sun.awt.X11.XEvent,int);static#sun.awt.X11.XMSelection#getInstance(long);static#void#initScreen(long,int);synchronized#java.lang.String#getName();synchronized#java.util.Collection#getListeners();synchronized#long#getOwner(int);synchronized#sun.awt.X11.XAtom#getAtom(int);synchronized#void#dispatchOwnerChangedEvent(sun.awt.X11.XEvent,int,long,long,long);synchronized#void#dispatchOwnerDeath(sun.awt.X11.XDestroyWindowEvent,int);synchronized#void#dispatchSelectionChanged(sun.awt.X11.XPropertyEvent,int);synchronized#void#setAtom(sun.awt.X11.XAtom,int);synchronized#void#setOwner(long,int)".equals(methods(c)), "methods");
        check("#[J#owners;#[Lsun.awt.X11.XAtom;#atoms;#java.lang.String#selectionName;#java.util.Vector#listeners;#long#eventMask;private static#sun.util.logging.PlatformLogger#log;static#int#numScreens;static#java.util.HashMap#selectionMap;static#sun.awt.X11.XAtom#XA_MANAGER".equals(fields(c)), "fields");
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
