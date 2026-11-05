# org.xml.sax.SAXNotRecognizedException — 设计案

## 1. API 功能
org.xml.sax.SAXNotRecognizedException 类。标记接口，无声明成员。

## 2. Java 用户如何使用
两种构造。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.SAXNotRecognizedException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXNotRecognizedExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXNotRecognizedExceptionTest.testBlackBox()`）覆盖：message/no-arg。

### 深化轮次2（行为契约）
黑盒：真实触发矩阵——reader 四访问器 × 未知 ID 全部抛本异常（非 SAXNotSupportedException）；XMLFilterImpl 孤儿 filter 上同样触发；构造链默认。
