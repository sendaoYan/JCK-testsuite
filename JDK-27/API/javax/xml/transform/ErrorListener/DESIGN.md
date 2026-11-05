# javax.xml.transform.ErrorListener — 设计案

## 1. API 功能
javax.xml.transform.ErrorListener 接口。实例/声明方法：warning, error, fatalError。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.ErrorListener() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.ErrorListener）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ErrorListenerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ErrorListenerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- 编译期回调：未闭合标签 → error+fatalError 收到 "unclosed"；非法 XPath "!!" 进监听消息。
- 运行期回调：xsl:message terminate → fatalError 含 "xsl:message"；document() 失联 → warning/error 含 "nonexistent"；非法数字格式 → error 含 "1gamma"。
- 传播：回调抛 RuntimeException("listener-boom") 原样穿透 transform()。
