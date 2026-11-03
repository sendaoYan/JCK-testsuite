# java.lang.runtime.ObjectMethods — 设计方案

## 1. API 功能
final 引导类（编译器为 record 生成 equals/hashCode/toString 时调用）：
- 唯一 public 方法 `bootstrap(Lookup, String name, TypeDescriptor type, Class<?> recordClass, String names, MethodHandle... getters)`
- name ∈ {equals, hashCode, toString}；type 为 MethodType 时返回 `ConstantCallSite`，为 `MethodHandle.class` 时返回裸 MethodHandle
- names 是 `;` 分隔的组件名列表，getters 为对应字段访问器

## 2. Java 用户如何使用
正常路径由 javac 生成 invokedynamic；也可显式调用：
```java
CallSite cs = (CallSite) ObjectMethods.bootstrap(lookup, "equals",
        MethodType.methodType(boolean.class, Pair.class, Object.class),
        Pair.class, "a;b", getterA, getterB);
boolean eq = (boolean) cs.dynamicInvoker().invokeExact(p, (Object) q);
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/runtime/ObjectMethods.java`：`makeEquals` 走 `CLASS_IS_INSTANCE + Objects.equals/eq`，`makeHashCode` 用 hashCombiner，`makeToString` 走 StringConcatFactory。
- JDK `test/jdk/java/lang/runtime/ObjectMethodsTest.java`：C/Empty 两个 fixture 的三种方法、`MethodHandle.class` 描述符、异常路径（错误 name/类型/名字不匹配）。
- record 编译产物（javap 任意 record）可见 `invokedynamic ObjectMethods.bootstrap`。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、唯一 public static bootstrap、返回 Object。
黑盒（Pair fixture + findGetter 访问器 + 真 record Point 对照）：
- equals：自等/同值/异值/null/异类型全路径；
- hashCode 与 `Objects.hash(组件)` 一致（含 0/负数）；
- toString 精确格式 `Pair[a=1, b=2]`；
- 真 record 的编译器生成行为与显式引导一致；
- `MethodHandle.class` 描述符返回裸句柄可 invokeExact；
- 非法 name/类型/名字数不匹配抛 IAE、null lookup 抛 NPE；单组件 record 行为。
对应 `ObjectMethodsTest.java` 的 `testBlackBox()`。
