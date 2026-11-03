/*
 * @test
 * @summary java.awt.datatransfer.DataFlavor conformance: white-box structure + black-box behavior.
 * @run main DataFlavorTest
 */

import java.lang.reflect.*;

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
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.DataFlavor str = java.awt.datatransfer.DataFlavor.stringFlavor;
        check(str.getRepresentationClass() == String.class, "stringFlavor representation String");
        check(str.isFlavorTextType(), "stringFlavor is text");
        check(str.isMimeTypeSerializedObject(), "stringFlavor serialized mime");
        java.awt.datatransfer.DataFlavor f = new java.awt.datatransfer.DataFlavor("text/plain; charset=utf-8");
        check(f.getMimeType().startsWith("text/plain"), "getMimeType");
        check("text".equals(f.getPrimaryType()), "getPrimaryType");
        check("plain".equals(f.getSubType()), "getSubType");
        check("utf-8".equals(f.getParameter("charset")), "getParameter charset");
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
