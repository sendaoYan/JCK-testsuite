/*
 * @test
 * @summary sun.applet.AppletViewer conformance: reflection (no init, native/display-heavy).
 * @run main AppletViewerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AppletViewerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.applet.AppletViewer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.applet.AppletViewer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.Frame".equals(c.getSuperclass().getName()), "superclass");
        check("java.applet.AppletContext;java.awt.print.Printable".equals(interfaces(c)), "interfaces");
        check("#void#appletCharacterEncoding();#void#appletClone();#void#appletClose();#void#appletEdit();#void#appletInfo();#void#appletPrint();#void#appletReload();#void#appletRestart();#void#appletSave();#void#appletStart();#void#appletStop();#void#appletTag();private static#java.io.Reader#makeReader(java.io.InputStream);private static#void#checkConnect(java.net.URL);private#[Ljava.lang.String;#splitSeparator(java.lang.String,java.lang.String);private#void#appletShutdown(sun.applet.AppletPanel);private#void#appletSystemExit();private#void#initEventQueue();protected#void#appletQuit();public static synchronized#void#networkProperties();public static#int#countApplets();public static#java.lang.String#scanIdentifier(java.io.Reader);public static#java.util.Hashtable#scanTag(java.io.Reader);public static#void#main([Ljava.lang.String;);public static#void#parse(java.net.URL);public static#void#parse(java.net.URL,java.io.PrintStream,sun.applet.AppletViewerFactory);public static#void#parse(java.net.URL,java.lang.String);public static#void#printTag(java.io.PrintStream,java.util.Hashtable);public static#void#skipSpace(java.io.Reader);public#int#print(java.awt.Graphics,java.awt.print.PageFormat,int);public#java.applet.Applet#getApplet(java.lang.String);public#java.applet.AudioClip#getAudioClip(java.net.URL);public#java.awt.Image#getImage(java.net.URL);public#java.awt.MenuItem#addMenuItem(java.awt.Menu,java.lang.String);public#java.io.InputStream#getStream(java.lang.String);public#java.util.Enumeration#getApplets();public#java.util.Iterator#getStreamKeys();public#void#processUserAction(java.awt.event.ActionEvent);public#void#setStream(java.lang.String,java.io.InputStream);public#void#showDocument(java.net.URL);public#void#showDocument(java.net.URL,java.lang.String);public#void#showStatus(java.lang.String);public#void#updateAtts();static#java.awt.Image#getCachedImage(java.net.URL);static#sun.misc.Ref#getCachedImageRef(java.net.URL);static#void#flushImageCache()".equals(methods(c)), "methods");
        check("#java.awt.Label#label;#java.io.PrintStream#statusMsgStream;#sun.applet.AppletViewerFactory#factory;#sun.applet.AppletViewerPanel#panel;private static final#int#XDELTA;private static final#int#YDELTA;private static#int#x;private static#int#y;private static#java.lang.String#defaultSaveFile;private static#java.util.Map#audioClips;private static#java.util.Map#imageRefs;private static#sun.applet.AppletMessageHandler#amh;static#int#c;static#java.lang.String#encoding;static#java.util.Hashtable#systemParam;static#java.util.Vector#appletPanels;static#sun.applet.AppletProps#props".equals(fields(c)), "fields");
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
