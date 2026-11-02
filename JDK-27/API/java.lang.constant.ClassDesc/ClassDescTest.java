/*
 * @test
 * @summary java.lang.constant.ClassDesc conformance: factories, descriptors,
 *          array/nested types, and resolution.
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

    public static void main(String[] args) throws Exception {
        check(ClassDesc.class.isInterface(), "ClassDesc is an interface");

        ClassDesc string = ClassDesc.of("java.lang.String");
        check("String".equals(string.displayName()), "displayName is simple name");
        check("Ljava/lang/String;".equals(string.descriptorString()), "descriptorString");
        check(!string.isArray() && !string.isPrimitive() && string.isClassOrInterface(),
              "String is class/interface");
        check("java.lang".equals(string.packageName()), "packageName");
        check(string.resolveConstantDesc(MethodHandles.lookup()) == String.class,
              "resolves to String.class");

        ClassDesc intPrim = ClassDesc.ofDescriptor("I");
        check(intPrim.isPrimitive(), "ofDescriptor(\"I\") is primitive");
        check("I".equals(intPrim.descriptorString()), "primitive int descriptor");

        ClassDesc iarr = intPrim.arrayType();
        check(iarr.isArray(), "int[] is array");
        check("I".equals(iarr.componentType().descriptorString()), "componentType");
        check("[I".equals(iarr.descriptorString()), "int[] descriptor");

        check("[[I".equals(intPrim.arrayType(2).descriptorString()),
              "arrayType(2) descriptor");

        ClassDesc nested = ClassDesc.of("java.util.Map").nested("Entry");
        check("Map$Entry".equals(nested.displayName()), "nested displayName");
        check("Ljava/util/Map$Entry;".equals(nested.descriptorString()), "nested descriptor");

        check(ClassDesc.ofDescriptor("Ljava/lang/Object;")
                  .resolveConstantDesc(MethodHandles.lookup()) == Object.class,
              "ofDescriptor resolves");
        check("Ljava/lang/String;".equals(
                ClassDesc.ofInternalName("java/lang/String").descriptorString()),
              "ofInternalName");
        check("Ljava/lang/Integer;".equals(
                ClassDesc.of("java.lang", "Integer").descriptorString()),
              "of(package,simple)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
