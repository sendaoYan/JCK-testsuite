/*
 * @test
 * @summary sun.awt.KeyboardFocusManagerPeerImpl conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main KeyboardFocusManagerPeerImplTest
 */

import java.util.*;
import java.lang.reflect.*;

public class KeyboardFocusManagerPeerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.KeyboardFocusManagerPeerImpl", false, KeyboardFocusManagerPeerImplTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.KeyboardFocusManagerPeerImpl".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.KeyboardFocusManagerPeer".equals(interfaces(c)), "interfaces");
        check("public static#boolean#deliverFocus(java.awt.Component,java.awt.Component,boolean,boolean,long,java.awt.event.FocusEvent$Cause,java.awt.Component);public static#boolean#processSynchronousLightweightTransfer(java.awt.Component,java.awt.Component,boolean,boolean,long);public static#boolean#shouldFocusOnClick(java.awt.Component);public static#int#shouldNativelyFocusHeavyweight(java.awt.Component,java.awt.Component,boolean,boolean,long,java.awt.event.FocusEvent$Cause);public static#void#removeLastFocusRequest(java.awt.Component);public static#void#requestFocusFor(java.awt.Component,java.awt.event.FocusEvent$Cause);public#void#clearGlobalFocusOwner(java.awt.Window)".equals(methods(c)), "methods");
        check("private static final#sun.util.logging.PlatformLogger#focusLog;public static final#int#SNFH_FAILURE;public static final#int#SNFH_SUCCESS_HANDLED;public static final#int#SNFH_SUCCESS_PROCEED".equals(fields(c)), "fields");
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
