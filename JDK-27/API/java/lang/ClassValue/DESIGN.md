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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 同 JDK-8 侧全闭环：compute 恰好一次（计数器）、恒等缓存、按类独立条目、remove 重算/对未计算类 no-op、null 值缓存不重算、异常不缓存下次重试、多实例互不干扰、异构键矩阵。
- 27 增强：lambda 类是 hidden class（isHidden()==true）也可作键且稳定缓存；record 类作键。
- 版本行为一致（双侧实测）：null 缓存、异常不缓存、remove 重算语义完全相同。

### 2. 使用方式
同 8 侧。

### 3. 参考来源
Oracle javadoc；JEP 371（hidden class）；双侧实测。

### 4. 测试用例设计 + 易错规范细节
- 继承 8 侧全部易错点（Integer 缓存陷阱、null/异常缓存方向相反、计数器独立）。
- 27 侧新增断言：lambda 键类 isHidden()==true（实现行为，JDK-15+ 稳定）。
- 覆盖：testComputeOnceAndIdentity/testPerClassEntries/testRemoveRecompute/testNullValueCached/testExceptionNotCached/testIndependentInstances/testExoticKeys。
