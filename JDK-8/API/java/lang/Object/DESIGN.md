# java.lang.Object 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 个 Object」的冒烟。重点补：白盒结构（无超类、具体类、getClass/
> notify/notifyAll/wait 为 final、hashCode native）、getClass 运行时类、默认 hashCode==identityHashCode、
> equals 自反/对称/null、toString 格式 `className@hexHash`、clone 的 Cloneable 契约 + 浅拷贝 +
> 非 Cloneable 抛 CloneNotSupportedException、wait/notify/notifyAll 监视器协议（未持锁抛 IMSE、
> notify 唤醒、wait(timeout) 超时返回）、protected finalize 可达且 no-op。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Object` 是所有类的根（无超类），具体类（非抽象）。
- **方法**：`getClass`（final）、`hashCode`（native，默认恒等）、`equals`（默认引用相等）、
  `toString`、`clone`（protected）、`finalize`（protected，deprecated）、
  `wait`(×3)/`notify`/`notifyAll`（监视器协议，均 final）。

## 2. Java 用户如何使用

- 覆写 `equals/hashCode/toString` 提供值语义；`clone` 要求类实现 `Cloneable`。
- `synchronized(lock) { lock.wait(); }` 等待通知，`lock.notify()/notifyAll()` 唤醒；
  未持有监视器调用 → `IllegalMonitorStateException`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Object.html`）：各方法契约（clone 需 Cloneable、
  wait 需持锁、hashCode 与 equals 关系）。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Object.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Object` + `Clone`。

## 4. 测试用例设计（映射到 ObjectTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：无超类、具体、getClass/notify/notifyAll/wait final、hashCode native | testWhiteBox |
| P0 | getClass 返回运行时类 | testGetClass |
| P0 | 默认 hashCode==identityHashCode、equals 自反/对称/null | testHashCodeEquals |
| P1 | toString == className@hexHash | testToString |
| P0 | clone：Cloneable 契约 + 浅拷贝 + 非 Cloneable→CNSE | testClone |
| P0 | wait/notify/notifyAll 未持锁→IMSE | testMonitorErrors |
| P0 | wait/notify 唤醒（daemon 子线程 + 有界 join） | testWaitNotify |
| P1 | wait(timeout) 超时返回 | testWaitTimeout |
| P1 | protected finalize 可达且 no-op | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **默认 `hashCode()` == `System.identityHashCode(this)`**（Object 未覆写时）。
- **默认 `equals` 是引用相等**：自反、对称、`equals(null)==false`。
- **`clone` 是浅拷贝**：引用字段共享；仅实现 `Cloneable` 才可调用，否则抛
  `CloneNotSupportedException`。
- **`wait`/`notify`/`notifyAll` 未持锁抛 `IllegalMonitorStateException`**；`getClass`/`wait`/
  `notify`/`notifyAll` 均为 final。
- **`toString` == `getClass().getName() + "@" + Integer.toHexString(hashCode())`**。

## 注意事项

- wait/notify 测试用 daemon 子线程 + 有界 join，不依赖 sleep 时序；`wait(timeout)` 用下界断言。
- headless 安全，不加 `@key`；不调用 `finalize` 触发 GC（仅直接调 protected 方法证明可达）。
