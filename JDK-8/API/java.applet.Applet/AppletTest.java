/*
 * @test
 * @summary java.applet.Applet conformance: class reflection.
 * @run main AppletTest
 */

public class AppletTest {
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
        Class<?> c = Class.forName("java.applet.Applet");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Panel"), "superclass java.awt.Panel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setStub"), "method setStub");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "getDocumentBase"), "method getDocumentBase");
        check(hasMethod(c, "getCodeBase"), "method getCodeBase");
        check(hasMethod(c, "getParameter"), "method getParameter");
        check(hasMethod(c, "getAppletContext"), "method getAppletContext");
        check(hasMethod(c, "resize"), "method resize");
        check(hasMethod(c, "isValidateRoot"), "method isValidateRoot");
        check(hasMethod(c, "showStatus"), "method showStatus");
        check(hasMethod(c, "getImage"), "method getImage");
        check(hasMethod(c, "newAudioClip"), "method newAudioClip");
        check(hasMethod(c, "getAudioClip"), "method getAudioClip");
        check(hasMethod(c, "getAppletInfo"), "method getAppletInfo");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "getParameterInfo"), "method getParameterInfo");
        check(hasMethod(c, "play"), "method play");
        check(hasMethod(c, "init"), "method init");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "stop"), "method stop");
        check(hasMethod(c, "destroy"), "method destroy");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
