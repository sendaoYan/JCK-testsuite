# javax.xml.validation.SchemaFactoryConfigurationError — 设计案

## 1. API 功能
javax.xml.validation.SchemaFactoryConfigurationError 类。标记接口，无声明成员。

## 2. Java 用户如何使用
四种构造。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.SchemaFactoryConfigurationError）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SchemaFactoryConfigurationErrorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SchemaFactoryConfigurationErrorTest.testBlackBox()`）覆盖：四种构造 + cause。

## 5. 深化条目（本批新增，SchemaFactoryConfigurationErrorTest 10→22 断言）
- 四构造器矩阵：()/(String)/(Throwable)/(String,Throwable) 的 message/cause 精确语义（(Throwable) 消息派生自 cause）。
- final 类、Error 层级、toString 含消息；序列化 round-trip（message+cause 保持）。
