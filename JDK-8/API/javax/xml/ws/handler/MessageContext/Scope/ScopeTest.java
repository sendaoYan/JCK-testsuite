/*
 * @test
 * @summary javax.xml.ws.handler.MessageContext.Scope conformance: exactly two enum constants (APPLICATION/HANDLER), valueOf/ordinal/name precision, enum mechanics and use in setScope.
 * @run main ScopeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;


public class ScopeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.MessageContext$Scope");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("APPLICATION"), "constant APPLICATION");
        check(cn.contains("HANDLER"), "constant HANDLER");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        testConstantsAndOrder();
        testEnumMechanics(c);
        testUseInMessageContext();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 常量与顺序：恰 APPLICATION, HANDLER（无第三个）
    static void testConstantsAndOrder() {
        Scope[] vs = Scope.values();
        check(vs.length == 2, "values() length exactly 2: " + vs.length);
        check(vs[0] == Scope.APPLICATION, "values()[0] is APPLICATION");
        check(vs[1] == Scope.HANDLER, "values()[1] is HANDLER");
        check(Scope.valueOf("APPLICATION") == Scope.APPLICATION, "valueOf(APPLICATION) identity");
        check(Scope.valueOf("HANDLER") == Scope.HANDLER, "valueOf(HANDLER) identity");
        check("APPLICATION".equals(Scope.APPLICATION.name()), "APPLICATION.name()");
        check("HANDLER".equals(Scope.HANDLER.name()), "HANDLER.name()");
        check(Scope.APPLICATION.toString().equals("APPLICATION"), "APPLICATION.toString");
        check(Scope.HANDLER.toString().equals("HANDLER"), "HANDLER.toString");
        check(Scope.APPLICATION.ordinal() == 0, "APPLICATION ordinal 0");
        check(Scope.HANDLER.ordinal() == 1, "HANDLER ordinal 1");
        // 无第三个常量：常见误值逐一排除
        for (Scope s : vs) {
            check(!"SYSTEM".equals(s.name()) && !"GLOBAL".equals(s.name()),
                  "no unexpected third constant name: " + s.name());
        }
        try {
            Scope.valueOf("APPLICATION2");
            check(false, "valueOf(unknown) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "valueOf(unknown) -> IllegalArgumentException");
        }
        try {
            Scope.valueOf(null);
            check(false, "valueOf(null) must throw");
        } catch (NullPointerException e) {
            check(true, "valueOf(null) -> NullPointerException");
        }
    }

    // 枚举机制：final、compareTo、getDeclaringClass、values() 每次新数组
    static void testEnumMechanics(Class<?> c) throws Exception {
        check(Modifier.isFinal(c.getModifiers()), "enum class is final (no constant bodies)");
        check(c == Scope.APPLICATION.getDeclaringClass(), "getDeclaringClass is MessageContext.Scope");
        check(Scope.APPLICATION.compareTo(Scope.HANDLER) < 0, "APPLICATION before HANDLER");
        check(Scope.HANDLER.compareTo(Scope.APPLICATION) > 0, "HANDLER after APPLICATION");
        check(Scope.APPLICATION.compareTo(Scope.APPLICATION) == 0, "compareTo self zero");
        check(Scope.values() != Scope.values(), "values() returns a fresh array each call");
        check(Enum.valueOf(Scope.class, "HANDLER") == Scope.HANDLER, "Enum.valueOf route");
        // 无自有字段（纯标记枚举；$VALUES 为合成字段需排除）
        int realFields = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (!f.isSynthetic()) realFields++;
        }
        check(realFields == 2, "exactly 2 declared fields (the constants): " + realFields);
        int ownMethods = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (!m.isSynthetic()) ownMethods++;
        }
        check(ownMethods == 2, "only compiler-generated values/valueOf declared: " + ownMethods);
    }

    // 与 MessageContext.setScope/getScope 配合的语义闭环
    static void testUseInMessageContext() {
        Map<Scope, Integer> seen = new HashMap<Scope, Integer>();
        seen.put(Scope.APPLICATION, 1);
        seen.put(Scope.HANDLER, 2);
        check(seen.size() == 2, "Scope usable as map key");
        // 规范语义：put 后默认 HANDLER，可切 APPLICATION
        javax.xml.ws.handler.MessageContext mc = new javax.xml.ws.handler.MessageContext() {
            private final Map<String, Object> m = new HashMap<String, Object>();
            private final Map<String, Scope> sc = new HashMap<String, Scope>();
            public void setScope(String k, Scope s) { sc.put(k, s); }
            public Scope getScope(String k) { return sc.containsKey(k) ? sc.get(k) : Scope.HANDLER; }
            public int size() { return m.size(); }
            public boolean isEmpty() { return m.isEmpty(); }
            public boolean containsKey(Object k) { return m.containsKey(k); }
            public boolean containsValue(Object v) { return m.containsValue(v); }
            public Object get(Object k) { return m.get(k); }
            public Object put(String k, Object v) { return m.put(k, v); }
            public Object remove(Object k) { return m.remove(k); }
            public void putAll(Map<? extends String, ? extends Object> mm) { m.putAll(mm); }
            public void clear() { m.clear(); }
            public java.util.Set<String> keySet() { return m.keySet(); }
            public java.util.Collection<Object> values() { return m.values(); }
            public java.util.Set<Map.Entry<String, Object>> entrySet() { return m.entrySet(); }
        };
        mc.put("k", "v");
        check(mc.getScope("k") == Scope.HANDLER, "default scope HANDLER via enum");
        mc.setScope("k", Scope.APPLICATION);
        check(mc.getScope("k") == Scope.APPLICATION, "switched scope APPLICATION via enum");
        // APPLICATION 与 HANDLER 互为仅有的两个取值
        check(mc.getScope("k") == Scope.values()[0], "APPLICATION is values()[0]");
    }
}
