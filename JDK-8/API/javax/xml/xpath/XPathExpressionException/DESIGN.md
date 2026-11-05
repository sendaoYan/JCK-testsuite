# javax.xml.xpath.XPathExpressionException — 设计案

## 1. API 功能
javax.xml.xpath.XPathExpressionException extends XPathException：表达式无效或求值失败。构造器 (String)/(Throwable)。

## 2. Java 用户如何使用
compile/evaluate 非法表达式时抛出；作为 catch 目标处理语法与运行时求值错误。

## 3. 参考来源
Oracle javadoc；OpenJDK XPathImpl 实测消息。

## 4. 测试用例设计 + 易错细节（XPathExpressionExceptionTest）
- **构造链**：(String) 往返、(Throwable) cause 往返 + 消息派生；**无 (String,Throwable) 重载（反射断言恰好 2 个公共构造器）**。
- **真实触发**：compile("//a[") → 消息含 "A location path was expected, but the end of the XPath expression was found instead."（消息不含原文表达式——任务初稿预期否定，以实测为准）。
- 序列化 round-trip cause 保持。
