/*
 * @test
 * @summary java.lang.constant.ClassDesc conformance: white-box + black-box behavior.
 * @run main ClassDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;

public class ClassDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.ClassDesc");
        check(c.isInterface(), "is an interface");
        check(java.lang.constant.ConstantDesc.class.isAssignableFrom(c), "extends ConstantDesc");
        check(Class.forName("java.lang.invoke.TypeDescriptor$OfField").isAssignableFrom(c),
              "extends TypeDescriptor.OfField");
        for (String m : new String[]{"of", "ofInternalName", "ofDescriptor", "arrayType",
                                      "nested", "isArray", "isPrimitive", "isClassOrInterface",
                                      "componentType", "packageName", "displayName",
                                      "descriptorString", "resolveConstantDesc"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("of", String.class).getModifiers()),
              "of is static");
        check(c.getDeclaredMethod("isArray").isDefault(), "isArray is default");
        check(!c.getDeclaredMethod("descriptorString").isDefault(), "descriptorString is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // canonical class descriptor
        ClassDesc string = ClassDesc.of("java.lang.String");
        check("Ljava/lang/String;".equals(string.descriptorString()), "descriptorString");
        check("String".equals(string.displayName()), "displayName is simple name");
        check(!string.isArray() && !string.isPrimitive() && string.isClassOrInterface(),
              "String is class/interface");
        check("java.lang".equals(string.packageName()), "packageName");
        check(string.resolveConstantDesc(MethodHandles.lookup()) == String.class,
              "resolves to String.class");
        check(string.equals(ClassDesc.of("java.lang", "String")),
              "of(pkg, simple) equals of(fqn)");
        check(string.hashCode() == ClassDesc.of("java.lang.String").hashCode(), "hashCode by value");

        // default (empty) package
        ClassDesc defaultPkg = ClassDesc.of("", "String");
        check("LString;".equals(defaultPkg.descriptorString()), "empty package descriptor");
        check("".equals(defaultPkg.packageName()), "empty packageName");

        // internal-name form
        check(ClassDesc.ofInternalName("java/lang/String").equals(string),
              "ofInternalName equals of");

        // descriptor form, reference and primitive
        check(ClassDesc.ofDescriptor("Ljava/lang/String;").equals(string),
              "ofDescriptor(String) equals");
        ClassDesc intPrim = ClassDesc.ofDescriptor("I");
        check(intPrim.isPrimitive(), "ofDescriptor(\"I\") is primitive");
        check("I".equals(intPrim.descriptorString()), "primitive int descriptor");
        check("int".equals(intPrim.displayName()), "primitive displayName");
        check(!intPrim.isClassOrInterface() && !intPrim.isArray(), "primitive is neither");
        check("".equals(intPrim.packageName()), "primitive has empty package");

        // arrays
        ClassDesc iarr = intPrim.arrayType();
        check(iarr.isArray(), "int[] is array");
        check("I".equals(iarr.componentType().descriptorString()), "componentType");
        check("[I".equals(iarr.descriptorString()), "int[] descriptor");
        check("[[I".equals(intPrim.arrayType(2).descriptorString()), "int[][] descriptor");
        check(iarr.arrayType().componentType().equals(iarr), "component of [I[] is int[]");
        check(string.arrayType().resolveConstantDesc(MethodHandles.lookup()) == String[].class,
              "String[] resolves to String[].class");
        check(string.arrayType(2).resolveConstantDesc(MethodHandles.lookup()) == String[][].class,
              "String[][] resolves");
        check(!iarr.componentType().isArray(), "int is not an array");
        check(iarr.displayName().endsWith("[]"), "array displayName ends with []");

        // nested classes
        ClassDesc nested = string.nested("Map").nested("Entry");
        check("Ljava/lang/String$Map$Entry;".equals(nested.descriptorString()),
              "nested descriptor");
        check("java.lang".equals(nested.packageName()), "nested keeps package");
        check("String$Map".equals(string.nested("Map").displayName()),
              "nested displayName joins with $");
        check(string.nested("Map", "Entry", "Leaf").descriptorString()
                      .endsWith("String$Map$Entry$Leaf;"), "nested(String, String...)");

        // resolution failure is a ReflectiveOperationException
        try {
            ClassDesc.of("no.such.Clz").resolveConstantDesc(MethodHandles.lookup());
            check(false, "unresolvable class should throw");
        } catch (ClassNotFoundException e) {
            check(e.getMessage().contains("no.such.Clz"), "message names the class");
        }

        // bad names
        try { ClassDesc.of("java/lang.String"); check(false, "slash name rejected"); }
        catch (IllegalArgumentException expected) { check(true, "slash name IAE"); }
        try { ClassDesc.of(null); check(false, "null name rejected"); }
        catch (NullPointerException expected) { check(true, "null name NPE"); }
        try { ClassDesc.of("java.lang.", "X"); check(false, "trailing dot rejected"); }
        catch (IllegalArgumentException expected) { check(true, "trailing dot IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
