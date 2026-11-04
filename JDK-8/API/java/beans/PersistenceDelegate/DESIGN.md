# java.beans.PersistenceDelegate 测试设计案（JDK-8 侧）

## 1. API 功能
持久化委托抽象基类。public `writeObject(Object, Encoder)`（非 final，javadoc 明言通常无需覆写）；protected abstract `instantiate(Object, Encoder)`；protected `mutatesTo(Object, Object)`（默认实现 = 双方非 null 且 class 相同即 true——**不比较 equals**）；protected `initialize(Class, Object, Object, Encoder)`（默认链到父类类型的委托）。隐式构造器 JDK-8 为 **public**（JDK-9+ 改为 protected，见版本差异）。

## 2. 使用方式
为无法用默认委托描述的类（私有构造器/工厂方法/不可变类）提供 `instantiate` 返回 `new Expression(value, FactoryClass.class, "create", args)`；在 `initialize` 中用 `out.writeStatement(new Statement(oldInstance, "setter", args))` 描述状态变化（以 oldInstance 为 target，编码器会换成克隆环境对象）。

## 3. 参考来源
- Oracle javadoc：Java SE 8 (`java/beans/<T>.html`, docs.oracle.com/javase/8/docs/api) 与 JDK 27 (docs.oracle.com/en/java/javase/27/docs/api/java.desktop/java/beans/<T>.html)。
- JavaBeans 规范 1.01-A（属性/事件/方法命名约定；持久化委托模型见规范第 5 章及 XMLEncoder javadoc）。
- OpenJDK 源码与回归思路（借鉴语义，代码自写）：`jdk8u-dev/jdk/src/share/classes/java/beans/{PersistenceDelegate,DefaultPersistenceDelegate,Encoder,XMLEncoder,XMLDecoder}.java`；`jdk/test/jdk/java/beans/XMLDecoder`、`jdk/test/jdk/java/beans/XMLEncoder`（round-trip/异常路由回归）；`jdk/test/jdk/java/beans/Statement`（默认 ExceptionListener 打印 "Continuing ..." 的行为）。
- 探针实证：本批所有不确定行为先在 jdk8u372-b07 与 jdk-27.0.1 双侧跑探针定案（见负例表）。

## 4. 测试用例设计（PersistenceDelegateTest，PASS 45 断言，旧 3）
| 测试方法 | 覆盖点 |
|---|---|
| testExistingReflection | 保留旧版 3 条反射断言（非接口/abstract/声明 writeObject） |
| testModifierMatrix | writeObject public+具体+非 final；instantiate protected abstract；mutatesTo/initialize protected 具体；构造器 public（JDK-8 特有） |
| testMutatesToDefaults | 同类任意实例 true（含 "a" vs "b"）；null 三组合 false；跨类 false；子类 false |
| testWriteObjectDispatch | 覆写 writeObject 收到精确 oldInstance/Encoder 实例 |
| testBaseWriteObjectFlow | 基类流程：get→mutatesTo→instantiate 各一次，get 发布克隆实例 |
| testFactoryDelegateRoundTrip | 工厂方法委托：XML 含 method="create"，round-trip v=9 |
| testInstantiateFailureRouted | instantiate 抛 IllegalStateException → ExceptionListener 收到 1 个 **java.lang.Exception** 包装（消息含 "discarding"，cause 为原异常），对象不落盘 |
| testStatementWritingDelegate | initialize 写 setter Statement → XML 含 property="s"/"x"，round-trip |
| testInitializeChainingForSubclass | 子类仅提供 instantiate：0 异常、默认 initialize 不写属性语句（解码值为默认值） |
| testNullEncoder | writeObject(x, null) → NullPointerException |

## 易错规范细节
- 工厂委托的 Expression target 必须是 **Class 对象**（静态工厂）；以实例为 target 会无限递归 StackOverflowError（实测）。
- instantiate 抛出的异常不会原样到达 listener，而是 `new Exception("XMLEncoder: discarding statement ...", cause)` 包装。
- mutatesTo 默认语义与直觉相反：同类即 true，不比内容（String "a"/"b" 也 true）；DPD 才按是否声明 equals 决定细化。

## 版本差异（实测 javap）
- 隐式构造器：JDK-8 `public PersistenceDelegate()`；JDK-27 `protected`。两侧各自断言。
