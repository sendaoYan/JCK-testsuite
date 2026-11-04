# java.beans.Encoder 测试设计案

## 1. API 功能
编码器抽象基类（public 类、public 构造器）。`set/getExceptionListener`（null 回退默认监听器）；`get/setPersistenceDelegate`；`remove(Object)` 返回被移除的绑定值；`get(Object)`（null→null；String→同一实例；未见过的值→null）；**protected** `writeObject(Object)`（仅子类可见，XMLEncoder 将其升为 public；`o == this` 时直接返回）；public `writeStatement(Statement)`（把目标/实参映射到克隆环境并**立即执行**，失败包装为 Exception 送 listener）；public `writeExpression(Expression)`（先求值；get 已绑定则早退；否则绑定克隆表达式并回调 writeObject；求值失败先送 listener 原异常再抛 RuntimeException）。注意 Encoder **没有** owner 方法（那是 XMLEncoder 的）。

## 2. 使用方式
一般经由 XMLEncoder 间接使用；测试与自定义流直接实例化并覆写 writeObject/writeStatement/writeExpression 观察委托机制。

## 3. 参考来源
- Oracle javadoc：Java SE 8 (`java/beans/Encoder.html`, docs.oracle.com/javase/8/docs/api) 与 JDK 27 (docs.oracle.com/en/java/javase/27/docs/api/java.desktop/java/beans/Encoder.html)。
- JavaBeans 规范 1.01-A（属性/事件/方法命名约定；持久化委托模型见规范第 5 章及 XMLEncoder javadoc）。
- OpenJDK 源码与回归思路（借鉴语义，代码自写）：`jdk8u-dev/jdk/src/share/classes/java/beans/{PersistenceDelegate,DefaultPersistenceDelegate,Encoder,XMLEncoder,XMLDecoder}.java`；`jdk/test/jdk/java/beans/XMLDecoder`、`jdk/test/jdk/java/beans/XMLEncoder`（round-trip/异常路由回归）；`jdk/test/jdk/java/beans/Statement`（默认 ExceptionListener 打印 "Continuing ..." 的行为）。
- 探针实证：本批所有不确定行为先在 jdk8u372-b07 与 jdk-27.0.1 双侧跑探针定案（见负例表）。

## 4. 测试用例设计（EncoderTest，JDK-8 PASS 63 / JDK-27 PASS 61，旧 2/4）
| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | 类 public、构造器 public、writeObject **protected** 非 final、writeStatement/writeExpression/get/remove/set-getPersistenceDelegate public |
| testBlackBox | 保留旧版：listener set/get、String 默认委托非空、自定义注册 |
| testLegacy(JDK-8) | 保留旧版 main 的两条 check 原样 |
| testRecordingSubclass | 子类覆写三方法：String 路径精确计数 1 次 + 参数同一性；bean 路径计数递增（委托机制重入） |
| testInternalDelegationObservable | DPD initialize 的 getter 表达式回流 writeExpression（we 递增） |
| testGetSemantics | get(null)=null、get(new String)==同一实例、get(未见 bean)=null |
| testExpressionCaching | writeExpression 后 get 发布绑定（非原对象、同型、稳定同一实例） |
| testRemoveAndRewrite | 计数委托：写一次 instantiate 1 次；再写仍 1 次；remove 返回克隆实例且 get 归 null；再写变 2 次 |
| testRemoveUnseen | remove(未见)=null、remove(null)=null |
| testStatementClonedExecution | writeStatement 后原对象未动、克隆对象 x=11（立即执行在克隆环境） |
| testStatementFailureRouted | 失败语句 → listener 恰 1 个 **java.lang.Exception**（消息含 discarding，cause=NoSuchMethodException） |
| testExpressionFailureRouted | 不可求值表达式 → writeExpression 抛 RuntimeException("failed to evaluate") 且 listener 收到**原始** NoSuchMethodException |
| testExceptionListenerDefaults | 默认非空；set(null) 回退默认非空；自定义原样返回 |
| testPersistenceDelegateRegistry | String/Integer/null 类型/未知类型委托；setPD(cls,null) 回退默认 |
| testWriteObjectDelegation | writeObject 委派注册委托（参数同一性） |
| testWriteObjectSelfIsNoOp | writeObject(this) 不派发（计数 0） |

## 易错规范细节
- 基类 writeObject 是 protected：测试经子类暴露（OpenEncoder.write）；精确计数断言用 String 值——Encoder.get 对 String 短路返回同一实例，避免委托机制重入干扰计数。
- writeStatement 并不执行在原对象上，而是克隆环境对象；副作用经 `enc.get(orig)` 观察。
- 失败语句与失败表达式的路由不同：语句→包装 Exception+cause；表达式→原异常直送 listener + 向调用方抛 RuntimeException。

## 版本差异（实测）
- 双侧公开 API 一致；无行为差异。
