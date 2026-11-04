# java.awt.datatransfer.MimeTypeParseException 测试设计

## 1. API 功能
MIME 类型解析异常。

## 2. Java 用户如何使用
new MimeTypeParseException()/new MimeTypeParseException(msg)。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/MimeTypeParseException.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 Exception；黑盒: 两种构造、getMessage。

自动化实现位于同目录 `MimeTypeParseExceptionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- 双构造器：无参 message=null；带参 message 精确保留（空串/Unicode/同一对象引用）。
- checked 异常语义：可从声明它的方法抛出并精确捕获。
- 序列化 round-trip：message（含 null）与类型保留；toString 含类名与 message。
