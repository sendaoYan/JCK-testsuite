# java.lang.CloneNotSupportedException 测试设计案（JDK-8）

> 覆盖目标：完整契约。重点补：白盒（恰 2 个构造器——无 (String,Throwable) 形态、父类 Exception、
> Serializable）、构造链与 cause（initCause 一次性、二次 → IllegalStateException、自引用 →
> IllegalArgumentException "Self-causation not permitted" 精确消息）、真触发闭环（未实现
> Cloneable 的 clone 调用 → 恰为 CloneNotSupportedException 类 + 消息==被克隆类二进制名 +
> 多态 catch）、序列化 round-trip（类与消息保留）、SUID=5195511250079656443 锁定（与 27 一致）、
> Throwable 通用面（suppressed 保序、setStackTrace 回读、防御拷贝、printStackTrace 的
  Suppressed: 行）。

## 1. API 提供了哪些功能

- **受检异常**（extends Exception）：`CloneNotSupportedException()` 与 `(String)` 两个构造器；
  无 cause 构造器——cause 经 `initCause` 补设；由 `Object.clone()` 在目标未实现 Cloneable 时
  抛出（消息为被克隆类的二进制名）。

## 2. Java 用户如何使用

- 覆写 clone 时必须声明/捕获它（受检）；
- 常见模式：`catch (CloneNotSupportedException e) { throw new AssertionError(e); }`（不可达时）。

```java
public Box clone() {
    try { return (Box) super.clone(); }
    catch (CloneNotSupportedException e) { throw new AssertionError(e); }
}
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`CloneNotSupportedException.html`）：两构造器；
  `Object.clone()` 抛出条件与消息。
- **OpenJDK 8u-dev** `Object.clone` C++ 实现（`CloneNotSupportedException(klass->name())`）。
- **Java Object Serialization Spec**：serialVersionUID 5195511250079656443（双侧实测一致）。

## 4. 测试用例设计（映射到 CloneNotSupportedExceptionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：2 构造器、无 (String,Throwable)、父类 Exception、Serializable | testWhiteBox |
| P0 | 构造链：null 消息 / "m" / toString 双形（有无冒号） | testConstructorAndCauseChain |
| P0 | initCause：返回 this、同实例、二次 ISE | testConstructorAndCauseChain |
| P0 | 真触发：类型 + 消息==二进制名 + 多态 catch + 消息稳定 | testRealTriggerFromObjectClone |
| P1 | 序列化 round-trip + SUID 锁定 | testSerializationAndSvuid |
| P1 | Throwable 面：suppressed 默认空、setStackTrace 回读、防御拷贝、printStackTrace | testThrowableSurface |
| P1 | 自引用 initCause → IAE "Self-causation not permitted" | testCauseEdgeCases |
| P2 | suppressed 插入保序、fillInStackTrace、非 RuntimeException | testCauseEdgeCases |
| P0 | 基础构造（原有） | testCloneNotSupportedException |

## 易错规范细节

- **无 (String,Throwable) 构造器**（与多数 JDK 异常不同）——cause 只能 initCause 补设。
- **消息来自被克隆类名**：`Object.clone()` 抛出的实例 getMessage() 恰为目标类二进制名
  （嵌套类含 `$`），非 null 也非固定字符串。
- **自引用 cause 禁止**：`e.initCause(e)` → IllegalArgumentException，消息
  "Self-causation not permitted"（8/27 一致，精确锁定）。
- **getStackTrace 返回防御拷贝**：`getStackTrace() != setStackTrace 传入数组`。
