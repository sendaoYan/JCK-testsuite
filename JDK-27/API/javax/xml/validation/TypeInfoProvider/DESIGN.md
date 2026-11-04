# javax.xml.validation.TypeInfoProvider — 设计案

## 1. API 功能
javax.xml.validation.TypeInfoProvider 抽象类。实例/声明方法：getElementTypeInfo, getAttributeTypeInfo, isIdAttribute, isSpecified。

## 2. Java 用户如何使用
newValidatorHandler().getTypeInfoProvider() → getElementTypeInfo/getAttributeTypeInfo/isIdAttribute/isSpecified。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.TypeInfoProvider）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TypeInfoProviderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TypeInfoProviderTest.testBlackBox()`）覆盖：初始空状态查询。
