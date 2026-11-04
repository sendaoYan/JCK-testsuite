/*
 * @test
 * @summary jdk.internal.loader.URLClassPath conformance: internal package reflection.
 * @modules java.base/jdk.internal.loader:+open
 * @run main URLClassPathTest
 */
public class URLClassPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.loader.URLClassPath", false, URLClassPathTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addFile"), "method addFile");
        check(hasMethod(c, "addURL"), "method addURL");
        check(hasMethod(c, "closeLoaders"), "method closeLoaders");
        check(hasMethod(c, "findResource"), "method findResource");
        check(hasMethod(c, "findResources"), "method findResources");
        check(hasMethod(c, "getResource"), "method getResource");
        check(hasMethod(c, "getResources"), "method getResources");
        check(hasMethod(c, "getURLs"), "method getURLs");
        check(hasField(c, "DEBUG"), "field DEBUG");
        check(hasField(c, "DEBUG_CP_URL_CHECK"), "field DEBUG_CP_URL_CHECK");
        check(hasField(c, "DISABLE_CP_URL_CHECK"), "field DISABLE_CP_URL_CHECK");
        check(hasField(c, "JAR_CHECKING_ENABLED"), "field JAR_CHECKING_ENABLED");
        check(hasField(c, "JAVA_VERSION"), "field JAVA_VERSION");
        check(hasField(c, "JNUA"), "field JNUA");
        check(hasField(c, "USER_AGENT_JAVA_VERSION"), "field USER_AGENT_JAVA_VERSION");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "jarHandler"), "field jarHandler");
        check(hasField(c, "lmap"), "field lmap");
        check(hasField(c, "loaders"), "field loaders");
        check(hasField(c, "manifestClassPath"), "field manifestClassPath");
        check(hasField(c, "nextURL"), "field nextURL");
        check(hasField(c, "searchPath"), "field searchPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
