/*
 * @test
 * @summary javax.naming.ldap.ControlFactory conformance: abstract class hierarchy and members.
 * @run main ControlFactoryTest
 */



import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.ControlFactory;
import javax.naming.ldap.LdapContext;
public class ControlFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.ControlFactory");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getControlInstance"), "has method getControlInstance");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dCfStaticDelegation();
        dCfFactoryProtocol();
        System.out.println("deep: ControlFactory contracts OK");
    }

    public static class DeepCtrlFactory extends ControlFactory {
        public static int calls;
        public Control getControlInstance(Control ctl) throws NamingException {
            calls++;
            if ("deep.magic".equals(ctl.getID()))
                return new BasicControl("deep.translated", true, null);
            return null; // not handled
        }
    }

    public static class DeepNullFactory extends ControlFactory {
        public Control getControlInstance(Control ctl) throws NamingException { return null; }
    }

    static void dCfStaticDelegation() throws Exception {
        check("java.naming.factory.control".equals(LdapContext.CONTROL_FACTORIES),
                "CONTROL_FACTORIES property name");
        BasicControl raw = new BasicControl("deep.magic", false, null);
        java.util.Hashtable<Object, Object> env = new java.util.Hashtable<>();
        env.put(LdapContext.CONTROL_FACTORIES, "ControlFactoryTest$DeepCtrlFactory");
        Control out = ControlFactory.getControlInstance(raw, null, env);
        check("deep.translated".equals(out.getID()), "factory consulted via env property");
        check(out.isCritical(), "factory result used verbatim");
        check(DeepCtrlFactory.calls == 1, "one invocation");
        // factory returning null -> original control returned unchanged
        env.put(LdapContext.CONTROL_FACTORIES, "ControlFactoryTest$DeepNullFactory");
        Control same = ControlFactory.getControlInstance(raw, null, env);
        check(same == raw, "null result -> original control");
        // no factories configured -> original control
        Control none = ControlFactory.getControlInstance(raw, null,
                new java.util.Hashtable<Object, Object>());
        check(none == raw, "no factories -> original control");
        // factory classes that cannot be located are skipped silently
        env.put(LdapContext.CONTROL_FACTORIES, "no.such.ControlFactory");
        Control missing = ControlFactory.getControlInstance(raw, null, env);
        check(missing == raw, "unknown factory class skipped, original returned");
    }

    static void dCfFactoryProtocol() throws Exception {
        DeepCtrlFactory f = new DeepCtrlFactory();
        Control handled = f.getControlInstance(new BasicControl("deep.magic"));
        check(handled != null && "deep.translated".equals(handled.getID()),
                "direct factory invocation");
        Control passthrough = f.getControlInstance(new BasicControl("other"));
        check(passthrough == null, "unhandled id -> null");
        // ControlFactory is abstract with exactly one abstract method
        check(java.lang.reflect.Modifier.isAbstract(ControlFactory.class.getModifiers()),
                "abstract class");
        int abstractCount = 0;
        for (java.lang.reflect.Method m : ControlFactory.class.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 1, "single abstract method");
    }

}
