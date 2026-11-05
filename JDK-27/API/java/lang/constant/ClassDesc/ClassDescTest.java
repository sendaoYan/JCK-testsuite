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

    // ---- Part 3: deep contract (JDK-27 nominal descriptor arithmetic) ----
    static void testDeepContract() throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // of() does NOT intern: two calls yield distinct instances with value equality
        ClassDesc s1 = ClassDesc.of("java.lang.String");
        ClassDesc s2 = ClassDesc.of("java.lang.String");
        check(s1 != s2, "ClassDesc.of does not intern (distinct instances)");
        check(s1.equals(s2) && s1.hashCode() == s2.hashCode(),
              "non-interned instances still equals/hashCode by value");

        // cross-factory equivalence: of(fqn) == of(pkg, simple) == ofInternalName == ofDescriptor
        ClassDesc byDescriptor = ClassDesc.ofDescriptor("Ljava/lang/String;");
        check(s1.equals(byDescriptor), "ofDescriptor equals of");
        check(ClassDesc.ofInternalName("java/lang/String").equals(byDescriptor),
              "ofInternalName equals ofDescriptor");
        check(ClassDesc.ofDescriptor("[I").equals(ClassDesc.ofDescriptor("I").arrayType()),
              "ofDescriptor(\"[I\") equals I.arrayType()");
        check(ClassDesc.ofDescriptor("[[Ljava/lang/String;")
                      .equals(s1.arrayType().arrayType()),
              "nested array descriptor equals two arrayType() calls");

        // a primitive-like name is accepted by of() (produces Lint;), but resolution fails
        ClassDesc pseudoInt = ClassDesc.of("int");
        check("Lint;".equals(pseudoInt.descriptorString()),
              "of(\"int\") yields the reference descriptor Lint; (no factory-time rejection)");
        check(pseudoInt.isClassOrInterface(),
              "Lint; is classified as class/interface (not primitive)");
        check(!pseudoInt.isPrimitive(), "Lint; is not primitive");
        try {
            pseudoInt.resolveConstantDesc(lookup);
            check(false, "Lint; must not resolve");
        } catch (ClassNotFoundException expected) {
            check(true, "Lint; resolution fails with ClassNotFoundException");
        }

        // malformed descriptor matrix for ofDescriptor
        String[] badDescriptors = {"", "(broken", "V;", "L", "Ljava/lang/String", "[", "II"};
        for (String bad : badDescriptors) {
            try {
                ClassDesc.ofDescriptor(bad);
                check(false, "ofDescriptor(" + bad + ") must be rejected");
            } catch (IllegalArgumentException expected) {
                check(true, "ofDescriptor(" + bad + ") -> IllegalArgumentException");
            }
        }
        try { ClassDesc.ofDescriptor(null); check(false, "ofDescriptor(null) rejected"); }
        catch (NullPointerException expected) { check(true, "ofDescriptor(null) -> NPE"); }

        // array rank boundaries
        ClassDesc primInt = ClassDesc.ofDescriptor("I");
        try { primInt.arrayType(0); check(false, "arrayType(0) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "arrayType(0) -> IAE"); }
        try { primInt.arrayType(-1); check(false, "arrayType(-1) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "arrayType(-1) -> IAE"); }
        check(ClassDesc.ofDescriptor("I").arrayType(3).descriptorString().equals("[[[I"),
              "arrayType(3) rank three");
        // void can never be an array component
        try { ClassDesc.ofDescriptor("V").arrayType(); check(false, "void array rejected"); }
        catch (IllegalArgumentException expected) { check(true, "V.arrayType() -> IAE"); }
        // primitives cannot be componentType()'d further
        check(ClassDesc.ofDescriptor("I").componentType() == null,
              "primitive componentType() is null");

        // full descriptor round-trip: of -> arrayType -> componentType -> resolve
        ClassDesc arr = s1.arrayType();
        check(arr.componentType().equals(s1), "array/component inverse");
        check(arr.descriptorString().equals("[Ljava/lang/String;"), "array descriptor");
        try {
            check(arr.componentType().resolveConstantDesc(lookup) == String.class,
                  "component resolution closure");
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
