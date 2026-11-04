/*
 * @test
 * @summary sun.awt.datatransfer.SunClipboard conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.datatransfer:+open
 * @run main SunClipboardTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunClipboardTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.datatransfer.SunClipboard", false, SunClipboardTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.datatransfer.SunClipboard".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.datatransfer.Clipboard".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#java.util.Set#formatArrayAsDataFlavorSet([J);protected abstract#[B#getClipboardData(long);protected abstract#[J#getClipboardFormats();protected abstract#void#clearNativeContext();protected abstract#void#registerClipboardViewerChecked();protected abstract#void#setContentsNative(java.awt.datatransfer.Transferable);protected abstract#void#unregisterClipboardViewerChecked();protected final#void#checkChange([J);protected synchronized#java.awt.datatransfer.Transferable#getContextContents();protected#[J#getClipboardFormatsOpenClose();protected#java.awt.datatransfer.Transferable#createLocaleTransferable([J);protected#void#lostOwnershipImpl();protected#void#lostOwnershipLater();protected#void#lostOwnershipNow();public abstract#long#getID();public static#java.awt.datatransfer.FlavorTable#getDefaultFlavorTable();public synchronized#[Ljava.awt.datatransfer.FlavorListener;#getFlavorListeners();public synchronized#java.awt.datatransfer.Transferable#getContents(java.lang.Object);public synchronized#void#addFlavorListener(java.awt.datatransfer.FlavorListener);public synchronized#void#removeFlavorListener(java.awt.datatransfer.FlavorListener);public synchronized#void#setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner);public#[Ljava.awt.datatransfer.DataFlavor;#getAvailableDataFlavors();public#boolean#areFlavorListenersRegistered();public#boolean#isDataFlavorAvailable(java.awt.datatransfer.DataFlavor);public#java.lang.Object#getData(java.awt.datatransfer.DataFlavor);public#void#closeClipboard();public#void#openClipboard(sun.awt.datatransfer.SunClipboard)".equals(methods(c)), "methods");
        check("private static#java.util.Set#flavorListeners;private volatile#[J#currentFormats;private volatile#int#numberOfFlavorListeners".equals(fields(c)), "fields");
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
