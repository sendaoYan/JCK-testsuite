/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.Loader conformance: abstract class hierarchy and members.
 * @run main LoaderTest
 */

public class LoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Loader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Loader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("childElement"), "has method childElement");
        check(mn.contains("computeExpectedElements"), "has method computeExpectedElements");
        check(mn.contains("expectText"), "has method expectText");
        check(mn.contains("fireAfterUnmarshal"), "has method fireAfterUnmarshal");
        check(mn.contains("fireBeforeUnmarshal"), "has method fireBeforeUnmarshal");
        check(mn.contains("getExpectedAttributes"), "has method getExpectedAttributes");
        check(mn.contains("getExpectedChildElements"), "has method getExpectedChildElements");
        check(mn.contains("handleGenericError"), "has method handleGenericError");
        check(mn.contains("handleGenericException"), "has method handleGenericException");
        check(mn.contains("handleParseConversionException"), "has method handleParseConversionException");
        check(mn.contains("leaveElement"), "has method leaveElement");
        check(mn.contains("reportError"), "has method reportError");
        check(mn.contains("reportUnexpectedChildElement"), "has method reportUnexpectedChildElement");
        check(mn.contains("startElement"), "has method startElement");
        check(mn.contains("text"), "has method text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
