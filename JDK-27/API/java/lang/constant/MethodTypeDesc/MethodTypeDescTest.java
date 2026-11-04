/*
 * @test
 * @summary java.lang.constant.MethodTypeDesc conformance: white-box + black-box behavior.
 * @run main MethodTypeDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class MethodTypeDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.MethodTypeDesc");
        check(c.isInterface(), "is an interface");
        check(Class.forName("java.lang.constant.ConstantDesc").isAssignableFrom(c),
              "extends ConstantDesc");
        check(Class.forName("java.lang.invoke.TypeDescriptor$OfMethod").isAssignableFrom(c),
              "extends TypeDescriptor.OfMethod");
        for (String m : new String[]{"ofDescriptor", "of", "returnType", "parameterCount",
                                      "parameterType", "parameterList", "parameterArray",
                                      "changeReturnType", "changeParameterType", "dropParameterTypes",
                                      "insertParameterTypes", "descriptorString", "displayDescriptor",
                                      "resolveConstantDesc"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(c.getDeclaredMethod("displayDescriptor").isDefault(), "displayDescriptor is default");
        check(!c.getDeclaredMethod("descriptorString").isDefault(), "descriptorString abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // of(returnType, paramTypes...) and accessors
        MethodTypeDesc mtd = MethodTypeDesc.of(ConstantDescs.CD_String,
                ConstantDescs.CD_int, ConstantDescs.CD_boolean);
        check(mtd.descriptorString().equals("(IZ)Ljava/lang/String;"), "descriptorString");
        check(mtd.parameterCount() == 2, "parameterCount");
        check(mtd.parameterType(0).descriptorString().equals("I"), "parameterType(0)");
        check(mtd.parameterType(1).descriptorString().equals("Z"), "parameterType(1)");
        check(mtd.returnType().descriptorString().equals("Ljava/lang/String;"), "returnType");
        check(mtd.parameterList().size() == 2
              && mtd.parameterList().get(0).descriptorString().equals("I"), "parameterList");
        check(mtd.parameterArray().length == 2, "parameterArray");
        check(mtd.displayDescriptor().equals("(int,boolean)String"), "displayDescriptor");

        // of(returnType, List) overload
        MethodTypeDesc fromList = MethodTypeDesc.of(ConstantDescs.CD_String,
                List.of(ConstantDescs.CD_int, ConstantDescs.CD_boolean));
        check(fromList.equals(mtd), "of(ret, List) equals of(ret, varargs)");

        // no-arg type
        MethodTypeDesc voidCall = MethodTypeDesc.of(ConstantDescs.CD_void);
        check(voidCall.descriptorString().equals("()V"), "()V");
        check(voidCall.parameterCount() == 0 && voidCall.parameterList().isEmpty(),
              "no parameters");
        check(voidCall.equals(ConstantDescs.MTD_void), "equals MTD_void");

        // ofDescriptor round trip
        check(MethodTypeDesc.ofDescriptor("(IZ)Ljava/lang/String;").equals(mtd),
              "ofDescriptor equals");
        check(MethodTypeDesc.ofDescriptor("()V").equals(ConstantDescs.MTD_void), "()V ofDescriptor");

        // resolution
        MethodType resolved = mtd.resolveConstantDesc(lookup);
        check(resolved == MethodType.methodType(String.class, int.class, boolean.class),
              "resolves to MethodType");
        check(voidCall.resolveConstantDesc(lookup)
                      == MethodType.methodType(void.class), "()V resolves");

        // mutation methods
        MethodTypeDesc changed = mtd.changeReturnType(ConstantDescs.CD_int);
        check(changed.descriptorString().equals("(IZ)I"), "changeReturnType");
        check(mtd.changeParameterType(0, ConstantDescs.CD_long).descriptorString()
                      .equals("(JZ)Ljava/lang/String;"), "changeParameterType");
        check(mtd.dropParameterTypes(0, 1).descriptorString().equals("(Z)Ljava/lang/String;"),
              "dropParameterTypes");
        check(mtd.insertParameterTypes(0, ConstantDescs.CD_long).descriptorString()
                      .equals("(JIZ)Ljava/lang/String;"), "insertParameterTypes");
        // original is unchanged (value objects)
        check(mtd.descriptorString().equals("(IZ)Ljava/lang/String;"), "mutations are pure");

        // value semantics
        check(mtd.equals(MethodTypeDesc.ofDescriptor("(IZ)Ljava/lang/String;")), "equals");
        check(mtd.hashCode() == MethodTypeDesc.ofDescriptor("(IZ)Ljava/lang/String;").hashCode(),
              "hashCode");
        check(!mtd.equals(voidCall), "!equals different shape");
        check(!mtd.equals(null) && !mtd.equals("(IZ)"), "!equals unrelated");

        // invalid descriptors rejected
        try { MethodTypeDesc.ofDescriptor("()"); check(false, "empty descriptor rejected"); }
        catch (IllegalArgumentException expected) { check(true, "ofDescriptor(\"\") IAE"); }
        try { MethodTypeDesc.ofDescriptor("(I"); check(false, "unterminated descriptor rejected"); }
        catch (IllegalArgumentException expected) { check(true, "unterminated IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
