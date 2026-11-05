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

    // ---- Part 3: deep contract (descriptor parsing matrix + MethodType agreement) ----
    static void testDeepContract() throws Exception {
        // malformed descriptor matrix: each is rejected with IllegalArgumentException
        String[] bad = {"VI", "()X", "(I;)V", "Ljava/lang/String", "(IX)V", "()", "(V)V", "I"};
        for (String d : bad) {
            try {
                MethodTypeDesc.ofDescriptor(d);
                check(false, "ofDescriptor(" + d + ") must be rejected");
            } catch (IllegalArgumentException expected) {
                check(true, "ofDescriptor(" + d + ") -> IAE");
            }
        }
        // null matrix
        try { MethodTypeDesc.ofDescriptor(null); check(false, "ofDescriptor(null) rejected"); }
        catch (NullPointerException expected) { check(true, "ofDescriptor(null) -> NPE"); }
        try { MethodTypeDesc.of(null); check(false, "of(null return) rejected"); }
        catch (NullPointerException expected) { check(true, "of(null return) -> NPE"); }
        try { MethodTypeDesc.of(ConstantDescs.CD_void, (ClassDesc[]) null); check(false, "of(ret, null) rejected"); }
        catch (NullPointerException expected) { check(true, "of(ret, null array) -> NPE"); }

        // index bounds on parameter surgery
        MethodTypeDesc m = MethodTypeDesc.ofDescriptor("(IZ)V");
        try { m.dropParameterTypes(5, 6); check(false, "drop out of range rejected"); }
        catch (IndexOutOfBoundsException expected) { check(true, "drop(5,6) -> IndexOutOfBounds"); }
        try { m.insertParameterTypes(9, ConstantDescs.CD_int); check(false, "insert out of range rejected"); }
        catch (IndexOutOfBoundsException expected) { check(true, "insert(9) -> IndexOutOfBounds"); }
        try { m.changeParameterType(5, ConstantDescs.CD_int); check(false, "change out of range rejected"); }
        catch (IndexOutOfBoundsException expected) { check(true, "changeParameterType(5) -> IndexOutOfBounds"); }
        try { m.parameterType(5); check(false, "parameterType out of range rejected"); }
        catch (IndexOutOfBoundsException expected) { check(true, "parameterType(5) -> IndexOutOfBounds"); }

        // descriptorString <-> MethodType.fromMethodDescriptorString agreement (round-trip table)
        String[] descriptors = {
                "()V", "(I)V", "(IJZ)V", "(Ljava/lang/String;I)Ljava/lang/Object;",
                "([Ljava/lang/String;)[I", "(Ljava/lang/Object;Ljava/lang/Object;)Z",
        };
        for (String d : descriptors) {
            MethodTypeDesc mtd = MethodTypeDesc.ofDescriptor(d);
            MethodType mt = MethodType.fromMethodDescriptorString(d, null);
            check(mt.toMethodDescriptorString().equals(d),
                  "MethodType round-trip preserves " + d);
            check(mtd.resolveConstantDesc(MethodHandles.lookup()).toMethodDescriptorString().equals(d),
                  "MethodTypeDesc resolves to an equal MethodType for " + d);
            check(mtd.descriptorString().equals(d), "descriptorString exact for " + d);
        }

        // surgery results stay consistent with MethodType surgery
        MethodTypeDesc two = MethodTypeDesc.ofDescriptor("(IZ)V");
        check(two.dropParameterTypes(0, 0).descriptorString().equals("(IZ)V"),
              "drop(0,0) is a no-op");
        check(two.insertParameterTypes(2, ConstantDescs.CD_long).descriptorString().equals("(IZJ)V"),
              "insert at end");
        check(two.changeReturnType(ConstantDescs.CD_String).descriptorString().equals("(IZ)Ljava/lang/String;"),
              "changeReturnType to reference");
        check(two.parameterList().equals(List.of(ConstantDescs.CD_int, ConstantDescs.CD_boolean))
                      && two.parameterArray().length == 2,
              "parameterList/parameterArray agree");
        check(two.displayDescriptor().equals("(int,boolean)void"), "displayDescriptor");
        // array parameter display + parsing
        MethodTypeDesc arrP = MethodTypeDesc.ofDescriptor("([I)V");
        check(arrP.parameterType(0).isArray(), "[I parameter parses as array");
        check(arrP.displayDescriptor().equals("(int[])void"), "displayDescriptor uses [] form");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
