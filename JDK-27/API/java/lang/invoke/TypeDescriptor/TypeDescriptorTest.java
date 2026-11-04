/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor conformance: white-box + black-box behavior, full descriptor matrix for Class/MethodType, descriptor round-trips and parse negatives.
 * @run main TypeDescriptorTest
 */

import java.lang.invoke.MethodType;
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

    // ---- helpers ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- Class descriptors: arrays, nesting, all primitives ----
    static void testClassDescriptorMatrix() {
        Class<?>[] cs = {
                byte.class, short.class, char.class, boolean.class, long.class,
                float.class, double.class, void.class,
                Object[].class, String[][].class, int[][][].class,
                java.util.Map.Entry.class, TypeDescriptorTest.class, Simple.class,
                new java.util.ArrayList<String>().getClass(),
        };
        String[] expect = {
                "B", "S", "C", "Z", "J", "F", "D", "V",
                "[Ljava/lang/Object;", "[[Ljava/lang/String;", "[[[I",
                "Ljava/util/Map$Entry;", "LTypeDescriptorTest;", "LTypeDescriptorTest$Simple;",
                "Ljava/util/ArrayList;",
        };
        for (int i = 0; i < cs.length; i++) {
            check(cs[i].descriptorString().equals(expect[i]),
                    expect[i] + " == " + cs[i].descriptorString());
        }
        check(int[].class.descriptorString().equals("[I"), "[I");
        check(String[].class.descriptorString().equals("[Ljava/lang/String;"),
                "[Ljava/lang/String;");
    }

    // ---- MethodType descriptors and round-trips ----
    static void testMethodTypeDescriptors() {
        MethodType[] mts = {
                MethodType.methodType(void.class),
                MethodType.methodType(void.class, int.class),
                MethodType.methodType(Object.class, int.class, String.class),
                MethodType.methodType(String.class, long.class, boolean.class),
                MethodType.methodType(int.class, int[].class, String[][].class),
        };
        String[] expect = {
                "()V", "(I)V", "(ILjava/lang/String;)Ljava/lang/Object;",
                "(JZ)Ljava/lang/String;", "([I[[Ljava/lang/String;)I",
        };
        ClassLoader cl = TypeDescriptorTest.class.getClassLoader();
        for (int i = 0; i < mts.length; i++) {
            check(mts[i].descriptorString().equals(expect[i]),
                    expect[i] + " == " + mts[i].descriptorString());
            check(MethodType.fromMethodDescriptorString(mts[i].descriptorString(), cl)
                    .equals(mts[i]), expect[i] + " round-trips");
        }
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodType.fromMethodDescriptorString("(Q)V", cl),
                "bad signature", "unknown primitive letter rejected");
        checkThrows(NullPointerException.class,
                () -> MethodType.fromMethodDescriptorString(null, cl),
                "null descriptor -> NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testClassDescriptorMatrix();
        testMethodTypeDescriptors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
