# ScopedValue — 设计方案

## 1. API 功能
`java.lang.ScopedValue<T>`（final，JDK 21 引入）是作用域值，替代 `ThreadLocal` 用于结构化并发：
- 工厂：`newInstance()`
- 绑定：静态 `where(ScopedValue, value)` 返回 `Carrier`；`Carrier.where` 链式
- 读取：`get/isBound/orElse/orElseThrow`；`Carrier.run/call`

## 2. Java 用户如何使用
- 在 `Carrier.run/call` 作用域内绑定值，作用域结束自动解除。
```java
ScopedValue<String> ctx = ScopedValue.newInstance();
ScopedValue.where(ctx, "req-1").run(() -> System.out.println(ctx.get()));
```

## 3. 开源代码实际用法调研
- JDK 结构化并发（`StructuredTaskScope`）用 `ScopedValue` 传播上下文。
- JDK `test/jdk/java/lang/ScopedValue` 系列验证绑定/解绑/重绑定/继承。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ScopedValue")` 验证 final、非接口。
黑盒：未绑定 `isBound=false`、`orElse` 默认值、`get` 抛 `NoSuchElementException`、`orElseThrow` 抛供应商异常；绑定 `Carrier.get/call/run`、`where` 覆盖、原始 carrier 不变、作用域外解绑；链式 `where` 多值。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- run 闭环：`ScopedValue.where(KEY,v).run(() -> KEY.get()==v && KEY.isBound())`；作用域外 `isBound()==false` 且 `KEY.get()` 抛 `NoSuchElementException`（消息实测锁定 **`ScopedValue not bound`**）。
- 嵌套重绑定：外 v1 内 v2（内层见 v2、退出回 v1）；内层抛异常也正确恢复外层值（finally 内实测）；三层 1→2→3 链；同值重绑合法。
- Callable 形态：`where(NUM,5).call(() -> NUM.get()*2)==10`；checked 异常透传；call 后解绑。
- run 体异常透传（**同一 RuntimeException 实例** catch 侧身份比对）；解绑在异常展开时完成。
- 线程封闭（招牌）：主线程绑定后子线程（命名类 + latch）内 `isBound()==false`、`get()` 抛 NSEE；子线程可绑定自己的值；子线程绑定对主线程也不可见；主绑定不受子线程活动影响。
- null 绑定：`where(KEY,null).run` 内 `isBound()==true`、`get()==null`、`orElse("d")==null`（null 绑定 ≠ 未绑定）；出作用域后 get 仍抛 NSEE。
- where 链式（2/3 KEY）与 `Carrier.get`/`Carrier.where` 覆盖（新 carrier、原 carrier 不变）；Carrier 复用（run 两次、call、失败后仍可用）；`where(null, v)` → NPE。
- newInstance 独立性：两 key 互不影响、身份 equals、hashCode 稳定。

### 使用方式
- 同前；跨线程 latch+join 有界等待（30s），子线程命名、必终止。

### 参考来源
- Oracle Java SE 27 javadoc（ScopedValue/Carrier：绑定作用域、NoSuchElementException、线程封闭语义）。
- 本仓库 JDK 27.0.1 实测探针（/tmp/lcI-exp/ProbeSV、ProbeFinal）。

### 测试用例设计 + 易错规范细节
- 新增方法：testRunBindsExactValue、testAfterRunDefaults、testNestedRebinding、testNestedRebindingWithException、testCallReturnsValue、testCallCheckedException、testRunExceptionSameInstance、testWhereChainMultipleKeys、testCarrierReuse、testThreadConfinement（Child 类）、testNullValueBinding、testNewInstanceIndependence、testOrElseInsideScope、testSameValueRebind、testWhereNullKey、testThreeLevelRebinding、testCarrierGetMultiple、testCarrierSurvivesBodyFailure、testChildBindingInvisible。
- 易错点：`where(KEY,null)` 合法且 isBound==true（null 是有效绑定值）；run lambda 不能抛 checked（latch await 需在 lambda 内 try/catch）；`orElse` 在 null 绑定下返回 null 而非默认值；异常路径下外层绑定在 inner 作用域 unwind 后即恢复（finally 可见）。
