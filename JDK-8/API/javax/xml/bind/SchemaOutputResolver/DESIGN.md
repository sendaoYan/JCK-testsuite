# SchemaOutputResolver 设计案（JDK-8 契约测试）

## 1. API 功能
抽象 SAM：createOutput(namespaceUri, suggestedFileName) 返回 Result 供 generateSchema 写出 schema 文件。

## 2. 使用方式
子类覆写 createOutput 返回 StreamResult(临时文件) → ctx.generateSchema(resolver) 驱动写出。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- SAM 结构：唯一抽象方法、返回 Result、仅声明 IOException、一个 public 构造。
- 真实闭环：generateSchema 精确调用 1 次（实测）、无包命名空间解析为空串、建议名以 .xsd 结尾。
- null Result：实测 RI **容忍**（不抛 IOException）——勿断言中断。
- 二次独立运行同样一次解析（无跨调用状态）。
