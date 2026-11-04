# java.util.jar.Attributes.Name 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

合法属性名字符集（字母数字 _ -）；jar 签名/清单常量表。

## 2. Java 用户如何使用

大小写不敏感 equals/hashCode；非法字符 IAE。

## 3. 参考来源

JAR File Specification: Manifest and Signature File Specifications。

## 4. 测试用例设计

NameTest 既有用例 + AttributesTest.testContracts 的 Name 合法/非法矩阵与常量文本断言。
