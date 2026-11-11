/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XEmbedHelper conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XEmbedHelperTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XEmbedHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XEmbedHelper", false, XEmbedHelperTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XEmbedHelper".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#int#getModifiers(int);#java.awt.AWTKeyStroke#getKeyStrokeForKeySym(long,long);#void#sendMessage(long,int);#void#sendMessage(long,int,long,long,long);static#java.lang.String#XEmbedMessageToString(sun.awt.X11.XClientMessageEvent);static#java.lang.String#focusIdToString(int);static#java.lang.String#msgidToString(int)".equals(methods(c)), "methods");
        check("private static final#sun.util.logging.PlatformLogger#xembedLog;static final#int#NON_STANDARD_XEMBED_GTK_GRAB_KEY;static final#int#NON_STANDARD_XEMBED_GTK_UNGRAB_KEY;static final#int#XEMBED_ACTIVATE_ACCELERATOR;static final#int#XEMBED_EMBEDDED_NOTIFY;static final#int#XEMBED_FOCUS_CURRENT;static final#int#XEMBED_FOCUS_FIRST;static final#int#XEMBED_FOCUS_IN;static final#int#XEMBED_FOCUS_LAST;static final#int#XEMBED_FOCUS_NEXT;static final#int#XEMBED_FOCUS_OUT;static final#int#XEMBED_FOCUS_PREV;static final#int#XEMBED_GRAB_KEY;static final#int#XEMBED_MAPPED;static final#int#XEMBED_MODALITY_OFF;static final#int#XEMBED_MODALITY_ON;static final#int#XEMBED_MODIFIER_ALT;static final#int#XEMBED_MODIFIER_CONTROL;static final#int#XEMBED_MODIFIER_HYPER;static final#int#XEMBED_MODIFIER_SHIFT;static final#int#XEMBED_MODIFIER_SUPER;static final#int#XEMBED_REGISTER_ACCELERATOR;static final#int#XEMBED_REQUEST_FOCUS;static final#int#XEMBED_UNGRAB_KEY;static final#int#XEMBED_UNREGISTER_ACCELERATOR;static final#int#XEMBED_VERSION;static final#int#XEMBED_WINDOW_ACTIVATE;static final#int#XEMBED_WINDOW_DEACTIVATE;static final#jdk.internal.misc.Unsafe#unsafe;static#sun.awt.X11.XAtom#XEmbed;static#sun.awt.X11.XAtom#XEmbedInfo".equals(fields(c)), "fields");
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
