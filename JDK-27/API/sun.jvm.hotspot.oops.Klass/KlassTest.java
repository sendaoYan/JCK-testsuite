/*
 * @test
 * @summary sun.jvm.hotspot.oops.Klass conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main KlassTest
 */
import java.lang.reflect.*;

public class KlassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.Klass", false, KlassTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.oops.Metadata".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.runtime.ClassConstants"), "interface sun.jvm.hotspot.runtime.ClassConstants");
        check(hasMethod(c, "signature"), "method signature");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getClassLoader"), "method getClassLoader");
        check(hasMethod(c, "isSubclassOf"), "method isSubclassOf");
        check(hasMethod(c, "getSymbol"), "method getSymbol");
        check(hasMethod(c, "getVtableLen"), "method getVtableLen");
        check(hasMethod(c, "getLayoutHelper"), "method getLayoutHelper");
        check(hasMethod(c, "iterateFields"), "method iterateFields");
        check(hasMethod(c, "getJavaMirror"), "method getJavaMirror");
        check(hasMethod(c, "getSuper"), "method getSuper");
        check(hasMethod(c, "arrayKlassOrNull"), "method arrayKlassOrNull");
        check(hasMethod(c, "arrayKlass"), "method arrayKlass");
        check(hasMethod(c, "arrayKlassImpl"), "method arrayKlassImpl");
        check(hasMethod(c, "getClassStatus"), "method getClassStatus");
        check(hasMethod(c, "getObjectSize"), "method getObjectSize");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "getJavaSuper"), "method getJavaSuper");
        check(hasMethod(c, "getClassLoaderData"), "method getClassLoaderData");
        check(hasMethod(c, "isSubtypeOf"), "method isSubtypeOf");
        check(hasMethod(c, "isKlass"), "method isKlass");
        check(hasMethod(c, "isArrayKlass"), "method isArrayKlass");
        check(hasMethod(c, "getSubklassKlass"), "method getSubklassKlass");
        check(hasMethod(c, "getNextSiblingKlass"), "method getNextSiblingKlass");
        check(hasMethod(c, "getNextLinkKlass"), "method getNextLinkKlass");
        check(hasMethod(c, "traceID"), "method traceID");
        check(hasMethod(c, "lca"), "method lca");
        check(hasField(c, "LH_INSTANCE_SLOW_PATH_BIT"), "field LH_INSTANCE_SLOW_PATH_BIT");
        check(hasField(c, "LH_LOG2_ELEMENT_SIZE_SHIFT"), "field LH_LOG2_ELEMENT_SIZE_SHIFT");
        check(hasField(c, "LH_ELEMENT_TYPE_SHIFT"), "field LH_ELEMENT_TYPE_SHIFT");
        check(hasField(c, "LH_HEADER_SIZE_SHIFT"), "field LH_HEADER_SIZE_SHIFT");
        check(hasField(c, "LH_ARRAY_TAG_SHIFT"), "field LH_ARRAY_TAG_SHIFT");
        check(hasField(c, "LH_ARRAY_TAG_TYPE_VALUE"), "field LH_ARRAY_TAG_TYPE_VALUE");
        check(hasField(c, "LH_ARRAY_TAG_OBJ_VALUE"), "field LH_ARRAY_TAG_OBJ_VALUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
