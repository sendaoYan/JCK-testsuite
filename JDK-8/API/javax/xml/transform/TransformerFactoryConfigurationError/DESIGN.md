# javax.xml.transform.TransformerFactoryConfigurationError — 设计案

## 1. API 功能
工厂实现类发现/实例化失败错误(Error 非 Exception)。4 个构造器:()、(String)、(Exception)、(String,Exception);方法 getException()、getMessage()。

## 2. Java 用户如何使用
一般不捕获;测试里以系统属性指向坏工厂类真实触发。

## 3. 开源代码实际用法调研
JDK 工厂发现逻辑(FactoryFinder)在 ClassNotFound/Instantiate 失败时抛出;test/jdk 用例以 -D 属性触发。

## 4. 测试用例设计 + 易错细节
黑盒:4 构造器 round-trip;() 消息 null、(String) 消息裸串、(Exception) 消息==e.toString() 且 getException()==e、(String,Exception) 双读。
真实触发:系统属性 javax.xml.transform.TransformerFactory="no.such.TF" 后 TransformerFactory.newInstance() → TFCE,getException() 非 null(class java.lang.ClassNotFoundException 家族)或消息含 "no.such";系统属性在 finally 中清除。
易错:(1) 这是 Error——测试若在 check() 外抛出会直接失败主线程,必须 try/catch 断言;(2) JDK-8 的发现失败消息与 JDK-27 措辞不同(实现内部文本),用 contains 关键词 "no.such" 两版通吃;(3) 属性名 javax.xml.transform.TransformerFactory 必须原样恢复,否则污染后续用例。
