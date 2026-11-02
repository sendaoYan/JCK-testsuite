/*
 * @test
 * @summary sun.awt.datatransfer.DataTransferer conformance: reflection (no init, native/display-heavy).
 * @run main DataTransfererTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DataTransfererTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.datatransfer.DataTransferer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.datatransfer.DataTransferer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#[B#convertObjectToBytes(java.lang.Object);private static#boolean#isFileInWebstartedCache(java.io.File);private static#java.lang.Object#createMapping(java.lang.Object,java.lang.Object);private static#java.security.ProtectionDomain#getUserProtectionDomain(java.awt.datatransfer.Transferable);private#[B#translateTransferableString(java.lang.String,long);private#boolean#isForbiddenToRead(java.io.File,java.security.ProtectionDomain);private#java.io.File#castToFile(java.lang.Object);private#java.lang.Object#concatData(java.lang.Object,java.lang.Object);private#java.lang.Object#constructFlavoredObject(java.lang.Object,java.awt.datatransfer.DataFlavor,java.lang.Class);private#java.lang.Object#translateStreamToInputStream(java.io.InputStream,java.awt.datatransfer.DataFlavor,long,java.awt.datatransfer.Transferable);private#java.lang.String#getBestCharsetForTextFormat(java.lang.Long,java.awt.datatransfer.Transferable);private#java.lang.String#removeSuspectedData(java.awt.datatransfer.DataFlavor,java.awt.datatransfer.Transferable,java.lang.String);private#java.lang.String#translateBytesToString([B,long,java.awt.datatransfer.Transferable);private#java.util.ArrayList#castToFiles(java.util.List,java.security.ProtectionDomain);protected abstract#[B#imageToPlatformBytes(java.awt.Image,long);protected abstract#[Ljava.lang.String;#dragQueryFile([B);protected abstract#java.awt.Image#platformImageBytesToImage([B,long);protected abstract#java.io.ByteArrayOutputStream#convertFileListToBytes(java.util.ArrayList);protected abstract#java.lang.Long#getFormatForNativeAsLong(java.lang.String);protected abstract#java.lang.String#getNativeForFormat(long);protected static#[B#inputStreamToByteArray(java.io.InputStream);protected#[B#imageToStandardBytes(java.awt.Image,java.lang.String);protected#[B#imageToStandardBytesImpl(java.awt.image.RenderedImage,java.lang.String);protected#[Ljava.net.URI;#dragQueryURIs(java.io.InputStream,long,java.awt.datatransfer.Transferable);protected#boolean#isTextFormat(long);protected#boolean#isURIListFormat(long);protected#java.awt.Image#standardImageBytesToImage([B,java.lang.String);protected#java.lang.String#getCharsetForTextFormat(java.lang.Long);public abstract#boolean#isFileFormat(long);public abstract#boolean#isImageFormat(long);public abstract#boolean#isLocaleDependentTextFormat(long);public abstract#java.lang.String#getDefaultUnicodeEncoding();public abstract#sun.awt.datatransfer.ToolkitThreadBlockedHandler#getToolkitThreadBlockedHandler();public static synchronized#sun.awt.datatransfer.DataTransferer#getInstance();public static#[J#keysToLongArray(java.util.SortedMap);public static#[Ljava.awt.datatransfer.DataFlavor;#setToSortedDataFlavorArray(java.util.Set);public static#boolean#doesSubtypeSupportCharset(java.awt.datatransfer.DataFlavor);public static#boolean#doesSubtypeSupportCharset(java.lang.String,java.lang.String);public static#boolean#isEncodingSupported(java.lang.String);public static#boolean#isFlavorCharsetTextType(java.awt.datatransfer.DataFlavor);public static#boolean#isFlavorNoncharsetTextType(java.awt.datatransfer.DataFlavor);public static#boolean#isRemote(java.lang.Class);public static#java.awt.datatransfer.FlavorTable#adaptFlavorMap(java.awt.datatransfer.FlavorMap);public static#java.lang.String#canonicalName(java.lang.String);public static#java.lang.String#getDefaultTextCharset();public static#java.lang.String#getTextCharset(java.awt.datatransfer.DataFlavor);public static#java.util.Set#standardEncodings();public#[B#convertData(java.lang.Object,java.awt.datatransfer.Transferable,long,java.util.Map,boolean);public#[B#translateTransferable(java.awt.datatransfer.Transferable,java.awt.datatransfer.DataFlavor,long);public#[J#getFormatsForFlavorAsArray(java.awt.datatransfer.DataFlavor,java.awt.datatransfer.FlavorTable);public#[J#getFormatsForFlavorsAsArray([Ljava.awt.datatransfer.DataFlavor;,java.awt.datatransfer.FlavorTable);public#[J#getFormatsForTransferableAsArray(java.awt.datatransfer.Transferable,java.awt.datatransfer.FlavorTable);public#[Ljava.awt.datatransfer.DataFlavor;#getFlavorsForFormatAsArray(long,java.awt.datatransfer.FlavorTable);public#[Ljava.awt.datatransfer.DataFlavor;#getFlavorsForFormatsAsArray([J,java.awt.datatransfer.FlavorTable);public#java.lang.Object#translateBytes([B,java.awt.datatransfer.DataFlavor,long,java.awt.datatransfer.Transferable);public#java.lang.Object#translateStream(java.io.InputStream,java.awt.datatransfer.DataFlavor,long,java.awt.datatransfer.Transferable);public#java.util.LinkedHashSet#getPlatformMappingsForFlavor(java.awt.datatransfer.DataFlavor);public#java.util.LinkedHashSet#getPlatformMappingsForNative(java.lang.String);public#java.util.Map#getFlavorsForFormat(long,java.awt.datatransfer.FlavorTable);public#java.util.Map#getFlavorsForFormats([J,java.awt.datatransfer.FlavorTable);public#java.util.Set#getFlavorsForFormatsAsSet([J,java.awt.datatransfer.FlavorTable);public#java.util.SortedMap#getFormatsForFlavor(java.awt.datatransfer.DataFlavor,java.awt.datatransfer.FlavorTable);public#java.util.SortedMap#getFormatsForFlavors([Ljava.awt.datatransfer.DataFlavor;,java.awt.datatransfer.FlavorTable);public#java.util.SortedMap#getFormatsForTransferable(java.awt.datatransfer.Transferable,java.awt.datatransfer.FlavorTable);public#void#processDataConversionRequests();public#void#registerTextFlavorProperties(java.lang.String,java.lang.String,java.lang.String,java.lang.String)".equals(methods(c)), "methods");
        check("private static final#[Ljava.lang.String;#DEPLOYMENT_CACHE_PROPERTIES;private static final#java.lang.String#DATA_CONVERTER_KEY;private static final#java.util.ArrayList#deploymentCacheDirectoryList;private static final#java.util.Map#nativeCharsets;private static final#java.util.Map#nativeEOLNs;private static final#java.util.Map#nativeTerminators;private static final#java.util.Map#textMIMESubtypeCharsetSupport;private static final#java.util.Set#textNatives;private static final#sun.util.logging.PlatformLogger#dtLog;private static#java.lang.String#defaultEncoding;private static#sun.awt.datatransfer.DataTransferer#transferer;public static final#java.awt.datatransfer.DataFlavor#javaTextEncodingFlavor;public static final#java.awt.datatransfer.DataFlavor#plainTextStringFlavor".equals(fields(c)), "fields");
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
