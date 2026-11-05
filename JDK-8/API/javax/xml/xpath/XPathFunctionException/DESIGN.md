# javax.xml.xpath.XPathFunctionException — 设计案

## 1. API 功能
javax.xml.xpath.XPathFunctionException extends XPathExpressionException：扩展函数求值失败。构造器 (String)/(Throwable)。

## 2. Java 用户如何使用
在 XPathFunction.evaluate 内抛出以标记扩展函数失败。

## 3. 参考来源
Oracle javadoc；OpenJDK JAXP 扩展函数调用路径实测。

## 4. 测试用例设计 + 易错细节（XPathFunctionExceptionTest）
- **构造链**：(String)/(Throwable) 往返 + 消息派生；恰好 2 个构造器；is-a XPathExpressionException。
- **真实触发（核心）**：函数实现内 throw new XPathFunctionException("boom") → **原样冒泡到调用方（不被包装）**——与 resolver 返回 null（→ XPathExpressionException）区分。
