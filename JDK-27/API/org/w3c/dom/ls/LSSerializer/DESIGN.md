# org.w3c.dom.ls.LSSerializer — 设计案

## 1. API 功能
org.w3c.dom.ls.LSSerializer 接口。实例/声明方法：getDomConfig, getNewLine, setNewLine, getFilter, setFilter, write, writeToURI, writeToString。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ls.LSSerializer() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ls.LSSerializer）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 LSSerializerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`LSSerializerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- write→parse 往返闭环：writeToString 出声明+属性+文本，重新 parse 后根/属性/文本全恢复；源树不被序列化改变。
- write(LSOutput) 与 writeToString 正文一致；newLine 默认非 null、setNewLine("
") 往返；format-pretty-print 可设且以 4 空格缩进。
- 版本差异（双侧各自断言）：JDK 8 writeToString/write(LSOutput) 声明后换行、LSOutput 无编码时声明 UTF-8、writeToURI 成功写文件；JDK 27 声明后不换行、LSOutput 无编码仍声明 UTF-16、writeToURI 抛 LSException（NPE charsetName，Xerces 回归）。
