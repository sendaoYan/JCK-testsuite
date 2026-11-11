/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XEvent conformance: reflection (no init, native/display-heavy).
 * @run main XEventTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XEvent", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XEvent".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#int#getDataSize();public#int#get_type();public#long#getPData();public#long#get_pad();public#long#get_pad(int);public#sun.awt.X11.XAnyEvent#get_xany();public#sun.awt.X11.XButtonEvent#get_xbutton();public#sun.awt.X11.XCirculateEvent#get_xcirculate();public#sun.awt.X11.XCirculateRequestEvent#get_xcirculaterequest();public#sun.awt.X11.XClientMessageEvent#get_xclient();public#sun.awt.X11.XColormapEvent#get_xcolormap();public#sun.awt.X11.XConfigureEvent#get_xconfigure();public#sun.awt.X11.XConfigureRequestEvent#get_xconfigurerequest();public#sun.awt.X11.XCreateWindowEvent#get_xcreatewindow();public#sun.awt.X11.XCrossingEvent#get_xcrossing();public#sun.awt.X11.XDestroyWindowEvent#get_xdestroywindow();public#sun.awt.X11.XErrorEvent#get_xerror();public#sun.awt.X11.XExposeEvent#get_xexpose();public#sun.awt.X11.XFocusChangeEvent#get_xfocus();public#sun.awt.X11.XGraphicsExposeEvent#get_xgraphicsexpose();public#sun.awt.X11.XGravityEvent#get_xgravity();public#sun.awt.X11.XKeyEvent#get_xkey();public#sun.awt.X11.XKeymapEvent#get_xkeymap();public#sun.awt.X11.XMapEvent#get_xmap();public#sun.awt.X11.XMapRequestEvent#get_xmaprequest();public#sun.awt.X11.XMappingEvent#get_xmapping();public#sun.awt.X11.XMotionEvent#get_xmotion();public#sun.awt.X11.XNoExposeEvent#get_xnoexpose();public#sun.awt.X11.XPropertyEvent#get_xproperty();public#sun.awt.X11.XReparentEvent#get_xreparent();public#sun.awt.X11.XResizeRequestEvent#get_xresizerequest();public#sun.awt.X11.XSelectionClearEvent#get_xselectionclear();public#sun.awt.X11.XSelectionEvent#get_xselection();public#sun.awt.X11.XSelectionRequestEvent#get_xselectionrequest();public#sun.awt.X11.XUnmapEvent#get_xunmap();public#sun.awt.X11.XVisibilityEvent#get_xvisibility();public#void#dispose();public#void#set_pad(int,long);public#void#set_type(int)".equals(methods(c)), "methods");
        check("#long#pData;private final#boolean#should_free_memory;private#sun.misc.Unsafe#unsafe".equals(fields(c)), "fields");
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
