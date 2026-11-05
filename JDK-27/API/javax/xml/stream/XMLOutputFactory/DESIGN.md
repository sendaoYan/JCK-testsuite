# javax.xml.stream.XMLOutputFactory — 设计案

## 1. API 功能
javax.xml.stream.XMLOutputFactory 抽象类。常量：IS_REPAIRING_NAMESPACES（共 1 个）。 静态入口：newDefaultFactory, newInstance, newFactory。 实例/声明方法：createXMLStreamWriter, createXMLEventWriter, setProperty, getProperty, isPropertySupported。

## 2. Java 用户如何使用
newFactory → createXMLStreamWriter 写固定文档 → 字节级精确输出；createXMLEventWriter 组合事件。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLOutputFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLOutputFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLOutputFactoryTest.testBlackBox()`）覆盖：流式写往返、事件写、property 支持、未知 property。
## 5. 契约深化（本轮追加：修复命名空间/通道全等）
- IS_REPAIRING_NAMESPACES：默认 false、往返、未知名双抛 IAE；true 后未声明前缀自动补 xmlns（输出全等锁定）+ 重复声明抑制 + 修复输出回读闭环。
- Writer 与 OutputStream+UTF-8 输出逐字符全等；createXMLEventWriter(OutputStream,enc) 输出 `<?xml version="1.0" encoding="UTF-8"?><e></e>` 全等。
- newDefaultFactory() 行为断言（JDK-8 无此方法）。
