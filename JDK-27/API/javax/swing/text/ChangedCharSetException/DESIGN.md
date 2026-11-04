# javax.swing.text.ChangedCharSetException — DESIGN

## 1. API 功能
`ChangedCharSetException extends IOException`，当文档字符集在解析过程中发生变化时抛出。方法分组：
- 构造器：`ChangedCharSetException(String charSetSpec, boolean charSetKey)`。
- 查询：`getCharSetSpec()` 返回字符集规格字符串；`keyEqualsCharSet()` 返回布尔标志（字符集是否等于关键字）。

## 2. Java 用户如何使用
HTML 解析器读取到 `<meta http-equiv="Content-Type" content="text/html; charset=...">` 时抛出新异常：`throw new ChangedCharSetException(content, key)`。上层捕获后读取 `getCharSetSpec()` 决定是否以新字符集重新解析，`keyEqualsCharSet()` 区分 “charset” 关键字与非 text/html 的 content-type。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DocumentParser.java`（约 191-205 行）：解析 meta 标签时
  `if (!content.equalsIgnoreCase("text/html") && ...) throw new ChangedCharSetException(content, false);`
  `else if ("charset".equalsIgnoreCase(...)) throw new ChangedCharSetException(content, true);`
- 同目录 `Parser.java`：`handleEmptyTag` 声明 `throws ChangedCharSetException`，作为解析器的协议异常。

## 4. 测试用例设计（黑盒部分，`ChangedCharSetExceptionTest.testBlackBox`）
1. `new ChangedCharSetException("UTF-8", true)` → `getCharSetSpec()=="UTF-8"`、`keyEqualsCharSet()==true`。
2. `new ChangedCharSetException("windows-1252", false)` → spec/flag 精确往返。
3. 边界：空串 spec、null spec 原样返回；flag 为 true/false 均往返。
4. 类型链路：instanceof IOException / Exception。
