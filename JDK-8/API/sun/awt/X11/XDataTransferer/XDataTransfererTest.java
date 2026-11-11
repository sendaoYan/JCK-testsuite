/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XDataTransferer conformance: reflection (no init, native/display-heavy).
 * @run main XDataTransfererTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XDataTransfererTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XDataTransferer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XDataTransferer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.datatransfer.DataTransferer".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#boolean#isMimeFormat(long,java.lang.String);private#java.lang.String#getTargetNameForAtom(long);private#javax.imageio.ImageTypeSpecifier#getDefaultImageTypeSpecifier();protected#[B#imageToPlatformBytes(java.awt.Image,long);protected#[Ljava.lang.String;#dragQueryFile([B);protected#[Ljava.net.URI;#dragQueryURIs(java.io.InputStream,long,java.awt.datatransfer.Transferable);protected#boolean#isURIListFormat(long);protected#java.awt.Image#platformImageBytesToImage([B,long);protected#java.io.ByteArrayOutputStream#convertFileListToBytes(java.util.ArrayList);protected#java.lang.Long#getFormatForNativeAsLong(java.lang.String);protected#java.lang.String#getCharsetForTextFormat(java.lang.Long);protected#java.lang.String#getNativeForFormat(long);public#boolean#isFileFormat(long);public#boolean#isImageFormat(long);public#boolean#isLocaleDependentTextFormat(long);public#boolean#isTextFormat(long);public#java.lang.String#getDefaultUnicodeEncoding();public#java.util.LinkedHashSet#getPlatformMappingsForFlavor(java.awt.datatransfer.DataFlavor);public#java.util.LinkedHashSet#getPlatformMappingsForNative(java.lang.String);public#sun.awt.datatransfer.ToolkitThreadBlockedHandler#getToolkitThreadBlockedHandler();static synchronized#sun.awt.X11.XDataTransferer#getInstanceImpl()".equals(methods(c)), "methods");
        check("private static#javax.imageio.ImageTypeSpecifier#defaultSpecifier;private static#sun.awt.X11.XDataTransferer#transferer;static final#sun.awt.X11.XAtom#DT_NET_FILE_ATOM;static final#sun.awt.X11.XAtom#FILE_NAME_ATOM;static final#sun.awt.X11.XAtom#INCR_ATOM;static final#sun.awt.X11.XAtom#JFIF_ATOM;static final#sun.awt.X11.XAtom#MULTIPLE_ATOM;static final#sun.awt.X11.XAtom#PNG_ATOM;static final#sun.awt.X11.XAtom#TARGETS_ATOM".equals(fields(c)), "fields");
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
