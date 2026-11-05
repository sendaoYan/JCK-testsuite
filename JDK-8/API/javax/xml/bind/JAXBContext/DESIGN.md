# JAXBContext 设计案（JDK-8 契约测试）

## 1. API 功能
JAXB 抽象工厂/入口：newInstance（String 路径 / Class 列表 / 带 Map 属性的重载）引导出上下文；createMarshaller/createUnmarshaller/createBinder/createJAXBIntrospector 派生组件；generateSchema 驱动 SchemaOutputResolver 写 XSD；createValidator 为 JAXB 1.0 遗留（RI 2.x 抛 UnsupportedOperationException，实测锁定）。

## 2. 使用方式
静态 newInstance → 持有上下文 → 每次操作新建 Marshaller/Unmarshaller（可复用但非线程安全）。本测试以 @XmlRootElement + XmlAccessType.FIELD 的静态嵌套 Person 建立真实上下文。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 结构：抽象类、无 public 构造、newInstance 四种重载签名面（Class.../String,ClassLoader/String,ClassLoader,Map/Class[],Map——注意没有 (Class[], ClassLoader) 形态）。
- newInstance 矩阵：合法类、空参（实测不抛，返回空上下文）、未知路径（JAXBException 消息含 ObjectFactory/jaxb.index）、null 元素。
- **闭环（P0）**：marshal 输出元素名映射（person/name/age 小写默认映射）、unmarshal 回读字段全等；JAXB_FORMATTED_OUTPUT/JAXB_ENCODING/JAXB_SCHEMA_LOCATION 属性读写。
- generateSchema：resolver 精确调用 1 次（实测），无包命名空间解析为空串。
- 易错点：
  - createValidator 在 JDK-8 RI 抛 UnsupportedOperationException 而非返回 deprecated 对象。
  - newInstance() 无参**不抛**异常（规范允许空上下文）。
  - 属性默认值：JAXB_ENCODING=="UTF-8"；schemaLocation 类默认 **null** 而非 ""（实测）。
  - generateSchema 的 resolver 调用次数是 1（仅根 schema），不要断言 2。
