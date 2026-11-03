# WeakPairMap.Pair.Weak — 设计方案

## 1. API 功能
`java.lang.WeakPairMap.Pair.Weak<K1, K2>`（public final，继承包私有 `WeakPairMap.WeakRefPeer<K1>`（根为 `java.lang.ref.WeakReference`），实现包私有 `WeakPairMap.Pair`）是 `WeakPairMap` 的存储键：`first()` 弱引用 K1（经 peer 清理），`second()` 弱引用 K2，`hashCode/equals` 与 `Pair.Lookup` 的值键匹配。

## 2. Java 用户如何使用
- 非导出内部 API：与 `Pair.Lookup` 相同，`WeakPairMap`/`Pair` 包私有、构造器包私有、`Pair.weak(...)` 工厂不可达，外部无法实例化；仅 `java.lang` 内部使用。

## 3. 开源代码实际用法调研
- JDK 内部数据结构（JDK 19+ ThreadLocal/Thread 相关实现细节）；无第三方公开使用（javap 证实包私有边界）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.WeakPairMap$Pair$Weak")` 验证 public final、继承 `WeakRefPeer`、实现 `WeakPairMap$Pair`、构造器包私有、`first/second/hashCode/equals` 公开及 `weakPair` 对等链接方法。
黑盒：受包私有限制无法实例化，仅验证 bootstrap 加载、java.lang 包、非接口/枚举、双泛型参数、继承链根到 `java.lang.ref.WeakReference`，并在测试注释与本设计说明原因。
对应 `WeakTest.java` 白盒 + 黑盒部分。
