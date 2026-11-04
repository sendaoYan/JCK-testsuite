/*
 * @test
 * @summary java.awt.datatransfer.StringSelection conformance: white-box structure + black-box behavior.
 * @run main StringSelectionTest
 */

import java.io.Reader;
import java.lang.reflect.*;

public class StringSelectionTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.StringSelection", false, StringSelectionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.StringSelection");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.awt.datatransfer.Transferable"), "implements Transferable");
        check(hasInterface(c, "java.awt.datatransfer.ClipboardOwner"), "implements ClipboardOwner");
        check(Modifier.isPublic(c.getConstructor(String.class).getModifiers()), "public String ctor");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.StringSelection s = new java.awt.datatransfer.StringSelection("hello world");
        java.awt.datatransfer.DataFlavor[] fl = s.getTransferDataFlavors();
        check(fl != null && fl.length >= 2, "has stringFlavor + plainTextFlavor");
        check(s.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor), "supports stringFlavor");
        check(!s.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.imageFlavor), "not imageFlavor");
        check("hello world".equals(s.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor)), "getTransferData string");
        Object text = s.getTransferData(java.awt.datatransfer.DataFlavor.plainTextFlavor);
        check(text != null, "plainText data non-null");
        boolean unsup = false;
        try { s.getTransferData(java.awt.datatransfer.DataFlavor.imageFlavor); }
        catch (java.awt.datatransfer.UnsupportedFlavorException e) { unsup = true; }
        check(unsup, "UnsupportedFlavorException for imageFlavor");
        s.lostOwnership(null, null);
        check(true, "lostOwnership callable");

        // ---- flavor array contract ----
        java.awt.datatransfer.DataFlavor[] fl2 = s.getTransferDataFlavors();
        check(fl2.length == 2, "exactly two flavors");
        check(fl2[0].equals(java.awt.datatransfer.DataFlavor.stringFlavor), "first flavor stringFlavor");
        check(fl2[1].equals(java.awt.datatransfer.DataFlavor.plainTextFlavor), "second flavor plainTextFlavor");
        // returned array is a defensive copy: mutating it does not affect the selection
        fl2[0] = null;
        check(s.getTransferDataFlavors()[0] != null, "flavors array is a defensive copy");
        check(s.getTransferDataFlavors() != s.getTransferDataFlavors(), "fresh array per call");

        // ---- plainTextFlavor returns a Reader over the same text ----
        Object plain = s.getTransferData(java.awt.datatransfer.DataFlavor.plainTextFlavor);
        check(plain instanceof Reader, "plainTextFlavor data is a Reader");
        StringBuilder sb = new StringBuilder();
        int ch;
        try (Reader r = (Reader) plain) {
            while ((ch = r.read()) != -1) sb.append((char) ch);
        }
        check(sb.toString().equals("hello world"), "plainText reader yields full text");
        // each call returns a fresh reader positioned at the start
        Reader r2 = (Reader) s.getTransferData(java.awt.datatransfer.DataFlavor.plainTextFlavor);
        r2.read();
        Reader r3 = (Reader) s.getTransferData(java.awt.datatransfer.DataFlavor.plainTextFlavor);
        check(r3.read() == 'h', "fresh reader per getTransferData call");

        // ---- isDataFlavorSupported matrix ----
        check(s.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.plainTextFlavor), "supports own plainTextFlavor");
        boolean npeNull = false;
        try { s.isDataFlavorSupported(null); }
        catch (NullPointerException x) { npeNull = true; }
        check(npeNull, "isDataFlavorSupported(null) throws NPE (implementation calls flavor.equals)");
        check(!s.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.getTextPlainUnicodeFlavor()), "unicode plain text flavor unsupported");

        // ---- boundary strings ----
        java.awt.datatransfer.StringSelection empty = new java.awt.datatransfer.StringSelection("");
        check("".equals(empty.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor)), "empty string round-trip");
        Reader er = (Reader) empty.getTransferData(java.awt.datatransfer.DataFlavor.plainTextFlavor);
        check(er.read() == -1, "empty string reader at EOF");
        String unicode = "汉字 café éè";
        java.awt.datatransfer.StringSelection uni = new java.awt.datatransfer.StringSelection(unicode);
        check(unicode.equals(uni.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor)), "unicode string round-trip");

        // ---- null constructor argument is accepted and flows through ----
        java.awt.datatransfer.StringSelection nullSel = new java.awt.datatransfer.StringSelection(null);
        check(nullSel.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor) == null, "null string round-trips as null");
        Reader nullReader = (Reader) nullSel.getTransferData(java.awt.datatransfer.DataFlavor.plainTextFlavor);
        check(nullReader.read() == -1, "null string plain reader at EOF");

        // ---- UnsupportedFlavorException message is the flavor's human name ----
        boolean unsup2 = false;
        String msg = null;
        try { s.getTransferData(java.awt.datatransfer.DataFlavor.imageFlavor); }
        catch (java.awt.datatransfer.UnsupportedFlavorException e) { unsup2 = true; msg = e.getMessage(); }
        check(unsup2 && msg != null && msg.length() > 0, "UFE carries non-empty message");
        check(java.awt.datatransfer.DataFlavor.imageFlavor.getHumanPresentableName().equals(msg), "UFE message is flavor human name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
