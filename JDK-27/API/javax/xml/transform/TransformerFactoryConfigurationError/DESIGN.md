# javax.xml.transform.TransformerFactoryConfigurationError — 设计案

## 1. API 功能
javax.xml.transform.TransformerFactoryConfigurationError 类。实例/声明方法：getMessage, getException, getCause。

## 2. Java 用户如何使用
new(...) 四种构造 → getMessage/getException/getCause。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.TransformerFactoryConfigurationError）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformerFactoryConfigurationErrorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformerFactoryConfigurationErrorTest.testBlackBox()`）覆盖：四种构造 + cause 传播。

## 5. 深化（本轮新增）
- 4 构造器 round-trip（消息/exception 组合）。
- 真实触发：系统属性指向 no.such 工厂后 newInstance() → 本 Error（finally 清属性）；消息关键词 "no.such" 双版通吃。
