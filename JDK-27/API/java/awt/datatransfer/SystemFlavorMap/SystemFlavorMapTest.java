/*
 * @test
 * @summary java.awt.datatransfer.SystemFlavorMap conformance: white-box structure + black-box behavior.
 * @run main SystemFlavorMapTest
 */

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

public class SystemFlavorMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.datatransfer.SystemFlavorMap", false, SystemFlavorMapTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.SystemFlavorMap");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(hasInterface(c, "java.awt.datatransfer.FlavorMap"), "implements FlavorMap");
        check(hasInterface(c, "java.awt.datatransfer.FlavorTable"), "implements FlavorTable");
        // mapping-mutation API present
        check(hasMethod(c, "addUnencodedNativeForFlavor"), "declares addUnencodedNativeForFlavor");
        check(hasMethod(c, "setNativesForFlavor"), "declares setNativesForFlavor");
        check(hasMethod(c, "addFlavorForUnencodedNative"), "declares addFlavorForUnencodedNative");
        check(hasMethod(c, "setFlavorsForNative"), "declares setFlavorsForNative");
        // no public constructor: instances come from getDefaultFlavorMap()
        boolean publicCtor = false;
        for (Constructor<?> ctor : c.getConstructors()) publicCtor = true;
        check(!publicCtor, "no public ctor (getDefaultFlavorMap is the factory)");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.FlavorMap fm = java.awt.datatransfer.SystemFlavorMap.getDefaultFlavorMap();
        check(fm != null, "getDefaultFlavorMap non-null");
        check(fm instanceof java.awt.datatransfer.FlavorTable, "is FlavorTable");
        // singleton identity within a JVM
        check(java.awt.datatransfer.SystemFlavorMap.getDefaultFlavorMap() == fm, "getDefaultFlavorMap is a singleton");
        check(fm instanceof java.awt.datatransfer.SystemFlavorMap, "default map is SystemFlavorMap");

        java.awt.datatransfer.SystemFlavorMap sfm = (java.awt.datatransfer.SystemFlavorMap) fm;
        java.awt.datatransfer.FlavorTable ft = (java.awt.datatransfer.FlavorTable) fm;

        // ---- FlavorTable list API ----
        List<String> natives = ft.getNativesForFlavor(java.awt.datatransfer.DataFlavor.stringFlavor);
        check(natives != null && !natives.isEmpty(), "natives for stringFlavor non-empty");
        // Native names are platform vocabulary (X11 uses "STRING"; Windows uses
        // "TEXT"/"UNICODE TEXT"), so round-trip a native this host advertises
        // instead of hard-coding one platform's name.
        java.util.List<java.awt.datatransfer.DataFlavor> flavors = ft.getFlavorsForNative(natives.get(0));
        check(flavors != null && !flavors.isEmpty(), "advertised native maps back to flavors [" + natives.get(0) + "]");
        // unknown arbitrary native maps to an empty (never null) list
        List<java.awt.datatransfer.DataFlavor> unknown = ft.getFlavorsForNative("MY_TEST_NATIVE_ZZ");
        check(unknown != null && unknown.isEmpty(), "unknown native -> empty non-null list");
        // encoded JAVA_DATAFLAVOR natives decode into full flavor lists
        List<java.awt.datatransfer.DataFlavor> synth = ft.getFlavorsForNative(
                java.awt.datatransfer.SystemFlavorMap.encodeJavaMIMEType("text/plain"));
        check(synth != null && !synth.isEmpty(), "encoded mime native yields flavors");
        boolean hasTextFlavor = false;
        for (java.awt.datatransfer.DataFlavor f : synth) if ("text".equals(f.getPrimaryType())) hasTextFlavor = true;
        check(hasTextFlavor, "decoded flavors include a text flavor");
        // imageFlavor maps to format natives on this platform, non-empty and not null
        List<String> imgNatives = ft.getNativesForFlavor(java.awt.datatransfer.DataFlavor.imageFlavor);
        check(imgNatives != null && !imgNatives.isEmpty(), "imageFlavor has platform natives");
        // known text flavor maps to text natives
        List<String> textNatives = ft.getNativesForFlavor(new java.awt.datatransfer.DataFlavor("text/plain; charset=unicode"));
        check(textNatives != null && !textNatives.isEmpty(), "text flavor has natives");
        // mutable list semantics: returned lists are snapshots (mutation must not corrupt the map)
        try { natives.add("HACK"); } catch (UnsupportedOperationException ignore) { /* immutable ok */ }
        check(ft.getNativesForFlavor(java.awt.datatransfer.DataFlavor.stringFlavor) != null, "map survives returned-list mutation");
        // empty arrays return empty maps
        Map<java.awt.datatransfer.DataFlavor, String> emptyN = sfm.getNativesForFlavors(new java.awt.datatransfer.DataFlavor[0]);
        check(emptyN != null && emptyN.isEmpty(), "empty flavor array -> empty map");
        Map<String, java.awt.datatransfer.DataFlavor> emptyF = sfm.getFlavorsForNatives(new String[0]);
        check(emptyF != null && emptyF.isEmpty(), "empty native array -> empty map");
        // null array returns the full map
        Map<java.awt.datatransfer.DataFlavor, String> fullN = sfm.getNativesForFlavors(null);
        check(fullN != null && !fullN.isEmpty(), "null flavor array -> full map");
        check(fullN.containsKey(java.awt.datatransfer.DataFlavor.stringFlavor), "full map contains stringFlavor");
        Map<String, java.awt.datatransfer.DataFlavor> fullF = sfm.getFlavorsForNatives(null);
        check(fullF != null && !fullF.isEmpty(), "null native array -> full map");
        check(fullF.containsKey("STRING"), "full map contains STRING");

        // ---- FlavorMap map API ----
        java.util.Map<java.awt.datatransfer.DataFlavor, String> nf = fm.getNativesForFlavors(new java.awt.datatransfer.DataFlavor[]{ java.awt.datatransfer.DataFlavor.stringFlavor });
        check(nf != null && nf.containsKey(java.awt.datatransfer.DataFlavor.stringFlavor), "getNativesForFlavors has stringFlavor");
        check(nf.get(java.awt.datatransfer.DataFlavor.stringFlavor) != null, "stringFlavor mapped to a native");

        // ---- JAVA_DATAFLAVOR encoding ----
        check(!java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType("application/x-java-serialized-object"), "raw serialized mime not java mime");
        check(!java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType("text/plain"), "text/plain not java mime");
        check(java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType("JAVA_DATAFLAVOR:text"), "any JAVA_DATAFLAVOR-prefixed string recognized");
        String enc = java.awt.datatransfer.SystemFlavorMap.encodeJavaMIMEType("application/x-java-serialized-object");
        check(java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType(enc), "encoded still java mime");
        check(enc.startsWith("JAVA_DATAFLAVOR:"), "encoded carries JAVA_DATAFLAVOR prefix");
        String dec = java.awt.datatransfer.SystemFlavorMap.decodeJavaMIMEType(enc);
        check("application/x-java-serialized-object".equals(dec), "decode roundtrip");
        // encode/decode with parameters
        String enc2 = java.awt.datatransfer.SystemFlavorMap.encodeJavaMIMEType("text/plain; charset=utf-8");
        check(java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType(enc2), "encoded with params still java mime");
        check(java.awt.datatransfer.SystemFlavorMap.decodeJavaMIMEType(enc2).startsWith("text/plain"), "decode with params");
        // decode of a non-encoded string yields null
        check(java.awt.datatransfer.SystemFlavorMap.decodeJavaMIMEType("text/plain") == null, "decode non-encoded returns null");
        // DataFlavor encode/decode roundtrip
        String e2 = java.awt.datatransfer.SystemFlavorMap.encodeDataFlavor(java.awt.datatransfer.DataFlavor.stringFlavor);
        java.awt.datatransfer.DataFlavor df = java.awt.datatransfer.SystemFlavorMap.decodeDataFlavor(e2);
        check(java.awt.datatransfer.DataFlavor.stringFlavor.equals(df), "encode/decode DataFlavor roundtrip");
        // decodeDataFlavor on a non-encoded string yields null (no exception)
        check(java.awt.datatransfer.SystemFlavorMap.decodeDataFlavor("text/plain") == null, "decodeDataFlavor non-encoded returns null");

        // ---- mapping mutation with restore ----
        java.awt.datatransfer.DataFlavor probeFlavor = java.awt.datatransfer.DataFlavor.stringFlavor;
        List<String> orig = ft.getNativesForFlavor(probeFlavor);
        sfm.addUnencodedNativeForFlavor(probeFlavor, "MY_PRIVATE_NATIVE");
        check(ft.getNativesForFlavor(probeFlavor).contains("MY_PRIVATE_NATIVE"), "addUnencodedNativeForFlavor visible");
        sfm.setNativesForFlavor(probeFlavor, orig.toArray(new String[0]));
        check(!ft.getNativesForFlavor(probeFlavor).contains("MY_PRIVATE_NATIVE"), "setNativesForFlavor restores original mapping");
        // addFlavorForUnencodedNative + setFlavorsForNative cleanup
        sfm.addFlavorForUnencodedNative("MY_NATIVE_X", java.awt.datatransfer.DataFlavor.stringFlavor);
        List<java.awt.datatransfer.DataFlavor> mutated = ft.getFlavorsForNative("MY_NATIVE_X");
        check(mutated.contains(java.awt.datatransfer.DataFlavor.stringFlavor), "addFlavorForUnencodedNative visible");
        sfm.setFlavorsForNative("MY_NATIVE_X", new java.awt.datatransfer.DataFlavor[0]);
        check(ft.getFlavorsForNative("MY_NATIVE_X").isEmpty(), "setFlavorsForNative empty clears mapping");
        check(ft.getFlavorsForNative("STRING").size() > 0, "STRING mapping unaffected");
        // NPE for null arguments on mutation methods
        boolean npe1 = false, npe2 = false, npe3 = false, npe4 = false;
        try { sfm.addUnencodedNativeForFlavor(null, "n"); } catch (NullPointerException x) { npe1 = true; }
        try { sfm.addUnencodedNativeForFlavor(java.awt.datatransfer.DataFlavor.stringFlavor, null); } catch (NullPointerException x) { npe2 = true; }
        try { sfm.addFlavorForUnencodedNative(null, java.awt.datatransfer.DataFlavor.stringFlavor); } catch (NullPointerException x) { npe3 = true; }
        try { sfm.addFlavorForUnencodedNative("n", null); } catch (NullPointerException x) { npe4 = true; }
        check(npe1, "addUnencodedNativeForFlavor(null,..) NPE");
        check(npe2, "addUnencodedNativeForFlavor(..,null) NPE");
        check(npe3, "addFlavorForUnencodedNative(null,..) NPE");
        check(npe4, "addFlavorForUnencodedNative(..,null) NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
