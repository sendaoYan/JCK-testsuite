# javax.xml.ws.soap.Addressing — 设计文档

## 一、API 功能
`@Addressing` 是 WS-Addressing 特性注解：成员 `enabled`（默认 true）、`required`（默认 false）、`responses`（AddressingFeature.Responses，默认 ALL）。运行时读取它启用/约束 WS-Addressing：required=true 时对端不支持即失败；responses 限定匿名/非匿名回复端点。

## 二、使用方式
标注在 SEI/实现类（TYPE）或方法/字段（兼容 JAX-WS 2.1 位置）。`@WebServiceFeatureAnnotation(id=AddressingFeature.ID, bean=AddressingFeature.class)` 元标注把它绑到特性 bean。测试覆盖成员默认值、@Target 集合、保留策略、元注解联动、自定义放置与 feature 桥接。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.soap.Addressing
- JSR 224 §9.6.2 Feature Annotations；WS-Addressing 2005/08
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.addressing.W3CAddressingFeature
- 本仓库测试：JDK-8/API/javax/xml/ws/soap/Addressing/AddressingTest.java

## 四、测试用例设计 + 易错规范细节
- 默认值矩阵：enabled=true、required=false、responses=ALL（实测，最常被记反的是 enabled 默认 true）。
- @Target 恰 {TYPE, METHOD, FIELD}；RUNTIME 保留；@Documented；非 @Inherited。
- 元注解：id 等于 AddressingFeature.ID（http://www.w3.org/2005/08/addressing/module）、bean 为 AddressingFeature.class。
- 放置：TYPE/METHOD/FIELD 三处合法放置均能读回；自定义成员值（enabled=false/required=true/ANONYMOUS）精确回读。
- 易错点：注解成员缺省值用 defaultValue() 判断；反射 getMethod("responses") 调用需 throws Exception。
