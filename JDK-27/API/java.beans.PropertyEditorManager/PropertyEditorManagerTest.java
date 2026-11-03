/*
 * @test
 * @summary java.beans.PropertyEditorManager conformance: white-box structure + black-box behavior.
 * @run main PropertyEditorManagerTest
 */

import java.beans.PropertyEditorManager;

public class PropertyEditorManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Custom {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyEditorManager");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        check(PropertyEditorManager.findEditor(Custom.class) == null, "no default editor for custom type");
        check(PropertyEditorManager.findEditor(String.class) != null, "String has default editor");

        PropertyEditorManager.registerEditor(Custom.class, java.beans.PropertyEditorSupport.class);
        check(PropertyEditorManager.findEditor(Custom.class) instanceof java.beans.PropertyEditorSupport, "registered editor found");
        PropertyEditorManager.registerEditor(Custom.class, null);
        check(PropertyEditorManager.findEditor(Custom.class) == null, "unregistered");

        String[] path = PropertyEditorManager.getEditorSearchPath();
        check(path != null, "searchPath non-null");
        String[] custom = new String[]{"pkg1", "pkg2"};
        PropertyEditorManager.setEditorSearchPath(custom);
        check(java.util.Arrays.equals(custom, PropertyEditorManager.getEditorSearchPath()), "setEditorSearchPath");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
