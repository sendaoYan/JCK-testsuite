# javax.xml.datatype.DatatypeConstants.Field — 设计案

## 1. API 功能
javax.xml.datatype.DatatypeConstants.Field 类。实例/声明方法：toString, getId。

## 2. Java 用户如何使用
getId/toString/instanceof 断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.datatype.DatatypeConstants.Field）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 FieldTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`FieldTest.testBlackBox()`）覆盖：6 字段、id 值、toString、互异性。

## 5. 深化（本批新增）
- 六常量完整族：getId 0..5（YEARS=0..SECONDS=5）顺序锁定、toString 精确、两两 identity 互异、非 null。
- 公开面锁定：**仅有 getId/toString 两个自有公开方法**（反射枚举声明方法）。
- Field 类 final。
