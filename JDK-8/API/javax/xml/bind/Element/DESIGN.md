# Element 设计案（JDK-8 契约测试）

## 1. API 功能
JAXB 1.0 遗留标记接口（无方法），历史上用于标示"可作为元素序列化的类"；JAXB 2.x 下仅保留兼容。

## 2. 使用方式
用户 bean 可 implements javax.xml.bind.Element 且不干扰注解映射。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 标记接口：无方法/无字段/无父接口/非泛型。
- 实现类闭环：Marked 实现 Element + @XmlRootElement(name="marked")：marshal JAXBElement 用 qname 命名；marshal 实例用注解根名；unmarshal 回读 payload 且类型仍实现标记。
- 易错点：**JAXBElement 不实现 Element**（实测）；实现该接口不影响映射行为。
