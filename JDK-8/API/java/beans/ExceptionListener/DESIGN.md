# java.beans.ExceptionListener 测试设计案

## 1. API 功能
函数式接口，唯一抽象方法 `void exceptionThrown(Exception e)`。未标注 @FunctionalInterface（实测两侧均无）但可用 lambda/匿名类实现。Encoder/XMLEncoder/XMLDecoder 用它报告可恢复异常；传入 null 时回退到 JDK 默认监听器（向 stderr 打印并 "Continuing ..."）。

## 2. 使用方式
实现监听器收集异常列表用于诊断；解码容错场景（缺类/坏 XML）下保持解码继续。

## 3. 参考来源
- Oracle javadoc：Java SE 8 (`java/beans/ExceptionListener.html`, docs.oracle.com/javase/8/docs/api) 与 JDK 27 (docs.oracle.com/en/java/javase/27/docs/api/java.desktop/java/beans/ExceptionListener.html)。
- JavaBeans 规范 1.01-A（属性/事件/方法命名约定；持久化委托模型见规范第 5 章及 XMLEncoder javadoc）。
- OpenJDK 源码与回归思路（借鉴语义，代码自写）：`jdk8u-dev/jdk/src/share/classes/java/beans/{PersistenceDelegate,DefaultPersistenceDelegate,Encoder,XMLEncoder,XMLDecoder}.java`；`jdk/test/jdk/java/beans/XMLDecoder`、`jdk/test/jdk/java/beans/XMLEncoder`（round-trip/异常路由回归）；`jdk/test/jdk/java/beans/Statement`（默认 ExceptionListener 打印 "Continuing ..." 的行为）。
- 探针实证：本批所有不确定行为先在 jdk8u372-b07 与 jdk-27.0.1 双侧跑探针定案（见负例表）。

## 4. 测试用例设计（ExceptionListenerTest，JDK-8 PASS 33 / JDK-27 PASS 30，旧 3/2）
| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox/testBlackBox | 保留旧版：接口断言、exceptionThrown 分发 |
| testExistingReflection(JDK-8) | 保留旧版 3 条反射断言 |
| testInterfaceStructure | 恰 1 个抽象方法；方法名/参数 java.lang.Exception/void 返回；无 @FunctionalInterface 注解 |
| testCaptureSemantics | 捕获列表累积、保序、同一性；Exception 子类原样透传 |
| testEncoderIntegration | Encoder 失败语句 → 恰 1 个 java.lang.Exception 包装（cause=NoSuchMethodException，消息含 discarding）；随后合法语句不再新增异常 |
| testXMLEncoderIntegration | DPD 未知属性名 → 首个 IllegalStateException（消息含属性名） |
| testXMLDecoderIntegration | 截断 XML → listener 收 SAXParseException（readObject 抛 AIOOBE 不冲突） |
| testXMLDecoderListenerIdentity | 3 参/4 参构造器 getExceptionListener==同一实例；null 监听器回退默认非空 |
| testDefaultListenersNonNull | Encoder/XMLEncoder/XMLDecoder 默认监听器均非 null |
| testThrowingListenerPropagates | 监听器抛出的 RuntimeException **原实例**穿透 readObject；close() 触发的重解析再次抛出 |

## 易错规范细节
- 截断 XML 场景：close() 会再次驱动未完成的解析，监听器若抛异常会从 close() 二次穿透——测试需同时兜住两处。
- "listener 不抛异常 → 解码继续" 的容错语义放在 XMLDecoderTest.testFaultTolerantContinuation 验证（坏元素后的 <int>5</int> 仍可读出）。

## 版本差异（实测）
- 双侧一致；均无 @FunctionalInterface 注解。
