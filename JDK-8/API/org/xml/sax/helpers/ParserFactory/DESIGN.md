# ParserFactory — 设计案

## 1. API 功能
SAX1 解析器工厂（deprecated）：makeParser()/makeParser(className) 按 org.xml.sax.parser 系统属性实例化。

## 2. Java 用户如何使用
纯遗留；已被 JAXP 取代。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc @deprecated。

## 4. 测试用例设计 + 自动化实现
黑盒：无属性时 makeParser() → NPE 且消息含 'sax.parser'；makeParser(未知类) → ClassNotFoundException；位于 org.xml.sax.helpers 包；@Deprecated 注解两侧差异——JDK-8 仅 javadoc 标记、JDK-27 带注解（is8 分支断言）。
