/*
 * @test
 * @summary java.beans.PropertyEditorManager conformance: registerEditor/findEditor.
 * @run main PropertyEditorManagerTest
 */

import java.beans.*;

public class PropertyEditorManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class StringEditor extends PropertyEditorSupport {}

    public static void main(String[] args) throws Exception {
        PropertyEditorManager.registerEditor(String.class, StringEditor.class);
        PropertyEditor e = PropertyEditorManager.findEditor(String.class);
        check(e != null && e instanceof StringEditor, "findEditor");
        check(PropertyEditorManager.getEditorSearchPath() != null, "search path");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
