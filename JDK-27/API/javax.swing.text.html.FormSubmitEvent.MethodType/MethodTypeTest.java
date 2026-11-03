/*
 * @test
 * @summary javax.swing.text.html.FormSubmitEvent.MethodType conformance: white-box structure + black-box behavior.
 * @run main MethodTypeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.html.FormSubmitEvent;

public class MethodTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.FormSubmitEvent$MethodType");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == true, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 2, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("GET"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("POST"), "enum constant 1");
    }

    static void testBlackBox() throws Exception {
        FormSubmitEvent.MethodType[] vals = FormSubmitEvent.MethodType.values();
        check(vals.length == 2, "values length 2");
        check(vals[0] == FormSubmitEvent.MethodType.GET, "values[0] GET");
        check(vals[1] == FormSubmitEvent.MethodType.POST, "values[1] POST");

        check(FormSubmitEvent.MethodType.valueOf("GET") == FormSubmitEvent.MethodType.GET, "valueOf GET");
        check(FormSubmitEvent.MethodType.valueOf("POST") == FormSubmitEvent.MethodType.POST, "valueOf POST");

        check(FormSubmitEvent.MethodType.GET.ordinal() == 0, "GET ordinal 0");
        check(FormSubmitEvent.MethodType.POST.ordinal() == 1, "POST ordinal 1");
        check("GET".equals(FormSubmitEvent.MethodType.GET.name()), "GET name");
        check("POST".equals(FormSubmitEvent.MethodType.POST.name()), "POST name");

        boolean threw = false;
        try {
            FormSubmitEvent.MethodType.valueOf("NOPE");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check(threw, "valueOf invalid throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
