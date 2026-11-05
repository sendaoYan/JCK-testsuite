# InheritableThreadLocal — 设计方案

## 1. API 功能
`java.lang.InheritableThreadLocal<T>` 扩展 `ThreadLocal<T>`：
- 子线程在创建时继承父线程的值（快照）
- 子类可重写 `protected childValue(T parentValue)` 变换继承值

## 2. Java 用户如何使用
- 让请求上下文/事务标识等自动传播到子线程（如异步任务继承当前用户会话）。
```java
InheritableThreadLocal<String> ctx = new InheritableThreadLocal<>();
ctx.set("user-1");
new Thread(() -> System.out.println(ctx.get())).start(); // "user-1"
```

## 3. 开源代码实际用法调研
- Spring `RequestContextHolder` 曾有 `inheritable` 变体以向子线程传播请求属性。
- 框架在任务提交到线程池前通过 `InheritableThreadLocal` 捕获上下文并传播（JDK `test/jdk/java/lang/InheritableThreadLocal` 验证继承语义）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证父类为 `ThreadLocal`、非接口。
黑盒：子线程继承父线程值（创建时快照）；`childValue` 变换继承值；`instanceof ThreadLocal`。子线程 daemon + join。
对应 `<Simple>Test.java` 黑盒部分。

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
