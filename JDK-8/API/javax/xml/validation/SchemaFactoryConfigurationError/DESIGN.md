# javax.xml.validation.SchemaFactoryConfigurationError — 设计案

## 1. API 功能
javax.xml.validation.SchemaFactoryConfigurationError extends Error（final 类）：工厂配置错误。构造器 ()/(String)/(Throwable)/(String,Throwable)。

## 2. Java 用户如何使用
配置错误（如系统属性指定工厂类不可用）时由工厂查找机制抛出；用户代码捕获 Error 层级。

## 3. 参考来源
Oracle javadoc javax.xml.validation.SchemaFactoryConfigurationError；java.lang.Error 消息/cause 语义。

## 4. 测试用例设计 + 易错细节（SchemaFactoryConfigurationErrorTest）
- **构造链矩阵**：() → message/cause null；(String) 往返、cause null；(Throwable) cause 同一引用、message 派生自 cause（含其消息）；(String,Throwable) 双往返。
- **final/Error 语义**：类 final、instanceof Error（unchecked）、toString 含消息。
- **序列化 round-trip**：message 与 cause（RuntimeException）保持。
