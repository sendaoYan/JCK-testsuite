# ClassValue — 设计方案

## 1. API 功能
`java.lang.ClassValue<T>`（abstract）为每个 `Class` 惰性计算并缓存一个值：`get(Class)`、`remove(Class)`，子类重写 `protected computeValue(Class)`。

## 2. Java 用户如何使用
- 作为类到值的缓存（比 `ClassValue` 弱引用语义、无泄漏）。
```java
ClassValue<MethodHandles.Lookup> CV = new ClassValue<>() {
    protected Lookup computeValue(Class<?> c) { return MethodHandles.lookup(); }
};
Lookup l = CV.get(Foo.class);
```

## 3. 开源代码实际用法调研
- `MethodHandles.Lookup` 缓存、字节码框架（ASM/ByteBuddy）用 `ClassValue` 缓存每类派生数据。
- JDK `test/jdk/java/lang/ClassValue` 验证惰性计算、缓存、`remove`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ClassValue")` 验证 abstract、`computeValue` protected。
黑盒：`get` 惰性计算且每类仅一次；`get` 按类隔离；`remove` 后重新计算。
对应 `<Simple>Test.java` 黑盒部分。
