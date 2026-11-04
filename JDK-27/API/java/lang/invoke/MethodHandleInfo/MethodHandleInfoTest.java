/*
 * @test
 * @summary java.lang.invoke.MethodHandleInfo conformance: white-box + black-box behavior.
 * @run main MethodHandleInfoTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class MethodHandleInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int sample(int x) { return x; }
    static String format(String f, Object... args) { return String.format(f, args); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandleInfo");
        check(c.isInterface(), "is an interface");
        // the nine JVMS reference kinds
        int[] expect = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        String[] fields = {"REF_getField", "REF_getStatic", "REF_putField", "REF_putStatic",
                "REF_invokeVirtual", "REF_invokeStatic", "REF_invokeSpecial",
                "REF_newInvokeSpecial", "REF_invokeInterface"};
        for (int i = 0; i < fields.length; i++) {
            check(c.getField(fields[i]).getInt(null) == expect[i], fields[i] + " == " + expect[i]);
        }
        String[] names = {"getReferenceKind", "getDeclaringClass", "getName", "getMethodType",
                "reflectAs", "getModifiers", "isVarArgs", "referenceKindToString", "toString"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        check(c.getDeclaredMethod("isVarArgs").isDefault(), "isVarArgs is default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // a static method handle round-trips through revealDirect
        MethodHandle mh = lookup.findStatic(MethodHandleInfoTest.class, "sample",
                MethodType.methodType(int.class, int.class));
        MethodHandleInfo info = lookup.revealDirect(mh);
        check(info.getReferenceKind() == MethodHandleInfo.REF_invokeStatic, "static kind");
        check(info.getDeclaringClass() == MethodHandleInfoTest.class, "declaring class");
        check(info.getName().equals("sample"), "name");
        check(info.getMethodType().toMethodDescriptorString().equals("(I)I"), "method type");
        check(java.lang.reflect.Modifier.isStatic(info.getModifiers())
              && !java.lang.reflect.Modifier.isPublic(info.getModifiers()),
              "modifiers reflect the member (static, package-private)");
        check(!info.isVarArgs(), "not varargs");

        // a virtual method handle reports REF_invokeVirtual and its declaring class
        MethodHandle len = lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        MethodHandleInfo lenInfo = lookup.revealDirect(len);
        check(lenInfo.getReferenceKind() == MethodHandleInfo.REF_invokeVirtual, "virtual kind");
        check(lenInfo.getDeclaringClass() == String.class, "String declares length");
        check(lenInfo.getName().equals("length"), "virtual name");

        // varargs detection
        MethodHandle fmt = lookup.findStatic(MethodHandleInfoTest.class, "format",
                MethodType.methodType(String.class, String.class, Object[].class));
        check(lookup.revealDirect(fmt).isVarArgs(), "varargs detected");

        // reflectAs yields an equivalent reflective Member
        Method reflected = info.reflectAs(Method.class, lookup);
        check(reflected.getName().equals("sample"), "reflectAs name");
        check(reflected.invoke(null, 21).equals(21), "reflectAs usable");

        // toString(int, Class, String, MethodType) formats like a member reference
        String s = MethodHandleInfo.toString(MethodHandleInfo.REF_invokeStatic,
                MethodHandleInfoTest.class, "sample",
                MethodType.methodType(int.class, int.class));
        check(s.contains("MethodHandleInfoTest") && s.contains("sample"), "static toString: " + s);

        // referenceKindToString maps each kind to its name
        String[] kindNames = {"getField", "getStatic", "putField", "putStatic",
                "invokeVirtual", "invokeStatic", "invokeSpecial", "newInvokeSpecial",
                "invokeInterface"};
        for (int i = 1; i <= 9; i++) {
            check(MethodHandleInfo.referenceKindToString(i).equals(kindNames[i - 1]),
                  "referenceKindToString(" + i + ")");
        }
        try {
            MethodHandleInfo.referenceKindToString(0);
            check(false, "kind 0 rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "referenceKindToString(0) IAE");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
