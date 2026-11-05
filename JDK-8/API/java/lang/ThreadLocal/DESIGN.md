# java.lang.ThreadLocal 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「set 一下 get 一下」的冒烟。重点补：白盒结构（非接口、Object 超类、
> get/set/remove 方法存在）、默认 initialValue 返回 null（protected 暴露）、set/get/remove 往返、
> initialValue 重写、withInitial 工厂（set 覆盖 / remove 后再初始化）、跨线程隔离。
> 断言确定，headless 安全；子线程 daemon + join，不留非 daemon 线程。

## 1. API 提供了哪些功能

- **泛型类**：`java.lang.ThreadLocal<T>`（非 final，可子类化，父类 Object）。
- **核心方法**：`get()`、`set(T)`、`remove()`。
- **初始化钩子**：`protected T initialValue()`（默认返回 null，可重写）。
- **工厂**：`static <S> ThreadLocal<S> withInitial(Supplier<? extends S>)`（JDK-8 已引入）。

## 2. Java 用户如何使用

- 每个线程独立存储：`tl.set(v)` 后同线程 `get()` 取回，其他线程看不到。
- 常见：`SimpleDateFormat`/数据库连接按线程缓存，避免线程安全开销。
- 子类重写 `initialValue()` 或用 `withInitial(supplier)` 提供首访默认值。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/ThreadLocal.html`）：get/set/remove、initialValue 默认 null、
  withInitial、线程隔离语义。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/ThreadLocal.java`：
  initialValue 返回 null、withInitial 用 SuppliedThreadLocal。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/ThreadLocal`。

## 4. 测试用例设计（映射到 ThreadLocalTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：非接口、Object 超类、get/set/remove 存在 | testWhiteBox |
| P0 | set/get/remove 往返 + 默认 null | testBasic |
| P1 | initialValue 重写 | testInitialValueOverride |
| P1 | withInitial：首访初始化、set 覆盖、remove 后再初始化 | testWithInitial |
| P0 | 跨线程隔离（子线程看不到主线程值） | testThreadIsolation |
| P0 | 默认 initialValue 返回 null（protected 暴露） | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **默认 `initialValue()` 返回 `null`**；`get()` 首访调用 `initialValue()`。
- **线程隔离**：值按线程存储，子线程看不到父线程的值（除非用 InheritableThreadLocal）。
- **`remove()` 后下次 `get()` 重新初始化**（对 withInitial 是再次调用 supplier）。
- **`withInitial` 的 supplier 不要求线程安全**，但每次线程首访各调一次。

## 注意事项

- 子线程一律 daemon + join 有界；不依赖 sleep 时序；headless 安全，不加 `@key`。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **生命周期全表**：set 覆盖旧值、remove 幂等（二次 remove 后 get 触发 initialValue）、set(null) 合法
  且不等于"未设置"（不重触发 initialValue）。
- **withInitial 契约**：supplier 每线程至多一次；**remove 后重新武装**（再次 get 调 supplier 得新实例）；
  withInitial(null)→NPE；supplier 抛异常原样传播（消息保留）。
- **身份语义**：同一 ThreadLocal 在不同线程返回各自实例（两个 latch 同步 worker 互不串扰）；
  同线程多个 ThreadLocal 独立；值可变时同线程内共享同一实例（Box 字段可见）。
- **initialValue 子类路径**：覆盖 initialValue 与 withInitial 各自独立计数
  （主线程第 1 次调用、worker 第 2 次——共享计数器确定性编号）。

### 使用方式
get→（惰性 initialValue/withInitial supplier）→set/remove 重置；跨线程靠 ThreadLocal 实例本身隔离。

### 参考来源
Oracle javadoc（remove 后 initialValue 可再触发）；双侧实测行为一致（13→122 断言）。

### 测试用例设计 + 易错规范细节
- 新增 testLifecycleFull/testWithInitialContract/testWorkerIsolationLatched/testInitialValuePerThread/
  testSequentialWorkersIsolated/testManyLocals 等 13 个方法。
- 易错点：**remove 会重新武装 supplier**；**set(null) 与 remove 语义不同**；
  worker 的 initialValue 调用次序要用共享计数器显式编号（不可依赖线程启动顺序）。
