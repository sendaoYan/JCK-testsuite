/*
 * @test
 * @summary sun.awt.AWTAutoShutdown conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main AWTAutoShutdownTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AWTAutoShutdownTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.AWTAutoShutdown", false, AWTAutoShutdownTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.AWTAutoShutdown".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Runnable".equals(interfaces(c)), "interfaces");
        check("#java.lang.Object#getPeer(java.lang.Object);#void#dumpPeers(sun.util.logging.PlatformLogger);#void#notifyPeerMapUpdated();#void#registerPeer(java.lang.Object,java.lang.Object);#void#unregisterPeer(java.lang.Object,java.lang.Object);private#boolean#isReadyToShutdown();private#void#activateBlockerThread();private#void#setToolkitBusy(boolean);public static#sun.awt.AWTAutoShutdown#getInstance();public static#void#notifyToolkitThreadBusy();public static#void#notifyToolkitThreadFree();public#void#notifyThreadBusy(java.lang.Thread);public#void#notifyThreadFree(java.lang.Thread);public#void#run();static#java.awt.AWTEvent#getShutdownEvent()".equals(methods(c)), "methods");
        check("private final#java.lang.Object#activationLock;private final#java.lang.Object#mainLock;private final#java.util.Map#peerMap;private final#java.util.Set#busyThreadSet;private static final#int#SAFETY_TIMEOUT;private static final#sun.awt.AWTAutoShutdown#theInstance;private#boolean#timeoutPassed;private#boolean#toolkitThreadBusy;private#java.lang.Thread#blockerThread".equals(fields(c)), "fields");
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
