/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XEmbedServerTester conformance: reflection (no init, native/display-heavy).
 * @run main XEmbedServerTesterTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XEmbedServerTesterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XEmbedServerTester", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XEmbedServerTester".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.X11.XEventDispatcher".equals(interfaces(c)), "interfaces");
        check("private#int#activateServer(int);private#int#activateServerNoWait(int);private#int#checkEventList(int,int);private#int#deactivateServer();private#int#embed();private#int#embedCompletely();private#int#focusServer();private#int#focusServerNext();private#int#focusServerPrev();private#int#getEventPos();private#int#hideModalDialog();private#int#pressAccelKey();private#int#requestFocus();private#int#requestFocusNoWait();private#int#showModalDialog();private#void#checkFocusGained(int);private#void#checkFocusLost();private#void#checkFocused();private#void#checkFocusedServerNext();private#void#checkFocusedServerPrev();private#void#checkMapped();private#void#checkNotFocused();private#void#checkNotMapped();private#void#checkWindowActivated();private#void#dumpReceivedEvents();private#void#grabKey();private#void#initAccel();private#void#registerAccelerator();private#void#sendMessage(int);private#void#sendMessage(int,int,long,long);private#void#sleep(int);private#void#ungrabKey();private#void#unregisterAccelerator();private#void#updateEmbedInfo();private#void#waitEmbeddedNotify(int);private#void#waitFocusGained(int);private#void#waitFocusLost(int);private#void#waitForEvent(int,int);private#void#waitSystemEvent(int,int);private#void#waitWindowActivated(int);private#void#waitWindowDeactivated(int);public static#sun.awt.X11.XEmbedServerTester#getTester([Ljava.awt.Rectangle;,long);public#void#dispatchEvent(sun.awt.X11.XEvent);public#void#test1_1();public#void#test1_2();public#void#test1_3();public#void#test1_4();public#void#test1_5();public#void#test1_6();public#void#test1_7();public#void#test2_5();public#void#test2_6();public#void#test3_1();public#void#test3_3();public#void#test3_4();public#void#test3_5();public#void#test3_6();public#void#test4_1();public#void#test4_2();public#void#test4_3();public#void#test4_4();public#void#test6_1_1();public#void#test6_1_2();public#void#test6_1_3();public#void#test6_1_4();public#void#test6_2_1();public#void#test6_2_2();public#void#test6_2_3();public#void#test6_2_4();public#void#test7_1();public#void#test7_2();public#void#test9_1()".equals(methods(c)), "methods");
        check("#[Ljava.awt.Rectangle;#serverBounds;#boolean#focused;#boolean#reparent;#boolean#windowActive;#boolean#xembedActive;#int#accel_key;#int#accel_keysym;#int#accel_mods;#int#focusedKind;#int#focusedServerComponent;#int#mapped;#int#my_version;#int#server_version;#java.awt.Robot#robot;#java.util.LinkedList#events;#long#parent;#sun.awt.X11.XBaseWindow#window;#sun.awt.X11.XEmbedHelper#xembed;private final#java.lang.Object#EVENT_LOCK;private static final#int#MODAL_CLOSE;private static final#int#OTHER_FRAME;private static final#int#SERVER_BOUNDS;private static final#int#SERVER_FOCUS;private static final#int#SERVER_MODAL;private static final#sun.util.logging.PlatformLogger#xembedLog;static final#int#SYSTEM_EVENT_MASK;static#java.awt.Rectangle#initialBounds;volatile#int#eventReceived;volatile#int#eventWaited".equals(fields(c)), "fields");
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
