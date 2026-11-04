# java.lang.InheritableThreadLocal 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「子线程读一下父线程值」的冒烟。重点补：白盒结构（父类 ThreadLocal、
> 非接口）、子线程创建时快照继承父值、childValue 变换继承值、默认 childValue 恒等语义。
> 断言确定，headless 安全；子线程 daemon + join，不留非 daemon 线程。

## 1. API 提供了哪些功能

- **泛型类**：`java.lang.InheritableThreadLocal<T>` 扩展 `ThreadLocal<T>`（非接口）。
- **继承语义**：子线程在创建时继承父线程的值（快照，之后互不影响）。
- **变换钩子**：`protected T childValue(T parentValue)`（默认返回父值不变，可重写）。

## 2. Java 用户如何使用

- 让请求上下文/事务标识等自动传播到子线程（如异步任务继承当前用户会话）。
- 子类重写 `childValue` 在子线程创建时变换继承值（如加前缀/脱敏）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/InheritableThreadLocal.html`）：childValue 默认返回父值、
  子线程创建时快照继承。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/InheritableThreadLocal.java`：
  `childValue` 返回 parentValue、`getMap`/`createMap` 用 InheritableThreadLocal 专用 Map。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/InheritableThreadLocal`。

## 4. 测试用例设计（映射到 InheritableThreadLocalTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：父类 ThreadLocal、非接口 | testWhiteBox |
| P0 | instanceof ThreadLocal | testIsThreadLocal |
| P0 | 子线程继承父线程值（快照），父值不变 | testInheritance |
| P1 | childValue 变换继承值 | testChildValueOverride |
| P0 | 默认 childValue 恒等返回父值（protected 暴露） | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **默认 `childValue(parentValue)` 返回 `parentValue` 本身（恒等）**。
- **继承是「创建时快照」**：子线程 `start()` 后才对父线程 `set` 的改动不会传播。
- **子线程对继承值的修改不影响父线程**（反之亦然）。

## 注意事项

- 子线程一律 daemon + join 有界；headless 安全，不加 `@key`。
