# EntityResolver — 设计案

## 1. API 功能
实体解析钩子：resolveEntity(publicId, systemId) 返回替代 InputSource 或 null（默认解析）。

## 2. Java 用户如何使用
拦截外部 DTD/实体的网络/文件访问（离线解析、本地缓存）。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax/ptests；SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：resolver 返回的替代 InputSource 内容流进 characters（真闭环）；返回 null → 默认解析（缺文件 FileNotFoundException）；setEntityResolver/getEntityResolver 往返；DefaultHandler 默认返回 null。
