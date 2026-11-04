/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor.OfMethod conformance: white-box + black-box behavior.
 * @run main OfMethodTest
 */

import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;
import java.lang.invoke.TypeDescriptor.OfField;
import java.lang.invoke.TypeDescriptor.OfMethod;
import java.util.List;

public class OfMethodTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.TypeDescriptor$OfMethod");
        check(c.isInterface(), "is an interface");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == TypeDescriptor.class, "extends TypeDescriptor");
        for (String m : new String[]{"parameterCount", "parameterType", "returnType",
                "parameterArray", "parameterList", "changeReturnType", "changeParameterType",
                "dropParameterTypes", "insertParameterTypes"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(OfMethod.class.isAssignableFrom(MethodType.class),
              "MethodType implements OfMethod");
        check(OfMethod.class.isAssignableFrom(
                      Class.forName("java.lang.constant.MethodTypeDesc")),
              "MethodTypeDesc implements OfMethod");
        check(!OfMethod.class.isAssignableFrom(Class.class), "Class is not OfMethod");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // MethodType through its OfMethod view
        @SuppressWarnings("unchecked")
        OfMethod<Class<?>, MethodType> mt =
                (OfMethod<Class<?>, MethodType>) (Object) MethodType.methodType(
                        String.class, int.class, boolean.class);
        check(mt.parameterCount() == 2, "parameterCount");
        check(mt.parameterType(0) == int.class, "parameterType(0)");
        check(mt.returnType() == String.class, "returnType");
        check(mt.parameterList().equals(List.of(int.class, boolean.class)), "parameterList");
        check(mt.parameterArray().length == 2, "parameterArray");
        check(mt.descriptorString().equals("(IZ)Ljava/lang/String;"), "descriptorString");

        // transformations return new OfMethod views
        check(mt.changeReturnType(int.class).descriptorString().equals("(IZ)I"),
              "changeReturnType");
        check(mt.changeParameterType(0, long.class).descriptorString().equals("(JZ)Ljava/lang/String;"),
              "changeParameterType");
        check(mt.dropParameterTypes(0, 1).descriptorString().equals("(Z)Ljava/lang/String;"),
              "dropParameterTypes");
        check(mt.insertParameterTypes(0, Object.class).descriptorString()
                      .equals("(Ljava/lang/Object;IZ)Ljava/lang/String;"),
              "insertParameterTypes");

        // MethodTypeDesc behaves identically through the same interface
        java.lang.constant.MethodTypeDesc d =
                java.lang.constant.MethodTypeDesc.ofDescriptor("(IZ)Ljava/lang/String;");
        @SuppressWarnings("unchecked")
        OfMethod<?, ?> dv = (OfMethod<?, ?>) d;
        check(dv.parameterCount() == 2, "desc parameterCount");
        check(dv.descriptorString().equals("(IZ)Ljava/lang/String;"), "desc descriptor");
        @SuppressWarnings("unchecked")
        OfMethod<java.lang.constant.ClassDesc, ?> typed =
                (OfMethod<java.lang.constant.ClassDesc, ?>) (Object) d;
        check(typed.changeReturnType(java.lang.constant.ConstantDescs.CD_int)
                      .descriptorString().equals("(IZ)I"),
              "desc changeReturnType");

        // the views compose: describeConstable of MethodType is an OfMethod too
        java.util.Optional<java.lang.constant.MethodTypeDesc> nd =
                MethodType.methodType(void.class, long.class).describeConstable();
        check(nd.isPresent() && OfMethod.class.isAssignableFrom(nd.get().getClass()),
              "nominal form is an OfMethod");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
