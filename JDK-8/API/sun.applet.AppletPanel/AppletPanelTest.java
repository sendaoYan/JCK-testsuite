/*
 * @test
 * @summary sun.applet.AppletPanel conformance: reflection (no init, native/display-heavy).
 * @run main AppletPanelTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AppletPanelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.applet.AppletPanel", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.applet.AppletPanel".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.Panel".equals(c.getSuperclass().getName()), "superclass");
        check("java.applet.AppletStub;java.lang.Runnable".equals(interfaces(c)), "interfaces");
        check("#boolean#emptyEventQueue();#void#joinAppletThread();#void#release();private synchronized#void#setLoaderThread(java.lang.Thread);private#int#readByte(byte);private#int#readShort([B,int);private#java.awt.Component#getMostRecentFocusOwnerForWindow(java.awt.Window);private#java.security.AccessControlContext#getAccessControlContext(java.net.URL);private#void#findAppletJDKLevel(java.applet.Applet);private#void#runLoader();private#void#setDefaultFocus();private#void#setExceptionStatus(java.security.AccessControlException);protected abstract#java.lang.String#getCode();protected abstract#java.lang.String#getJarFiles();protected abstract#java.lang.String#getSerializedObject();protected synchronized#boolean#okToLoad();protected synchronized#void#clearLoadAbortRequest();protected synchronized#void#setLoadAbortRequest();protected synchronized#void#stopLoading();protected#boolean#isJDK11Applet();protected#boolean#isJDK12Applet();protected#java.applet.Applet#createApplet(sun.applet.AppletClassLoader);protected#sun.applet.AppletClassLoader#createClassLoader(java.net.URL);protected#void#loadJarFiles(sun.applet.AppletClassLoader);protected#void#setupAppletAppContext();protected#void#showAppletException(java.lang.Throwable);protected#void#showAppletLog(java.lang.String);protected#void#showAppletLog(java.lang.String,java.lang.Object);protected#void#showAppletStatus(java.lang.String);protected#void#showAppletStatus(java.lang.String,java.lang.Object);protected#void#showAppletStatus(java.lang.String,java.lang.Object,java.lang.Object);public abstract#boolean#hasInitialFocus();public abstract#int#getHeight();public abstract#int#getWidth();public static synchronized#void#flushClassLoader(java.lang.String);public static synchronized#void#flushClassLoaders();public static#void#changeFrameAppContext(java.awt.Frame,sun.awt.AppContext);public synchronized#void#addAppletListener(sun.applet.AppletListener);public synchronized#void#removeAppletListener(sun.applet.AppletListener);public#boolean#isActive();public#int#getAppletHeight();public#int#getAppletWidth();public#java.applet.Applet#getApplet();public#java.awt.Dimension#minimumSize();public#java.awt.Dimension#preferredSize();public#java.lang.String#getClassLoaderCacheKey();public#java.lang.Thread#getAppletHandlerThread();public#void#appletResize(int,int);public#void#dispatchAppletEvent(int,java.lang.Object);public#void#init();public#void#run();public#void#sendEvent(int);public#void#setBounds(int,int,int,int);synchronized#sun.applet.AppletClassLoader#getClassLoader(java.net.URL,java.lang.String);synchronized#sun.applet.AppletEvent#getNextEvent();synchronized#void#createAppletThread()".equals(methods(c)), "methods");
        check("#boolean#loadAbortRequest;#java.applet.Applet#applet;#java.awt.Dimension#currentAppletSize;#java.awt.Dimension#defaultAppletSize;#java.lang.Thread#loaderThread;#sun.misc.MessageUtils#mu;private static#int#threadGroupNumber;private static#java.util.HashMap#classloaders;private static#sun.applet.AppletMessageHandler#amh;private#boolean#jdk11Applet;private#boolean#jdk12Applet;private#java.awt.EventQueue#appEvtQ;private#sun.applet.AppletListener#listeners;private#sun.misc.Queue#queue;protected#boolean#doInit;protected#int#status;protected#java.lang.Thread#handler;protected#sun.applet.AppletClassLoader#loader;public static final#int#APPLET_DESTROY;public static final#int#APPLET_DISPOSE;public static final#int#APPLET_ERROR;public static final#int#APPLET_INIT;public static final#int#APPLET_LOAD;public static final#int#APPLET_LOADING;public static final#int#APPLET_LOADING_COMPLETED;public static final#int#APPLET_QUIT;public static final#int#APPLET_RESIZE;public static final#int#APPLET_START;public static final#int#APPLET_STOP".equals(fields(c)), "fields");
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
