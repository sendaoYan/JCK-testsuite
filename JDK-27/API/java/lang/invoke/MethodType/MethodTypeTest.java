/*
 * @test
 * @summary java.lang.invoke.MethodType conformance: white-box + black-box behavior.
 * @run main MethodTypeTest
 */

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

public class MethodTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodType");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(Class.forName("java.lang.constant.Constable").isAssignableFrom(c), "Constable");
        check(Class.forName("java.lang.invoke.TypeDescriptor$OfMethod").isAssignableFrom(c),
              "TypeDescriptor.OfMethod");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor (factory only)");
        String[] names = {"methodType", "genericMethodType", "fromMethodDescriptorString",
                "changeParameterType", "insertParameterTypes", "appendParameterTypes",
                "dropParameterTypes", "changeReturnType", "hasPrimitives", "hasWrappers",
                "erase", "generic", "wrap", "unwrap", "parameterType", "parameterCount",
                "returnType", "parameterList", "lastParameterType", "parameterArray",
                "toMethodDescriptorString", "descriptorString", "describeConstable",
                "equals", "hashCode", "toString"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // factory families agree; MethodType is interned
        MethodType a = MethodType.methodType(String.class, int.class, boolean.class);
        MethodType b = MethodType.methodType(String.class,
                List.of(int.class, boolean.class));
        MethodType d = MethodType.methodType(String.class,
                new Class<?>[]{int.class, boolean.class});
        check(a.equals(b) && a.equals(d), "factories agree");
        check(a == MethodType.methodType(String.class, int.class, boolean.class),
              "interned instances");
        check(MethodType.methodType(int.class, String.class)
                      == MethodType.methodType(int.class).appendParameterTypes(String.class),
              "appendParameterTypes equals factory");

        // accessors
        check(a.returnType() == String.class, "returnType");
        check(a.parameterCount() == 2, "parameterCount");
        check(a.parameterType(0) == int.class, "parameterType(0)");
        check(a.parameterList().equals(List.of(int.class, boolean.class)), "parameterList");
        check(a.lastParameterType() == boolean.class, "lastParameterType");
        check(a.parameterArray().length == 2, "parameterArray");
        check(a.toString().equals("(int,boolean)String"), "toString format");

        // one-param sugar: (Class, MethodType) copies the tail
        check(MethodType.methodType(void.class, a).toMethodDescriptorString()
                      .equals("(IZ)Ljava/lang/String;".replace("Ljava/lang/String;", "V")),
              "methodType(rtype, MethodType) reuses parameter list");
        check(MethodType.methodType(void.class).parameterCount() == 0, "void type no params");

        // genericMethodType
        MethodType g2 = MethodType.genericMethodType(2);
        check(g2.parameterCount() == 2 && g2.parameterType(0) == Object.class
              && g2.returnType() == Object.class, "genericMethodType(2)");
        MethodType gv = MethodType.genericMethodType(1, true);
        check(gv.parameterCount() == 2 && gv.lastParameterType() == Object[].class,
              "genericMethodType(1, true) appends Object[]");

        // transformations
        MethodType v = MethodType.methodType(void.class, String.class, int.class);
        check(v.changeReturnType(int.class).returnType() == int.class, "changeReturnType");
        check(v.changeParameterType(0, Object.class).parameterType(0) == Object.class,
              "changeParameterType");
        check(v.dropParameterTypes(0, 1).parameterCount() == 1, "dropParameterTypes");
        check(v.insertParameterTypes(0, long.class).parameterType(0) == long.class,
              "insertParameterTypes");
        check(v.toMethodDescriptorString().equals("(Ljava/lang/String;I)V"),
              "descriptor for (String,int)void");

        // primitive/wrapper classification and conversions
        check(v.hasPrimitives(), "hasPrimitives (void/int)");
        check(!MethodType.methodType(String.class, String.class).hasPrimitives(),
              "no primitives");
        check(MethodType.methodType(Integer.class, int.class).hasWrappers(), "hasWrappers");
        check(!MethodType.methodType(int.class).hasWrappers(), "no wrappers");
        MethodType refy = MethodType.methodType(String.class, String.class, int.class);
        check(refy.erase().toMethodDescriptorString().equals("(Ljava/lang/Object;I)Ljava/lang/Object;"),
              "erase: references -> Object, primitives kept");
        check(refy.generic().toMethodDescriptorString()
                      .equals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
              "generic: everything Object");
        check(MethodType.methodType(Integer.class, int.class).wrap()
                      .toMethodDescriptorString().equals("(Ljava/lang/Integer;)Ljava/lang/Integer;"),
              "wrap");
        check(MethodType.methodType(int.class, Integer.class).unwrap()
                      .toMethodDescriptorString().equals("(I)I"),
              "unwrap");
        check(MethodType.methodType(int.class, Integer.class).wrap().unwrap()
                      == MethodType.methodType(int.class, int.class),
              "wrap then unwrap");

        // descriptor round trip through fromMethodDescriptorString
        String desc = "(IZLjava/lang/String;)V";
        check(MethodType.fromMethodDescriptorString(desc, null).toMethodDescriptorString()
                      .equals(desc), "descriptor round trip");

        // describeConstable produces a resolvable nominal form
        Optional<java.lang.constant.MethodTypeDesc> nd = a.describeConstable();
        check(nd.isPresent(), "describeConstable present");
        check(nd.get().resolveConstantDesc(java.lang.invoke.MethodHandles.lookup()) == a,
              "nominal form resolves back");

        // equals/hashCode are value-based
        check(a.hashCode() == b.hashCode(), "hashCode equal for equal types");
        check(!a.equals(v), "!equals different shape");
        check(!a.equals(null) && !a.equals("(int,boolean)String"), "!equals unrelated");

        // invalid input
        try { MethodType.methodType(void.class, (List<Class<?>>) null); check(false, "null list rejected"); }
        catch (NullPointerException expected) { check(true, "null list NPE"); }
        try { a.parameterType(99); check(false, "index rejected"); }
        catch (IndexOutOfBoundsException expected) { check(true, "parameterType OOB"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
