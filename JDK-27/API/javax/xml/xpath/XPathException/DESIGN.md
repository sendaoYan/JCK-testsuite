# javax.xml.xpath.XPathException — 设计案

## 1. API 功能
javax.xml.xpath.XPathException 类。实例/声明方法：getCause, printStackTrace。

## 2. Java 用户如何使用
new(String)/new(Throwable) → getMessage/getCause。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathExceptionTest.testBlackBox()`）覆盖：两种构造 + cause 传播。

## 5. 深化条目（本批新增，XPathExceptionTest 10→21 断言）
- **构造链实测**：(String null) → NPE "message can't be null"；(Throwable null) → NPE "cause can't be null"；(Throwable) 消息派生 cause.toString()；getCause 多次稳定。
- 序列化 round-trip（message+cause）；三子类 is-a 断言。
