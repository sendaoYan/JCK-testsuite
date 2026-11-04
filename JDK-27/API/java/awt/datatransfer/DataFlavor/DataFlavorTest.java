/*
 * @test
 * @summary java.awt.datatransfer.DataFlavor conformance: white-box structure + black-box behavior.
 * @run main DataFlavorTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class DataFlavorTest {
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
    @SuppressWarnings("unchecked")
    static byte[] ser(Object o) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        new ObjectOutputStream(b).writeObject(o);
        return b.toByteArray();
    }
    @SuppressWarnings("unchecked")
    static <T> T deser(byte[] b) throws IOException, ClassNotFoundException {
        return (T) new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.datatransfer.DataFlavor", false, DataFlavorTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.DataFlavor");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(!c.isInterface(), "class");
        check(hasInterface(c, "java.io.Externalizable"), "implements Externalizable");
        check(hasInterface(c, "java.lang.Cloneable"), "implements Cloneable");
        check(hasMethod(c, "getMimeType"), "declares getMimeType");
        check(hasMethod(c, "match"), "declares match");
        check(hasMethod(c, "isMimeTypeEqual"), "declares isMimeTypeEqual");
        // JDK-27: the three HTML flavor constants are final (in JDK-8 they are not)
        check(Modifier.isFinal(c.getField("selectionHtmlFlavor").getModifiers()), "selectionHtmlFlavor final");
        check(Modifier.isFinal(c.getField("fragmentHtmlFlavor").getModifiers()), "fragmentHtmlFlavor final");
        check(Modifier.isFinal(c.getField("allHtmlFlavor").getModifiers()), "allHtmlFlavor final");
        check(Modifier.isFinal(c.getField("stringFlavor").getModifiers()), "stringFlavor final");
        check(Modifier.isFinal(c.getField("imageFlavor").getModifiers()), "imageFlavor final");
        check(Modifier.isFinal(c.getField("javaFileListFlavor").getModifiers()), "javaFileListFlavor final");
        check(Modifier.isFinal(c.getField("plainTextFlavor").getModifiers()), "plainTextFlavor final");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.DataFlavor str = java.awt.datatransfer.DataFlavor.stringFlavor;
        check(str.getRepresentationClass() == String.class, "stringFlavor representation String");
        check(str.isFlavorTextType(), "stringFlavor is text");
        check(str.isMimeTypeSerializedObject(), "stringFlavor serialized mime");
        check("Unicode String".equals(str.getHumanPresentableName()), "stringFlavor human name Unicode String");
        check("application/x-java-serialized-object".equals(java.awt.datatransfer.DataFlavor.javaSerializedObjectMimeType), "javaSerializedObjectMimeType constant");
        check("application/x-java-jvm-local-objectref".equals(java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType), "javaJVMLocalObjectMimeType constant");
        check("application/x-java-remote-object".equals(java.awt.datatransfer.DataFlavor.javaRemoteObjectMimeType), "javaRemoteObjectMimeType constant");
        java.awt.datatransfer.DataFlavor f = new java.awt.datatransfer.DataFlavor("text/plain; charset=utf-8");
        check(f.getMimeType().startsWith("text/plain"), "getMimeType");
        check("text".equals(f.getPrimaryType()), "getPrimaryType");
        check("plain".equals(f.getSubType()), "getSubType");
        check("utf-8".equals(f.getParameter("charset")), "getParameter charset");
        check(f.getParameter("nonexistent") == null, "getParameter missing returns null");
        f.setHumanPresentableName("Plain Text");
        check("Plain Text".equals(f.getHumanPresentableName()), "setHumanPresentableName");
        check(f.isMimeTypeEqual("text/plain"), "isMimeTypeEqual(String)");
        check(f.match(new java.awt.datatransfer.DataFlavor("text/plain; charset=utf-8")), "match");
        check(f.equals(f), "equals reflexive");
        check(f.hashCode() == f.hashCode(), "hashCode stable");
        java.awt.datatransfer.DataFlavor typed = new java.awt.datatransfer.DataFlavor(String.class, "text/plain");
        check(typed.getRepresentationClass() == String.class, "Class ctor representationClass");
        check(typed.isRepresentationClassSerializable(), "String serializable");
        java.awt.datatransfer.DataFlavor javaFile = java.awt.datatransfer.DataFlavor.javaFileListFlavor;
        check(javaFile.isFlavorJavaFileListType(), "javaFileListFlavor flag");
        check(java.awt.datatransfer.DataFlavor.getTextPlainUnicodeFlavor() != null, "getTextPlainUnicodeFlavor");
        java.awt.datatransfer.DataFlavor cl = (java.awt.datatransfer.DataFlavor) f.clone();
        check(cl != f && cl.equals(f), "clone equal");
        check(f.toString() != null, "toString non-null");
        java.awt.datatransfer.DataFlavor noRep = new java.awt.datatransfer.DataFlavor("application/octet-stream");
        check("application".equals(noRep.getPrimaryType()), "octet-stream primary");
        check(noRep.getRepresentationClass() != null, "default representationClass non-null");

        // ---- constructor matrix ----
        // default constructor creates a placeholder flavor
        java.awt.datatransfer.DataFlavor dflt = new java.awt.datatransfer.DataFlavor();
        check(dflt.getMimeType() == null, "default ctor mime is null (placeholder flavor)");
        check("java.io.InputStream".equals(dflt.getDefaultRepresentationClassAsString()), "default representation class InputStream");
        check(dflt.getDefaultRepresentationClass() == InputStream.class, "getDefaultRepresentationClass");
        // (String mime) - null rejected with NPE
        boolean npe = false;
        try { new java.awt.datatransfer.DataFlavor((String) null); }
        catch (NullPointerException x) { npe = true; }
        check(npe, "DataFlavor(null) throws NPE");
        // (String mime) - unparsable strings rejected with IAE
        String[] bad = { "", "not a valid mime type!", "///" };
        for (String s : bad) {
            boolean iae = false;
            try { new java.awt.datatransfer.DataFlavor(s); }
            catch (IllegalArgumentException x) { iae = true; }
            check(iae, "bad mime [" + s + "] throws IAE");
        }
        // (String, String) full form with human name
        java.awt.datatransfer.DataFlavor named = new java.awt.datatransfer.DataFlavor("text/html", "HTML");
        check("HTML".equals(named.getHumanPresentableName()), "(mime,humanName) ctor");
        check("html".equals(named.getSubType()), "(mime,humanName) sub type");
        // (String, String, ClassLoader) resolves class parameter
        java.awt.datatransfer.DataFlavor withLoader = new java.awt.datatransfer.DataFlavor("text/plain; class=java.lang.String", "P", null);
        check(withLoader.getRepresentationClass() == String.class, "3-arg ctor resolves class param");
        // (Class, String) - null class rejected with NPE
        boolean npeClass = false;
        try { new java.awt.datatransfer.DataFlavor((Class<?>) null, "x"); }
        catch (NullPointerException x) { npeClass = true; }
        check(npeClass, "DataFlavor((Class)null,..) throws NPE");
        // (Class, String) mime derived from serialized-object template
        java.awt.datatransfer.DataFlavor serObj = new java.awt.datatransfer.DataFlavor(String.class, "S");
        check(serObj.isFlavorSerializedObjectType(), "(Class,..) ctor is serialized-object type");
        check(serObj.getRepresentationClass() == String.class, "(Class,..) ctor rep class");

        // ---- MIME parsing: case-insensitivity and parameters ----
        java.awt.datatransfer.DataFlavor upper = new java.awt.datatransfer.DataFlavor("TEXT/PLAIN; CHARSET=UTF-8");
        check("text".equals(upper.getPrimaryType()), "primary type lowercased");
        check("plain".equals(upper.getSubType()), "sub type lowercased");
        check("UTF-8".equals(upper.getParameter("charset")), "charset param preserved");
        check(upper.isMimeTypeEqual("text/plain; charset=UTF-8"), "isMimeTypeEqual ignores case");
        check(upper.isMimeTypeEqual(new java.awt.datatransfer.DataFlavor("text/plain; charset=UTF-8")), "isMimeTypeEqual(DataFlavor) ignores case");
        check(!f.isMimeTypeEqual("text/html"), "isMimeTypeEqual different type false");
        java.awt.datatransfer.DataFlavor withClassParam = new java.awt.datatransfer.DataFlavor("text/plain; class=java.io.Reader");
        check(Reader.class == withClassParam.getRepresentationClass(), "class param sets representation class");
        check(withClassParam.isRepresentationClassReader(), "isRepresentationClassReader");

        // ---- equals/hashCode contract ----
        java.awt.datatransfer.DataFlavor a1 = new java.awt.datatransfer.DataFlavor("text/plain; charset=utf-8");
        java.awt.datatransfer.DataFlavor a2 = new java.awt.datatransfer.DataFlavor("text/plain; charset=utf-8");
        check(a1.equals(a2), "equal flavors from same mime");
        check(a2.equals(a1), "equals symmetric");
        check(a1.hashCode() == a2.hashCode(), "equal flavors equal hashCode");
        check(a1.match(a2), "match equal flavors");
        java.awt.datatransfer.DataFlavor b1 = new java.awt.datatransfer.DataFlavor("text/plain; charset=us-ascii");
        check(!a1.equals(b1), "different charset not equal");
        check(!a1.match(b1), "different charset does not match");
        check(!a1.equals((Object) null), "equals((Object)null) false");
        check(!a1.equals((java.awt.datatransfer.DataFlavor) null), "equals((DataFlavor)null) false");
        check(!a1.equals((Object) "text/plain; charset=utf-8"), "equals(Object,String) false");
        check(!a1.equals((Object) Integer.valueOf(1)), "equals(Object,Integer) false");
        check(!a1.equals(java.awt.datatransfer.DataFlavor.stringFlavor), "text/plain not equal stringFlavor");
        // human presentable name is NOT part of equals
        java.awt.datatransfer.DataFlavor n1 = new java.awt.datatransfer.DataFlavor("text/plain");
        java.awt.datatransfer.DataFlavor n2 = new java.awt.datatransfer.DataFlavor("text/plain");
        n1.setHumanPresentableName("A");
        n2.setHumanPresentableName("B");
        check(n1.equals(n2), "human name not part of equals");
        check(n1.hashCode() == n2.hashCode(), "human name not part of hashCode");
        // deprecated equals(String) compares primaryType/subType only
        check(a1.equals("text/plain"), "equals(String) mime equality");

        // ---- predicate matrix ----
        check(java.awt.datatransfer.DataFlavor.javaFileListFlavor.isFlavorJavaFileListType(), "javaFileListFlavor isFlavorJavaFileListType");
        check(!f.isFlavorJavaFileListType(), "text/plain not file list");
        check(!f.isFlavorRemoteObjectType(), "text/plain not remote");
        check(!f.isFlavorSerializedObjectType(), "text/plain not serialized-object flavor");
        check(!f.isMimeTypeSerializedObject(), "text/plain not serialized mime");
        java.awt.datatransfer.DataFlavor serMime = new java.awt.datatransfer.DataFlavor("application/x-java-serialized-object; class=java.lang.String");
        check(serMime.isMimeTypeSerializedObject(), "serialized mime flag");
        check(serMime.isFlavorSerializedObjectType(), "serialized-object flavor flag");
        java.awt.datatransfer.DataFlavor remoteMime = new java.awt.datatransfer.DataFlavor("application/x-java-remote-object; class=java.lang.String");
        check(remoteMime.getRepresentationClass() == String.class, "remote mime class param");
        check(!remoteMime.isFlavorRemoteObjectType(), "remote mime but non-Remote rep class => false");
        java.awt.datatransfer.DataFlavor textFlavor = new java.awt.datatransfer.DataFlavor("text/html");
        check(textFlavor.isFlavorTextType(), "text/html is text type");
        check(!noRep.isFlavorTextType(), "application/octet-stream not text");
        check(java.awt.datatransfer.DataFlavor.imageFlavor.getRepresentationClass() == java.awt.Image.class, "imageFlavor rep class Image");
        check(!java.awt.datatransfer.DataFlavor.imageFlavor.isFlavorTextType(), "imageFlavor not text");
        // representation-class predicates
        check(new java.awt.datatransfer.DataFlavor("text/plain").isRepresentationClassInputStream(), "default rep class InputStream");
        check(!new java.awt.datatransfer.DataFlavor("text/plain").isRepresentationClassReader(), "default not Reader");
        check(!new java.awt.datatransfer.DataFlavor("text/plain").isRepresentationClassCharBuffer(), "default not CharBuffer");
        check(!new java.awt.datatransfer.DataFlavor("text/plain").isRepresentationClassByteBuffer(), "default not ByteBuffer");
        java.awt.datatransfer.DataFlavor cb = new java.awt.datatransfer.DataFlavor("text/plain; class=java.nio.CharBuffer");
        check(cb.isRepresentationClassCharBuffer(), "CharBuffer rep class");
        java.awt.datatransfer.DataFlavor bb = new java.awt.datatransfer.DataFlavor("text/plain; class=java.nio.ByteBuffer");
        check(bb.isRepresentationClassByteBuffer(), "ByteBuffer rep class");
        check(cb.getRepresentationClass() == CharBuffer.class, "CharBuffer class resolved");
        check(bb.getRepresentationClass() == ByteBuffer.class, "ByteBuffer class resolved");

        // ---- clone independence ----
        java.awt.datatransfer.DataFlavor orig = new java.awt.datatransfer.DataFlavor("text/plain", "orig");
        java.awt.datatransfer.DataFlavor clone = (java.awt.datatransfer.DataFlavor) orig.clone();
        check(clone.equals(orig), "clone equals original");
        clone.setHumanPresentableName("changed");
        check("orig".equals(orig.getHumanPresentableName()), "clone human-name change does not leak back");

        // ---- serialization round-trip (Externalizable) ----
        java.awt.datatransfer.DataFlavor s1 = new java.awt.datatransfer.DataFlavor("text/plain; charset=utf-8", "SerName");
        java.awt.datatransfer.DataFlavor s1b = deser(ser(s1));
        check(s1b.equals(s1), "serialization round-trip equals");
        check("SerName".equals(s1b.getHumanPresentableName()), "serialization preserves human name");
        check("utf-8".equals(s1b.getParameter("charset")), "serialization preserves charset param");
        check(s1b.isFlavorTextType(), "round-trip keeps text type");
        java.awt.datatransfer.DataFlavor s2 = deser(ser(java.awt.datatransfer.DataFlavor.javaFileListFlavor));
        check(s2.equals(java.awt.datatransfer.DataFlavor.javaFileListFlavor), "javaFileListFlavor round-trip");
        check(s2.isFlavorJavaFileListType(), "round-trip keeps file-list flag");
        java.awt.datatransfer.DataFlavor s3 = deser(ser(java.awt.datatransfer.DataFlavor.stringFlavor));
        check(s3.getRepresentationClass() == String.class, "stringFlavor round-trip rep class");
        check(s3.equals(java.awt.datatransfer.DataFlavor.stringFlavor), "stringFlavor round-trip equals");

        // ---- selectBestTextFlavor ----
        check(java.awt.datatransfer.DataFlavor.selectBestTextFlavor(null) == null, "selectBestTextFlavor(null) null");
        check(java.awt.datatransfer.DataFlavor.selectBestTextFlavor(new java.awt.datatransfer.DataFlavor[0]) == null, "selectBestTextFlavor(empty) null");
        check(java.awt.datatransfer.DataFlavor.selectBestTextFlavor(new java.awt.datatransfer.DataFlavor[]{ java.awt.datatransfer.DataFlavor.imageFlavor }) == null, "selectBestTextFlavor(no text) null");
        java.awt.datatransfer.DataFlavor best = java.awt.datatransfer.DataFlavor.selectBestTextFlavor(new java.awt.datatransfer.DataFlavor[] {
                new java.awt.datatransfer.DataFlavor("text/plain; charset=us-ascii"),
                new java.awt.datatransfer.DataFlavor("text/plain; charset=utf-8") });
        check(best != null && best.getParameter("charset") != null, "selectBestTextFlavor picks a text flavor");

        // ---- getReaderForText ----
        final java.awt.datatransfer.DataFlavor utf8Stream = new java.awt.datatransfer.DataFlavor("text/plain; charset=UTF-8; class=java.io.InputStream");
        final byte[] utf8Bytes = "hi".getBytes("UTF-8");
        java.awt.datatransfer.Transferable t = new java.awt.datatransfer.Transferable() {
            public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() { return new java.awt.datatransfer.DataFlavor[]{ utf8Stream }; }
            public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor fl) { return utf8Stream.equals(fl); }
            public Object getTransferData(java.awt.datatransfer.DataFlavor fl) throws java.awt.datatransfer.UnsupportedFlavorException {
                if (!utf8Stream.equals(fl)) throw new java.awt.datatransfer.UnsupportedFlavorException(fl);
                return new ByteArrayInputStream(utf8Bytes);
            }
        };
        java.io.Reader rd = utf8Stream.getReaderForText(t);
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = rd.read()) != -1) sb.append((char) ch);
        check(sb.toString().equals("hi"), "getReaderForText decodes charset");
        boolean ufe = false;
        try { utf8Stream.getReaderForText(new java.awt.datatransfer.StringSelection("x")); }
        catch (java.awt.datatransfer.UnsupportedFlavorException x) { ufe = true; }
        check(ufe, "getReaderForText unsupported flavor throws UnsupportedFlavorException");
        final java.awt.Image img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.datatransfer.Transferable imgT = new java.awt.datatransfer.Transferable() {
            public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() { return new java.awt.datatransfer.DataFlavor[]{ java.awt.datatransfer.DataFlavor.imageFlavor }; }
            public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor fl) { return java.awt.datatransfer.DataFlavor.imageFlavor.equals(fl); }
            public Object getTransferData(java.awt.datatransfer.DataFlavor fl) { return img; }
        };
        boolean iaeReader = false;
        try { java.awt.datatransfer.DataFlavor.imageFlavor.getReaderForText(imgT); }
        catch (IllegalArgumentException x) { iaeReader = true; }
        check(iaeReader, "getReaderForText non-Reader/InputStream flavor throws IAE");

        // ---- text flavor constants ----
        check(java.awt.datatransfer.DataFlavor.selectionHtmlFlavor.isFlavorTextType(), "selectionHtmlFlavor is text");
        check(java.awt.datatransfer.DataFlavor.fragmentHtmlFlavor.isFlavorTextType(), "fragmentHtmlFlavor is text");
        check(java.awt.datatransfer.DataFlavor.allHtmlFlavor.isFlavorTextType(), "allHtmlFlavor is text");
        check("html".equals(java.awt.datatransfer.DataFlavor.selectionHtmlFlavor.getSubType()), "selectionHtmlFlavor sub type html");
        check(java.awt.datatransfer.DataFlavor.plainTextFlavor.isFlavorTextType(), "plainTextFlavor is text");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
