/*
 * @test
 * @summary jdk.internal.jimage.ImageLocation conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main ImageLocationTest
 */
public class ImageLocationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageLocation", false, ImageLocationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "compress"), "method compress");
        check(hasMethod(c, "getBase"), "method getBase");
        check(hasMethod(c, "getBaseOffset"), "method getBaseOffset");
        check(hasMethod(c, "getCompressedSize"), "method getCompressedSize");
        check(hasMethod(c, "getContentOffset"), "method getContentOffset");
        check(hasMethod(c, "getExtension"), "method getExtension");
        check(hasMethod(c, "getExtensionOffset"), "method getExtensionOffset");
        check(hasMethod(c, "getFlags"), "method getFlags");
        check(hasMethod(c, "getFullName"), "method getFullName");
        check(hasMethod(c, "getModule"), "method getModule");
        check(hasMethod(c, "getModuleOffset"), "method getModuleOffset");
        check(hasMethod(c, "getPackageFlags"), "method getPackageFlags");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getParentOffset"), "method getParentOffset");
        check(hasMethod(c, "getPreviewFlags"), "method getPreviewFlags");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getUncompressedSize"), "method getUncompressedSize");
        check(hasMethod(c, "hasPreviewVersion"), "method hasPreviewVersion");
        check(hasMethod(c, "isPreviewOnly"), "method isPreviewOnly");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "verify"), "method verify");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "ATTRIBUTE_BASE"), "field ATTRIBUTE_BASE");
        check(hasField(c, "ATTRIBUTE_COMPRESSED"), "field ATTRIBUTE_COMPRESSED");
        check(hasField(c, "ATTRIBUTE_COUNT"), "field ATTRIBUTE_COUNT");
        check(hasField(c, "ATTRIBUTE_END"), "field ATTRIBUTE_END");
        check(hasField(c, "ATTRIBUTE_EXTENSION"), "field ATTRIBUTE_EXTENSION");
        check(hasField(c, "ATTRIBUTE_MODULE"), "field ATTRIBUTE_MODULE");
        check(hasField(c, "ATTRIBUTE_OFFSET"), "field ATTRIBUTE_OFFSET");
        check(hasField(c, "ATTRIBUTE_PARENT"), "field ATTRIBUTE_PARENT");
        check(hasField(c, "ATTRIBUTE_PREVIEW_FLAGS"), "field ATTRIBUTE_PREVIEW_FLAGS");
        check(hasField(c, "ATTRIBUTE_UNCOMPRESSED"), "field ATTRIBUTE_UNCOMPRESSED");
        check(hasField(c, "FLAGS_HAS_PREVIEW_VERSION"), "field FLAGS_HAS_PREVIEW_VERSION");
        check(hasField(c, "FLAGS_IS_PREVIEW_ONLY"), "field FLAGS_IS_PREVIEW_ONLY");
        check(hasField(c, "FLAGS_IS_PREVIEW_VERSION"), "field FLAGS_IS_PREVIEW_VERSION");
        check(hasField(c, "MODULES_PREFIX"), "field MODULES_PREFIX");
        check(hasField(c, "PACKAGES_PREFIX"), "field PACKAGES_PREFIX");
        check(hasField(c, "PREVIEW_INFIX"), "field PREVIEW_INFIX");
        check(hasField(c, "attributes"), "field attributes");
        check(hasField(c, "strings"), "field strings");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
