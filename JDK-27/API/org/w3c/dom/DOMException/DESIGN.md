# org.w3c.dom.DOMException — 设计案

## 1. API 功能
org.w3c.dom.DOMException 类。常量：INDEX_SIZE_ERR, DOMSTRING_SIZE_ERR, HIERARCHY_REQUEST_ERR, WRONG_DOCUMENT_ERR, INVALID_CHARACTER_ERR, NO_DATA_ALLOWED_ERR, NO_MODIFICATION_ALLOWED_ERR, NOT_FOUND_ERR, NOT_SUPPORTED_ERR, INUSE_ATTRIBUTE_ERR, INVALID_STATE_ERR, SYNTAX_ERR, INVALID_MODIFICATION_ERR, NAMESPACE_ERR, INVALID_ACCESS_ERR, VALIDATION_ERR, TYPE_MISMATCH_ERR（共 17 个）。

## 2. Java 用户如何使用
常量断言 + new(short,String) → code/getMessage

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DOMException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMExceptionTest.testBlackBox()`）覆盖：常量 + code 字段 + 构造。
