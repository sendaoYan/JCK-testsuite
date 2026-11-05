/*
 * @test
 * @summary jdk.dynalink.internal.InternalTypeUtilities conformance: internal class reflection.
 * @modules jdk.dynalink/jdk.dynalink.internal:+open
 * @run main InternalTypeUtilitiesTest
 */
public class InternalTypeUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepContract() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.internal.InternalTypeUtilities");
        // public class with static-only surface
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        for (java.lang.reflect.Constructor<?> ctor : c.getDeclaredConstructors())
            check(java.lang.reflect.Modifier.isPrivate(ctor.getModifiers()), "private constructor");
        // areAssignable is symmetric assignability over both references and primitives
        java.lang.reflect.Method areAssignable = c.getMethod("areAssignable", Class.class, Class.class);
        check((Boolean) areAssignable.invoke(null, String.class, CharSequence.class), "String assignable to CharSequence");
        check((Boolean) areAssignable.invoke(null, CharSequence.class, String.class), "supertype is assignable to itself");
        check((Boolean) areAssignable.invoke(null, String.class, Number.class) == false, "unrelated is not assignable");
        check((Boolean) areAssignable.invoke(null, int.class, Integer.class) == false, "no boxing");
        check((Boolean) areAssignable.invoke(null, int.class, long.class) == false, "no widening");
        // canReferenceDirectly: bootstrap-loader types are visible everywhere
        java.lang.reflect.Method canRef = c.getMethod("canReferenceDirectly", ClassLoader.class, ClassLoader.class);
        ClassLoader app = InternalTypeUtilitiesTest.class.getClassLoader();
        check((Boolean) canRef.invoke(null, app, app), "same loader");
        check((Boolean) canRef.invoke(null, app, null), "app-loaded code can reference bootstrap classes directly");
        check((Boolean) canRef.invoke(null, null, app) == false, "bootstrap-loaded code cannot see app classes directly");
        // getCommonLosslessConversionType
        java.lang.reflect.Method common = c.getMethod("getCommonLosslessConversionType", Class.class, Class.class);
        check(common.invoke(null, int.class, long.class) == long.class, "int/long -> long");
        check(common.invoke(null, long.class, int.class) == long.class, "order independent");
        check(common.invoke(null, Integer.class, String.class) == Object.class, "unrelated references -> Object");
        check(common.invoke(null, Integer.class, Number.class) == Number.class, "widest common supertype");
    }

    public static void main(String[] args) throws Throwable {
        Class<?> c = Class.forName("jdk.dynalink.internal.InternalTypeUtilities", false, InternalTypeUtilitiesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.dynalink.internal.InternalTypeUtilities"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("areAssignable;canReferenceDirectly;getCommonLosslessConversionType"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
