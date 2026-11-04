# javax.xml.transform.Transformer — 设计案

## 1. API 功能
javax.xml.transform.Transformer 抽象类。实例/声明方法：reset, transform, setParameter, getParameter, clearParameters, setURIResolver, getURIResolver, setOutputProperties, getOutputProperties, setOutputProperty, getOutputProperty, setErrorListener, getErrorListener。

## 2. Java 用户如何使用
newTransformer → set/getParameter → set/getOutputProperty → transform(DOM→Stream) → reset。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.Transformer）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformerTest.testBlackBox()`）覆盖：parameter 往返+clear、output property 往返+null 异常、error listener、transform 输出、reset。
