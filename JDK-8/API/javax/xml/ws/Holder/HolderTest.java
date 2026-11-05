/*
 * @test
 * @summary javax.xml.ws.Holder conformance: value wrapper, identity equality semantics, serialization round-trip.
 * @run main HolderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.xml.ws.Holder;

public class HolderTest {
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
        testBasics();
        testEqualitySemantics();
        testSerialization();
        testStructure();
        testOutParamSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testBasics() throws Exception {
        javax.xml.ws.Holder<String> h = new javax.xml.ws.Holder<String>("x");
        check("x".equals(h.value), "value field");
        h.value = "y";
        check("y".equals(h.value), "set value");
        check(new javax.xml.ws.Holder<String>().value == null, "default null");
        // 追加：public 可变字段语义（OUT/IN-OUT 参数容器）
        Holder<String> a = new Holder<String>("a");
        check(a.value.equals("a"), "ctor arg stored in public field");
        a.value = "b";
        check("b".equals(a.value), "public field directly mutable");
        Holder<Integer> i = new Holder<Integer>(7);
        check(Integer.valueOf(7).equals(i.value), "Integer value");
        i.value = 8;
        check(Integer.valueOf(8).equals(i.value), "Integer replaced (box swap)");
        Holder<Object> o = new Holder<Object>(null);
        check(o.value == null, "explicit null stored");
        o.value = "s";
        check("s".equals(o.value), "null -> value transition");
        Holder<int[]> arr = new Holder<int[]>(new int[]{1, 2});
        check(arr.value.length == 2 && arr.value[0] == 1 && arr.value[1] == 2, "array value");
        // 原始类型 int 不可作 T（编译层）；包装类型链表值
        Holder<java.util.List<String>> list = new Holder<java.util.List<String>>(
            new java.util.ArrayList<String>());
        list.value.add("e");
        check(list.value.size() == 1 && list.value.get(0).equals("e"), "mutable payload visible through field");
        // null Holder 引用与 null value 区分
        Holder<String> nh = null;
        try {
            nh.value = "z";
            check(false, "null holder reference must NPE");
        } catch (NullPointerException e) {
            check(true, "null holder reference NPE at field access");
        }
    }

    // 实测锁定：Holder 未覆写 equals/hashCode —— 恒等语义（非值语义）
    static void testEqualitySemantics() {
        Holder<String> h1 = new Holder<String>("v");
        Holder<String> h2 = new Holder<String>("v");
        check(h1.equals(h1), "reflexive equals");
        check(!h1.equals(h2), "equal-valued distinct holders NOT equal (identity semantics, RI locked)");
        check(h1.hashCode() == System.identityHashCode(h1), "hashCode is identity-based (not overridden)");
        check(!h1.equals(new Object()), "not equal to unrelated object");
        check(!h1.equals(null), "equals(null) false");
        Holder<String> h3 = h1;
        check(h3.equals(h1), "alias reference equal");
        check(h3.hashCode() == h1.hashCode(), "alias same hashCode");
        Holder<Integer> hi = new Holder<Integer>(7);
        Holder<String> hs = new Holder<String>("7");
        check(!hi.equals(hs), "cross-type holders never equal (identity)");
        check((Object) hi != (Object) hs, "distinct instances");
    }

    // Serializable round-trip：writeObject -> readObject，value 复活
    static void testSerialization() throws Exception {
        Holder<String> orig = new Holder<String>("ser");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        byte[] bytes = bos.toByteArray();
        check(bytes.length > 0, "serialized bytes non-empty");
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object back = ois.readObject();
        ois.close();
        check(back instanceof Holder, "deserialized is Holder");
        @SuppressWarnings("unchecked")
        Holder<String> h = (Holder<String>) back;
        check("ser".equals(h.value), "String value resurrected via serialization");
        check(back != orig, "deserialized is a distinct instance");

        // 非 Serializable 载体：Object 持有不可序列化对象 → 序列化抛异常（字段透传契约）
        Holder<Object> bad = new Holder<Object>(new Object());
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ObjectOutputStream o2 = new ObjectOutputStream(b2);
        try {
            o2.writeObject(bad);
            check(false, "non-serializable payload must throw");
        } catch (java.io.NotSerializableException e) {
            check(true, "non-serializable payload -> NotSerializableException");
        }
        o2.close();

        // null value 序列化往返
        Holder<String> nul = new Holder<String>(null);
        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        ObjectOutputStream o3 = new ObjectOutputStream(b3);
        o3.writeObject(nul);
        o3.close();
        @SuppressWarnings("unchecked")
        Holder<String> h4 = (Holder<String>) new ObjectInputStream(
            new ByteArrayInputStream(b3.toByteArray())).readObject();
        check(h4.value == null, "null value survives round-trip");

        // 可变值序列化后再改不影响原对象
        java.util.List<String> shared = new java.util.ArrayList<String>();
        shared.add("a");
        Holder<java.util.List<String>> lh = new Holder<java.util.List<String>>(shared);
        ByteArrayOutputStream b5 = new ByteArrayOutputStream();
        ObjectOutputStream o5 = new ObjectOutputStream(b5);
        o5.writeObject(lh);
        o5.close();
        @SuppressWarnings("unchecked")
        Holder<java.util.List<String>> lh2 = (Holder<java.util.List<String>>) new ObjectInputStream(
            new ByteArrayInputStream(b5.toByteArray())).readObject();
        lh2.value.add("b");
        check(shared.size() == 1, "mutation of copy invisible to original list");
        check(lh2.value.size() == 2, "copy carries both elements");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Holder");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getTypeParameters().length == 1, "generic class <T>");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter T");
        // value 公有字段
        java.lang.reflect.Field vf = c.getField("value");
        check(java.lang.reflect.Modifier.isPublic(vf.getModifiers()), "value public");
        check(!java.lang.reflect.Modifier.isStatic(vf.getModifiers()), "value instance field");
        check(!java.lang.reflect.Modifier.isFinal(vf.getModifiers()), "value mutable (non-final)");
        // 恰好两个构造器：() 与 (T)
        check(c.getConstructors().length == 2, "exactly 2 public constructors");
        check(c.getConstructor().getParameterTypes().length == 0, "no-arg ctor");
        check(c.getConstructor(Object.class).getParameterTypes().length == 1, "single-arg ctor (T erasure)");
        // equals/hashCode 未声明（恒等语义的结构面证据）
        boolean declEquals = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("equals") || m.getName().equals("hashCode")) declEquals = true;
        }
        check(!declEquals, "no declared equals/hashCode (inherited from Object)");
    }

    // 模拟 OUT/IN-OUT 参数传递语义：方法内修改 Holder.value 对调用方可见
    static void fillIn(Holder<String> out, Holder<Integer> inout) {
        out.value = "filled";
        inout.value = inout.value.intValue() * 2;
    }

    static void testOutParamSemantics() {
        Holder<String> out = new Holder<String>();
        Holder<Integer> inout = new Holder<Integer>(21);
        fillIn(out, inout);
        check("filled".equals(out.value), "OUT param: callee write visible to caller");
        check(Integer.valueOf(42).equals(inout.value), "IN-OUT param: caller value transformed");
        // 独立 Holder 互不影响
        Holder<String> x = new Holder<String>("x");
        Holder<String> y = new Holder<String>("x");
        y.value = "y";
        check("x".equals(x.value), "holders independent");
    }
}
