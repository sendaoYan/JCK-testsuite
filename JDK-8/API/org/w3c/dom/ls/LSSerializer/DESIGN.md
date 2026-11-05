# org.w3c.dom.ls.LSSerializer — 设计案

## 1. API 功能
LSSerializer 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- write→parse 往返闭环：writeToString 出声明+属性+文本，重新 parse 后根/属性/文本全恢复；源树不被序列化改变。
- write(LSOutput) 与 writeToString 正文一致；newLine 默认非 null、setNewLine("
") 往返；format-pretty-print 可设且以 4 空格缩进。
- 版本差异（双侧各自断言）：JDK 8 writeToString/write(LSOutput) 声明后换行、LSOutput 无编码时声明 UTF-8、writeToURI 成功写文件；JDK 27 声明后不换行、LSOutput 无编码仍声明 UTF-16、writeToURI 抛 LSException（NPE charsetName，Xerces 回归）。