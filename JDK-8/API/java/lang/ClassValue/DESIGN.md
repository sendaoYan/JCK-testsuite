# java.lang.ClassValue 测试设计案（JDK-8）

> 覆盖目标：ClassValue 的核心契约是「按 Class 键计算一次、缓存、remove 后重算」。本轮从 3 断言冒烟
> 深化到全契约：compute 恰好一次（静态计数器实证）、同一缓存对象恒等、按类独立、remove 语义、
> null 值缓存、异常不缓存、实例间独立、异构键（原始/void/数组/枚举/接口/lambda 合成类）。

## 1. API 功能
- `ClassValue<T>`：抽象类，子类实现 `protected T computeValue(Class<?> type)`。
- `get(Class)`：首次调用触发 computeValue 并缓存，后续调用返回同一对象。
- `remove(Class)`：丢弃该键的缓存值，下次 get 重新计算。

## 2. 使用方式
典型用途是按类缓存派生数据（如 LambdaMetafactory、反射缓存）：
```java
ClassValue<Formatter> FMT = new ClassValue<Formatter>() {
    protected Formatter computeValue(Class<?> c) { return newFormatter(c); }
};
Formatter f = FMT.get(SomeClass.class); // 计算一次
```

## 3. 参考来源
Oracle javadoc（ClassValue 计算语义、remove 语义、线程安全说明）；JLS 17.1（内存可见性——本测试单线程确定性）；双侧 JDK 实测。

## 4. 测试用例设计 + 易错规范细节（2026-11 深化）
- **易错 1**：验证「缓存同一对象」不能用 Integer.valueOf 小值——-128..127 命中整数缓存，跨实例也 ==。计数器返回值乘 1000（>127）保证每次计算必是新对象。
- **易错 2**：null 值**会被缓存**（第二次 get 不再调 computeValue）；computeValue 抛异常则**不缓存**（下次 get 重试）——两者方向相反，实测锁定。
- **易错 3**：remove 从未计算过的类是无害 no-op；连续两次 remove 也只重算一次。
- **易错 4**：computeValue 收到的参数与 get 传入的 Class 恒等（记录 asked 列表逐一 ==）；单线程下调用顺序即 get 顺序。
- **易错 5**：原始类型/void/数组/枚举/接口/lambda 合成类都是合法键；两个不同 lambda 的类是不同键。
- 覆盖：testComputeOnceAndIdentity/testPerClassEntries/testRemoveRecompute/testNullValueCached/testExceptionNotCached/testIndependentInstances/testExoticKeys。
