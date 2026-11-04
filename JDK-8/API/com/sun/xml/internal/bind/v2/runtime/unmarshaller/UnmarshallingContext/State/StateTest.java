/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext.State conformance: class hierarchy and members.
 * @run main StateTest
 */

public class StateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext$State", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext$State"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBackup"), "has method getBackup");
        check(mn.contains("getContext"), "has method getContext");
        check(mn.contains("getElementDefaultValue"), "has method getElementDefaultValue");
        check(mn.contains("getLoader"), "has method getLoader");
        check(mn.contains("getPrev"), "has method getPrev");
        check(mn.contains("getTarget"), "has method getTarget");
        check(mn.contains("isMixed"), "has method isMixed");
        check(mn.contains("isNil"), "has method isNil");
        check(mn.contains("pop"), "has method pop");
        check(mn.contains("push"), "has method push");
        check(mn.contains("setBackup"), "has method setBackup");
        check(mn.contains("setElementDefaultValue"), "has method setElementDefaultValue");
        check(mn.contains("setIntercepter"), "has method setIntercepter");
        check(mn.contains("setLoader"), "has method setLoader");
        check(mn.contains("setNil"), "has method setNil");
        check(mn.contains("setReceiver"), "has method setReceiver");
        check(mn.contains("setTarget"), "has method setTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
