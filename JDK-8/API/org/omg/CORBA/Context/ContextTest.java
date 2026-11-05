/*
 * @test
 * @summary org.omg.CORBA.Context conformance: abstract class shape.
 * @run main ContextTest
 */

public class ContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Recording anonymous subclass: Context cannot be obtained from the offline ORB. */
    static class RecContext extends org.omg.CORBA.Context {
        String lastOneValueName;
        org.omg.CORBA.Any lastOneValueAny;
        org.omg.CORBA.NVList lastSetValues;
        String lastDeleteProp;
        String lastGetStart, lastGetPattern;
        int lastGetFlags = -1;
        String childName;
        String name = "ctx";
        org.omg.CORBA.Context parentCtx;

        public String context_name() { return name; }
        public org.omg.CORBA.Context parent() { return parentCtx; }
        public org.omg.CORBA.Context create_child(String child) { childName = child; return new RecContext(); }
        public void set_one_value(String propName, org.omg.CORBA.Any value) {
            lastOneValueName = propName; lastOneValueAny = value;
        }
        public void set_values(org.omg.CORBA.NVList values) { lastSetValues = values; }
        public void delete_values(String propname) { lastDeleteProp = propname; }
        public org.omg.CORBA.NVList get_values(String startScope, int flags, String pattern) {
            lastGetStart = startScope; lastGetFlags = flags; lastGetPattern = pattern;
            return null; // caller asserts the null passes through
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.Context");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        try {
            new org.omg.CORBA.Context() {
                public String context_name() { return null; }
                public org.omg.CORBA.Context parent() { return null; }
                public org.omg.CORBA.Context create_child(String s) { return null; }
                public void set_one_value(String n, org.omg.CORBA.Any v) { }
                public void set_values(org.omg.CORBA.NVList l) { }
                public void delete_values(String p) { }
                public org.omg.CORBA.NVList get_values(String s, int f, String p) { return null; }
            };
            check(true, "public no-arg ctor allows subclassing");
        } catch (Throwable t) {
            throw new AssertionError("subclassing Context failed: " + t, t);
        }

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);

        // ---- channel through the subclass: method invoked, args passed through, return passed back ----
        RecContext ctx = new RecContext();
        check("ctx".equals(ctx.context_name()), "context_name channel");

        RecContext parent = new RecContext();
        parent.name = "parent";
        ctx.parentCtx = parent;
        check(ctx.parent() == parent, "parent channel passes same reference");

        org.omg.CORBA.Any any = orb.create_any();
        any.insert_string("v");
        ctx.set_one_value("k", any);
        check("k".equals(ctx.lastOneValueName), "set_one_value records name");
        check(ctx.lastOneValueAny == any, "set_one_value passes same Any");
        check(ctx.lastOneValueAny.extract_string().equals("v"), "Any content intact");

        org.omg.CORBA.NVList nl = orb.create_list(1);
        nl.add_value("n", any, org.omg.CORBA.ARG_IN.value);
        ctx.set_values(nl);
        check(ctx.lastSetValues == nl, "set_values passes same NVList");
        check(ctx.lastSetValues.count() == 1, "NVList content intact through channel");

        ctx.delete_values("k*");
        check("k*".equals(ctx.lastDeleteProp), "delete_values passes pattern");

        org.omg.CORBA.NVList ret = ctx.get_values("", org.omg.CORBA.CTX_RESTRICT_SCOPE.value, "k");
        check(ret == null, "get_values return passes through unchanged");
        check("".equals(ctx.lastGetStart), "get_values startScope arg");
        check(ctx.lastGetFlags == org.omg.CORBA.CTX_RESTRICT_SCOPE.value, "get_values flags arg");
        check("k".equals(ctx.lastGetPattern), "get_values pattern arg");

        org.omg.CORBA.Context child = ctx.create_child("child");
        check("child".equals(ctx.childName), "create_child records name");
        check(child != null && child instanceof RecContext, "create_child returns our subclass instance");

        // ---- implementation fact: offline ORB has no default context (jdk8u372) ----
        try {
            orb.get_default_context();
            throw new AssertionError("get_default_context() should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(true, "ORB.get_default_context() offline -> NO_IMPLEMENT");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
