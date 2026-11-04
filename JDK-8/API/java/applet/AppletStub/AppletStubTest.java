/*
 * @test
 * @summary java.applet.AppletStub conformance: interface reflection.
 * @run main AppletStubTest
 */

public class AppletStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.applet.AppletStub");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "getDocumentBase"), "method getDocumentBase");
        check(hasMethod(c, "getCodeBase"), "method getCodeBase");
        check(hasMethod(c, "getParameter"), "method getParameter");
        check(hasMethod(c, "getAppletContext"), "method getAppletContext");
        check(hasMethod(c, "appletResize"), "method appletResize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
