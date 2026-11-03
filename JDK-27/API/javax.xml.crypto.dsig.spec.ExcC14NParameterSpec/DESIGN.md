# javax.xml.crypto.dsig.spec.ExcC14NParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.ExcC14NParameterSpec 类。常量：DEFAULT（共 1 个）。 实例/声明方法：getPrefixList。

## 2. Java 用户如何使用
DEFAULT 常量、无参构造（空列表）、List 构造往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.ExcC14NParameterSpec）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ExcC14NParameterSpecTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ExcC14NParameterSpecTest.testBlackBox()`）覆盖：常量精确值、无参空列表、列表往返。。
