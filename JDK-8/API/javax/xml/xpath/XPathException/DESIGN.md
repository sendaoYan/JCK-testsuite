# javax.xml.xpath.XPathException — 设计案

## 1. API 功能
javax.xml.xpath.XPathException extends Exception（checked）：XPath 求值异常基类。构造器 (String)/(Throwable)，覆写 getCause 与 printStackTrace 族。

## 2. Java 用户如何使用
捕获 XPathExpressionException/XPathFactoryConfigurationException/XPathFunctionException 的公共基类；或构造后携带 cause 上抛。

## 3. 参考来源
Oracle javadoc javax.xml.xpath.XPathException（cause 前置初始化语义）。

## 4. 测试用例设计 + 易错细节（XPathExceptionTest）
- **构造链**：(String) 消息往返、cause null；**null 消息 → NPE（实测锁定 "message can't be null"）**；(Throwable) cause 同一引用、消息派生自 cause.toString()；**(Throwable null) → NPE（"cause can't be null"，实测）**。
- **getCause 稳定**：多次调用同一引用（覆写语义，非 initCause 可改）。
- **序列化 round-trip**：message 与 cause 保持。
- **家族层级**：三个子类均 instanceof XPathException。
