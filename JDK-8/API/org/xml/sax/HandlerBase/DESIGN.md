# HandlerBase — 设计案

## 1. API 功能
SAX1 四合一空基类（org.xml.sax 包）：实现 EntityResolver/DTDHandler/DocumentHandler/ErrorHandler，resolveEntity 默认 null。

## 2. Java 用户如何使用
SAX1 时代便捷基类；现用 helpers.DefaultHandler。

## 3. 开源代码实际用法调研
SAX 1.0 javadoc；注意 JDK 把它放在 org.xml.sax 而非 sax.helpers。

## 4. 测试用例设计 + 自动化实现
黑盒：四接口全实现；resolveEntity 默认 null；DefaultHandler 空解析全程静默；子类覆写记录 SAX1 事件流；JAXP SAX1 通道可用其做 handler。易错：包名是 org.xml.sax.HandlerBase（JDK 特有迁移位置）。
