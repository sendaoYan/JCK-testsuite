/*
 * @test
 * @summary sun.applet.AppletClassLoader conformance: reflection (no init, native/display-heavy).
 * @run main AppletClassLoaderTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AppletClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.applet.AppletClassLoader", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.applet.AppletClassLoader".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.net.URLClassLoader".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.Boolean#isJDK11Target(java.lang.Class);#java.lang.Boolean#isJDK12Target(java.lang.Class);#java.lang.Class#loadCode(java.lang.String);#java.net.URL#getBaseURL();#void#setCodebaseLookup(boolean);#void#setJDK11Target(java.lang.Class,boolean);#void#setJDK12Target(java.lang.Class,boolean);private static#[B#getBytes(java.net.URL);private static#void#printError(java.lang.String,java.lang.Throwable);private#boolean#resourceExists(java.net.URL);protected#java.lang.Class#findClass(java.lang.String);protected#java.security.PermissionCollection#getPermissions(java.security.CodeSource);protected#sun.awt.AppContext#resetAppContext();protected#void#addJar(java.lang.String);protected#void#release();protected#void#setExceptionStatus();public synchronized#java.lang.Class#loadClass(java.lang.String,boolean);public#[Ljava.net.URL;#getURLs();public#boolean#getExceptionStatus();public#java.io.InputStream#getResourceAsStream(java.lang.String);public#java.io.InputStream#getResourceAsStreamFromJar(java.lang.String);public#java.lang.ThreadGroup#getThreadGroup();public#java.net.URL#findResource(java.lang.String);public#java.util.Enumeration#findResources(java.lang.String);public#sun.awt.AppContext#getAppContext();public#void#disableRecursiveDirectoryRead();public#void#grab()".equals(methods(c)), "methods");
        check("#int#usageCount;private final#java.lang.Object#grabReleaseSynchronizer;private final#java.lang.Object#threadGroupSynchronizer;private static#sun.applet.AppletMessageHandler#mh;private volatile#boolean#allowRecursiveDirectoryRead;private#boolean#codebaseLookup;private#boolean#exceptionStatus;private#boolean#resourceAsStreamFromJarInCall;private#boolean#resourceAsStreamInCall;private#java.lang.Object#syncResourceAsStream;private#java.lang.Object#syncResourceAsStreamFromJar;private#java.net.URL#base;private#java.security.AccessControlContext#acc;private#java.security.CodeSource#codesource;private#java.util.HashMap#jdk11AppletInfo;private#java.util.HashMap#jdk12AppletInfo;private#sun.applet.AppletThreadGroup#threadGroup;private#sun.awt.AppContext#appContext".equals(fields(c)), "fields");
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
