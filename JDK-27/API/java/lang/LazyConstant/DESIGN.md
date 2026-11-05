# LazyConstant — 设计方案

## 1. API 功能
`java.lang.LazyConstant`（预览 API，JDK 25+）是惰性求值的常量容器：`interface LazyConstant<T> extends Supplier<T>`，抽象成员 `T get()`、`equals/hashCode/toString`，静态工厂 `static <T> LazyConstant<T> of(Supplier<T> supplier)`。语义：supplier 仅在被首次 `get()` 时求值一次，之后缓存结果。

## 2. Java 用户如何使用
- 用 `LazyConstant.of(() -> 昂贵计算())` 声明延迟初始化常量，需要时 `get()` 触发求值并缓存。
```java
LazyConstant<String> name = LazyConstant.of(() -> loadName());
name.get(); // 首次求值
name.get(); // 直接返回缓存
```
- 预览 API，编译运行需 `--enable-preview`。

## 3. 开源代码实际用法调研
- JEP 496（`java.lang` 预览）引入 `LazyConstant`；JDK `test/jdk/java/lang/LazyConstant` 验证惰性、单次求值与 `equals/hashCode/toString` 契约。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.LazyConstant")` 验证为接口、继承 `Supplier`、`of(Supplier)` 静态。
黑盒：`of` 不求值 supplier，`get()` 触发并只调用一次（计数），`get` 返回缓存值；验证 `equals(self)`、`hashCode` 稳定、`toString` 非空。
对应 `<Simple>Test.java` 白盒 + 黑盒部分（文件使用 `@enablePreview`）。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- `of(null)` → 立即 NPE（构造期，不是 get 期）。
- **失败缓存语义（锁定）**：supplier 抛异常或返回 null 都视为失败——get() 抛 `NoSuchElementException`，消息精确 `Unable to access the constant because <原异常类名> was thrown at initial computation`；**首次**失败携带原异常为 cause（身份断言），后续 get 同消息但 cause==null；supplier（含失败路径）**仅求值一次**。
- equals 语义：每个 `of()` 产生独立常量——同一 supplier 的两个实例**不相等**；equals(null)/跨类型 false；求值后自等不变。
- toString 演化（impl 锁定 27.0.1）：求值前含 `computing function=`、求值后为 `[value]` 形（`[v]`）；hashCode 跨求值稳定。
- Supplier 视图：`LazyConstant extends Supplier`，两视图共享同一次求值（总调用数=1）；对象值 get 返回**同一实例**（身份断言）。
- 组合与独立：常量可依赖常量（`base.get()*2`）；同 supplier 的两个常量各自求值（总调用=2）。
- 并发单次求值：4 线程 latch 同步 get，supplier 恰好调用 1 次、各线程同值。
- 非预览断言面：**无 @FunctionalInterface 注解**（但仍是 lambda 目标）。

### 参考来源
JDK 27.0.1 `jdk.internal.lang.LazyConstantImpl` 行为实测（/tmp/lcI-exp/ProbeLC*、ProbeFinal）；JEP 稳定值/惰性常量系列（预览）。

### 测试用例设计 + 易错规范细节
新增方法：testOfNullSupplier、testFailingSupplierCached、testNullValueRejected、testEqualsSemantics、testToStringEvolution、testSupplierView、testValueIdentity、testComposition、testIndependentConstants、testConcurrentSingleEvaluation、testPreviewSurface。
- 易错点：**null 返回值是失败**（内部 requireNonNull → NPE → 缓存失败）不是合法常量值；失败消息属于 impl（预览 API），本套件锁精确文本并标注随版本漂移风险；文件保留 `@enablePreview`，javac/java 需 `--enable-preview -source 27`。
