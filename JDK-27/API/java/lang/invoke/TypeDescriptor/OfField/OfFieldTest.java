/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor.OfField conformance: white-box + black-box behavior.
 * @run main OfFieldTest
 */

import java.lang.invoke.TypeDescriptor;
import java.lang.invoke.TypeDescriptor.OfField;

public class OfFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.TypeDescriptor$OfField");
        check(c.isInterface(), "is an interface");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == TypeDescriptor.class, "extends TypeDescriptor");
        for (String m : new String[]{"isArray", "isPrimitive", "componentType", "arrayType"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(Class.class.isAssignableFrom(Class.class)
              && OfField.class.isAssignableFrom(Class.class), "Class implements OfField");
        check(OfField.class.isAssignableFrom(
                      Class.forName("java.lang.constant.ClassDesc")),
              "ClassDesc implements OfField");
        check(OfField.class.isAssignableFrom(
                      Class.forName("java.lang.invoke.MethodType")) == false,
              "MethodType does not implement OfField");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Class as OfField: reference types
        @SuppressWarnings("unchecked")
        OfField<Class<?>> str = (OfField<Class<?>>) (Object) String.class;
        check(!str.isArray() && !str.isPrimitive(), "String is plain");
        check(str.componentType() == null, "componentType null for plain class");
        @SuppressWarnings("unchecked")
        OfField<Class<?>> strArr = (OfField<Class<?>>) (Object) String[].class;
        check(strArr.isArray() && !strArr.isPrimitive(), "String[] is array");
        check(strArr.componentType() == String.class, "componentType String");

        // arrayType() lifts one dimension
        check(str.arrayType() == String[].class, "arrayType String[]");
        check(strArr.arrayType() == String[][].class, "arrayType String[][]");

        // primitives
        @SuppressWarnings("unchecked")
        OfField<Class<?>> i = (OfField<Class<?>>) (Object) int.class;
        check(i.isPrimitive() && !i.isArray(), "int is primitive");
        check(i.componentType() == null, "primitive has no component");
        check(i.arrayType() == int[].class, "int arrayType");
        @SuppressWarnings("unchecked")
        OfField<Class<?>> v = (OfField<Class<?>>) (Object) void.class;
        check(v.isPrimitive(), "void counts as primitive");

        // descriptors chain through arrayType
        check(i.arrayType().descriptorString().equals("[I"), "array descriptor");
        check(strArr.descriptorString().equals("[Ljava/lang/String;"), "array ref descriptor");

        // ClassDesc as OfField behaves identically
        java.lang.constant.ClassDesc cd = java.lang.constant.ClassDesc.of("java.lang.String");
        @SuppressWarnings("unchecked")
        OfField<?> cdOf = (OfField<?>) cd;
        check(!cdOf.isArray() && !cdOf.isPrimitive(), "ClassDesc plain");
        OfField<?> cdArr = cdOf.arrayType();
        check(cdArr.isArray() && cdArr.descriptorString().equals("[Ljava/lang/String;"),
              "ClassDesc arrayType descriptor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
