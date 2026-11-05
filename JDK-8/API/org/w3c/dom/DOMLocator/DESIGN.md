# DOMLocator — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMLocator javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
真实实例经 DOMError.getLocation() 取得：getLineNumber/getColumnNumber 1 基（"<r><1x/></r>" → line 1 col 5）、getUtf16Offset=4、getByteOffset=-1（字符输入未知）、getUri()=null（内存字符串输入）；多行文档行号指向出错行；五 getter 全回读。
