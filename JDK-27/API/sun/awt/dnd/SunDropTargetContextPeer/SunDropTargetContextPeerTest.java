/*
 * @test
 * @summary sun.awt.dnd.SunDropTargetContextPeer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.dnd:+open
 * @run main SunDropTargetContextPeerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunDropTargetContextPeerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.dnd.SunDropTargetContextPeer", false, SunDropTargetContextPeerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.dnd.SunDropTargetContextPeer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.datatransfer.Transferable;java.awt.dnd.peer.DropTargetContextPeer".equals(interfaces(c)), "interfaces");
        check("private static#java.awt.datatransfer.Transferable#getJVMLocalSourceTransferable();private#int#handleEnterMessage(java.awt.Component,int,int,int,int,[J,long);private#int#handleMotionMessage(java.awt.Component,int,int,int,int,[J,long);private#int#mapOperation(int);private#void#handleDropMessage(java.awt.Component,int,int,int,int,[J,long);private#void#handleExitMessage(java.awt.Component,long);protected abstract#java.lang.Object#getNativeData(long);protected abstract#void#doDropDone(boolean,int,boolean);protected synchronized#long#getNativeDragContext();protected#int#postDropTargetEvent(java.awt.Component,int,int,int,int,[J,long,int,boolean);protected#void#eventPosted(sun.awt.dnd.SunDropTargetEvent);protected#void#eventProcessed(sun.awt.dnd.SunDropTargetEvent,int,boolean);protected#void#processDropMessage(sun.awt.dnd.SunDropTargetEvent);protected#void#processEnterMessage(sun.awt.dnd.SunDropTargetEvent);protected#void#processExitMessage(sun.awt.dnd.SunDropTargetEvent);protected#void#processMotionMessage(sun.awt.dnd.SunDropTargetEvent,boolean);public static#void#setCurrentJVMLocalSourceTransferable(java.awt.datatransfer.Transferable);public synchronized#void#acceptDrag(int);public synchronized#void#acceptDrop(int);public synchronized#void#dropComplete(boolean);public synchronized#void#rejectDrag();public synchronized#void#rejectDrop();public synchronized#void#setTargetActions(int);public#[Ljava.awt.datatransfer.DataFlavor;#getTransferDataFlavors();public#boolean#isDataFlavorSupported(java.awt.datatransfer.DataFlavor);public#boolean#isTransferableJVMLocal();public#int#getTargetActions();public#java.awt.datatransfer.Transferable#getTransferable();public#java.awt.dnd.DropTarget#getDropTarget();public#java.lang.Object#getTransferData(java.awt.datatransfer.DataFlavor)".equals(methods(c)), "methods");
        check("#boolean#dropInProcess;private static final#sun.util.logging.PlatformLogger#dndLog;private#[J#currentT;private#boolean#dragRejected;private#int#currentA;private#int#currentDA;private#int#currentSA;private#int#previousDA;private#java.awt.datatransfer.Transferable#local;private#java.awt.dnd.DropTarget#currentDT;private#java.awt.dnd.DropTargetContext#currentDTC;private#long#nativeDragContext;protected static final#int#STATUS_ACCEPT;protected static final#int#STATUS_NONE;protected static final#int#STATUS_REJECT;protected static final#int#STATUS_WAIT;protected static final#java.lang.Object#_globalLock;protected static#java.awt.datatransfer.Transferable#currentJVMLocalSourceTransferable;protected#boolean#dropComplete;protected#int#dropStatus;public static final#boolean#DISPATCH_SYNC".equals(fields(c)), "fields");
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
