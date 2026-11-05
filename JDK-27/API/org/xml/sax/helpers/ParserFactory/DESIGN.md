# org.xml.sax.helpers.ParserFactory — 设计案

## 1. API 功能
org.xml.sax.helpers.ParserFactory 类。静态入口：makeParser。

## 2. Java 用户如何使用
makeParser()/makeParser(className)。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.ParserFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ParserFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ParserFactoryTest.testBlackBox()`）覆盖：两种 makeParser。

### 深化轮次2（行为契约）
黑盒：makeParser() 无属性 → NPE 含 'sax.parser'；makeParser(未知类) → ClassNotFoundException；@Deprecated 注解 JDK-8 缺失 vs JDK-27 存在（版本差异）。
