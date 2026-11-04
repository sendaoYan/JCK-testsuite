/*
 * @test
 * @summary sun.java2d.SunGraphicsEnvironment conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d:+open
 * @run main SunGraphicsEnvironmentTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunGraphicsEnvironmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.SunGraphicsEnvironment", false, SunGraphicsEnvironmentTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.SunGraphicsEnvironment".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.GraphicsEnvironment".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.DisplayChangedListener".equals(interfaces(c)), "interfaces");
        check("protected abstract#int#getNumScreens();protected abstract#java.awt.GraphicsDevice#makeScreenDevice(int);public abstract#boolean#isDisplayLocal();public static#boolean#isUIScaleEnabled();public static#double#getDebugScale();public static#double#getScaleFactor(java.lang.String);public static#java.awt.Dimension#toUserSpace(java.awt.GraphicsConfiguration,int,int);public static#java.awt.GraphicsConfiguration#getGraphicsConfigurationAtPoint(java.awt.GraphicsConfiguration,double,double);public static#java.awt.Point#toDeviceSpace(int,int);public static#java.awt.Point#toDeviceSpaceAbs(int,int);public static#java.awt.Rectangle#getGCDeviceBounds(java.awt.GraphicsConfiguration);public static#java.awt.Rectangle#getUsableBounds(java.awt.GraphicsDevice);public static#java.awt.Rectangle#toDeviceSpace(java.awt.GraphicsConfiguration,int,int,int,int);public static#java.awt.Rectangle#toDeviceSpaceAbs(java.awt.GraphicsConfiguration,int,int,int,int);public static#java.awt.Rectangle#toDeviceSpaceAbs(java.awt.Rectangle);public static#sun.font.FontManagerForSGE#getFontManagerForSGE();public static#void#useAlternateFontforJALocales();public synchronized#[Ljava.awt.GraphicsDevice;#getScreenDevices();public#[Ljava.awt.Font;#getAllFonts();public#[Ljava.lang.String;#getAvailableFontFamilyNames();public#[Ljava.lang.String;#getAvailableFontFamilyNames(java.util.Locale);public#boolean#isFlipStrategyPreferred(java.awt.peer.ComponentPeer);public#java.awt.Graphics2D#createGraphics(java.awt.image.BufferedImage);public#java.awt.GraphicsDevice#getDefaultScreenDevice();public#void#addDisplayChangedListener(sun.awt.DisplayChangedListener);public#void#displayChanged();public#void#paletteChanged();public#void#removeDisplayChangedListener(sun.awt.DisplayChangedListener)".equals(methods(c)), "methods");
        check("private final#java.awt.Font#defaultFont;private static final#boolean#uiScaleEnabled;private static final#double#debugScale;protected#[Ljava.awt.GraphicsDevice;#screens;protected#sun.awt.SunDisplayChanger#displayChanger".equals(fields(c)), "fields");
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
