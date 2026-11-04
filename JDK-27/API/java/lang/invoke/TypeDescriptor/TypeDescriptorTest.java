/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor conformance: white-box + black-box behavior.
 * @run main TypeDescriptorTest
 */

import java.lang.invoke.TypeDescriptor;

public class TypeDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // a minimal user implementation of the contract
    static final class Simple implements TypeDescriptor {
        private final String desc;
        Simple(String desc) { this.desc = desc; }
        @Override public String descriptorString() { return desc; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.TypeDescriptor");
        check(c.isInterface(), "is an interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1 && ms[0].getName().equals("descriptorString"),
              "single abstract method descriptorString");
        check(ms[0].getReturnType() == String.class, "returns String");
        // Class and MethodType implement it through the OfField/OfMethod subinterfaces
        check(TypeDescriptor.class.isAssignableFrom(Class.class), "Class is a TypeDescriptor");
        check(TypeDescriptor.class.isAssignableFrom(
                      Class.forName("java.lang.invoke.MethodType")),
              "MethodType is a TypeDescriptor");
        check(c.getDeclaredMethods().length == 1, "no other methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Class produces field descriptors
        TypeDescriptor[] tds = {
                String.class, int.class, void.class, int[].class, String[].class};
        String[] expect = {
                "Ljava/lang/String;", "I", "V", "[I", "[Ljava/lang/String;"};
        for (int i = 0; i < tds.length; i++) {
            check(tds[i].descriptorString().equals(expect[i]),
                  expect[i] + ": " + tds[i].descriptorString());
        }

        // MethodType produces a method descriptor
        TypeDescriptor mt = java.lang.invoke.MethodType.methodType(
                boolean.class, String.class, int.class);
        check(mt.descriptorString().equals("(Ljava/lang/String;I)Z"),
              "MethodType descriptor");

        // user implementations honor the contract
        check(new Simple("LFoo;").descriptorString().equals("LFoo;"),
              "user implementation descriptorString");

        // usable as a type bound in generic code
        java.util.List<TypeDescriptor> list = java.util.List.of(String.class, int.class);
        boolean allValid = true;
        for (TypeDescriptor td : list) {
            String d = td.descriptorString();
            if (d.isEmpty()) allValid = false;
        }
        check(allValid, "descriptors are non-empty strings");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
