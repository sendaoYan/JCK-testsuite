/*
 * @test
 * @summary sun.awt.datatransfer.DataTransferer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.datatransfer:+open
 * @run main DataTransfererTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DataTransfererTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.datatransfer.DataTransferer", false, DataTransfererTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.datatransfer.DataTransferer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#[B#imageToStandardBytesImpl(java.awt.image.RenderedImage,java.lang.String);private static#[B#convertObjectToBytes(java.lang.Object);private#[B#translateTransferableString(java.lang.String,long);private#java.io.File#castToFile(java.lang.Object);private#java.lang.Object#concatData(java.lang.Object,java.lang.Object);private#java.lang.Object#constructFlavoredObject(java.lang.Object,java.awt.datatransfer.DataFlavor,java.lang.Class);private#java.lang.Object#translateStreamToInputStream(java.io.InputStream,java.awt.datatransfer.DataFlavor,long,java.awt.datatransfer.Transferable);private#java.lang.String#translateBytesToString([B,long,java.awt.datatransfer.Transferable);private#java.util.ArrayList#castToFiles(java.util.List);protected abstract#[B#imageToPlatformBytes(java.awt.Image,long);protected abstract#[Ljava.lang.String;#dragQueryFile([B);protected abstract#java.awt.Image#platformImageBytesToImage([B,long);protected abstract#java.io.ByteArrayOutputStream#convertFileListToBytes(java.util.ArrayList);protected abstract#java.lang.Long#getFormatForNativeAsLong(java.lang.String);protected abstract#java.lang.String#getNativeForFormat(long);protected static#[B#inputStreamToByteArray(java.io.InputStream);protected#[B#imageToStandardBytes(java.awt.Image,java.lang.String);protected#[Ljava.net.URI;#dragQueryURIs(java.io.InputStream,long,java.awt.datatransfer.Transferable);protected#boolean#isTextFormat(long);protected#boolean#isURIListFormat(long);protected#java.awt.Image#standardImageBytesToImage([B,java.lang.String);protected#java.lang.String#getBestCharsetForTextFormat(java.lang.Long,java.awt.datatransfer.Transferable);protected#java.lang.String#getCharsetForTextFormat(java.lang.Long);public abstract#boolean#isFileFormat(long);public abstract#boolean#isImageFormat(long);public abstract#boolean#isLocaleDependentTextFormat(long);public abstract#java.lang.String#getDefaultUnicodeEncoding();public abstract#sun.awt.datatransfer.ToolkitThreadBlockedHandler#getToolkitThreadBlockedHandler();public static synchronized#sun.awt.datatransfer.DataTransferer#getInstance();public static#[J#keysToLongArray(java.util.SortedMap);public static#[Ljava.awt.datatransfer.DataFlavor;#setToSortedDataFlavorArray(java.util.Set);public static#java.awt.datatransfer.FlavorTable#adaptFlavorMap(java.awt.datatransfer.FlavorMap);public#[B#convertData(java.lang.Object,java.awt.datatransfer.Transferable,long,java.util.Map,boolean);public#[B#translateTransferable(java.awt.datatransfer.Transferable,java.awt.datatransfer.DataFlavor,long);public#[J#getFormatsForTransferableAsArray(java.awt.datatransfer.Transferable,java.awt.datatransfer.FlavorTable);public#[Ljava.awt.datatransfer.DataFlavor;#getFlavorsForFormatsAsArray([J,java.awt.datatransfer.FlavorTable);public#java.lang.Object#translateBytes([B,java.awt.datatransfer.DataFlavor,long,java.awt.datatransfer.Transferable);public#java.lang.Object#translateStream(java.io.InputStream,java.awt.datatransfer.DataFlavor,long,java.awt.datatransfer.Transferable);public#java.util.LinkedHashSet#getPlatformMappingsForFlavor(java.awt.datatransfer.DataFlavor);public#java.util.LinkedHashSet#getPlatformMappingsForNative(java.lang.String);public#java.util.Map#getFlavorsForFormats([J,java.awt.datatransfer.FlavorTable);public#java.util.Set#getFlavorsForFormatsAsSet([J,java.awt.datatransfer.FlavorTable);public#java.util.SortedMap#getFormatsForFlavors([Ljava.awt.datatransfer.DataFlavor;,java.awt.datatransfer.FlavorTable);public#java.util.SortedMap#getFormatsForTransferable(java.awt.datatransfer.Transferable,java.awt.datatransfer.FlavorTable);public#void#processDataConversionRequests();public#void#registerTextFlavorProperties(java.lang.String,java.lang.String,java.lang.String,java.lang.String)".equals(methods(c)), "methods");
        check("private static final#java.util.Map#nativeCharsets;private static final#java.util.Map#nativeEOLNs;private static final#java.util.Map#nativeTerminators;private static final#java.util.Set#textNatives;private static volatile#java.lang.Runnable#dataConverterInstance;public static final#java.awt.datatransfer.DataFlavor#javaTextEncodingFlavor".equals(fields(c)), "fields");
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
