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

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **身份继承**：子线程持父线程**同一实例**（==，不是副本）；多 key 各自继承；
  子线程 set 只影响自己（隔离）；父在子构造后再 set 不可见（快照时点）。
- **构造时点快照**：`new Thread()` 时复制（"before-construction" 可见、"after-construction" 不可见），
  非 start() 时点；remove 后构造的子线程看不到该值（并阻断孙代链）。
- **childValue 变换**：默认恒等返回；覆盖后每子线程恰一次（两子→2 次）；null 值也调用 childValue(null)；
  孙代链式变换（">" 追加 → "root>>"）；同一父的两个子互相独立。
- **对照**：普通 ThreadLocal 不继承；**ThreadLocal.withInitial 构造的值也不继承**
  （子线程首次 get 跑自己的 supplier，distinct 实例、supplier 计数 +1）。
- **继承源**：父线程的 initialValue/withInitial 结果同样参与继承（子构造触发父首次 get）。

### 使用方式
父线程 set/首次 get → new Thread（快照+childValue）→ 子线程 get 得变换值 → 孙代再链式。

### 参考来源
Oracle javadoc（childValue 默认恒等、构造时复制）；双侧实测一致（8→91 断言）。

### 测试用例设计 + 易错规范细节
- 新增 testIdentityInheritance/testConstructionTimeSnapshot/testChildValueTransform/
  testGrandchildChain/testPlainThreadLocalNotInherited/testWithInitialNotInheritable/
  testChildValueOncePerChild 等 24 个方法。
- 易错点：**快照在 new Thread() 不在 start()**；**withInitial+ITL 组合中子线程会跑自己的 supplier**
  （断言 distinct 而非 null）；childValue 计数按"每子一次"断言。
