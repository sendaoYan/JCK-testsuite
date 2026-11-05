# javax.xml.ws.WebServiceRef 测试设计

## 1. API 功能

`javax.xml.ws.WebServiceRef` 是 Web Service 资源注入注解（对标 JSR-250 `@Resource` 的 JAX-WS 特化版），可标注 TYPE / METHOD / FIELD，六成员：`name`（JNDI 环境项名，默认 ""）、`type`（资源类型，默认 `Object.class` 哨兵——"从注入点推导"）、`mappedName`（厂商专有映射名）、`value`（生成 Service 子类的类型，默认 `javax.xml.ws.Service` 自身）、`wsdlLocation`（覆盖 WSDL 位置）、`lookup`（JNDI 查找名，JAX-WS 2.2 新增）。注入语义由容器（Java EE / Servlet 环境）完成，SE 环境仅承载元数据。

## 2. 使用方式

```java
public static class FieldCarrier {
    @WebServiceRef(name = "echoSvc", type = Echo.class, value = EchoLookupService.class,
                   mappedName = "jboss/echo", wsdlLocation = "http://x/echo?wsdl",
                   lookup = "java:comp/env/echo")
    public Object fullField;

    @WebServiceRef
    public Object bareField;
}
```
本测试做反射级全覆盖：三目标位置（TYPE 类级声明、FIELD 字段注入、METHOD setter 注入）× 三形态（全成员 / 局部成员 / 裸标注），配合六成员默认值精确锁定（Object.class / Service.class 两个 Class 哨兵）。SE 无注入容器，运行时通道不适用（在 DESIGN 说明）。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServiceRef.html
- JSR 224 (JAX-WS 2.2) §7.8（WebServiceRef）；JSR-109 / Java EE 资源注入
- OpenJDK 8u RI（注解 API 源码；注入消费者在 GlassFish 容器侧，JDK 内无）
- 本仓库测试：`JDK-8/API/javax/xml/ws/WebServiceRef/WebServiceRefTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：成员集合恰 6 个；Target 恰 `{TYPE, METHOD, FIELD}`（无 PARAMETER）；六成员全部带默认值（与 FaultAction.className / WebServiceRefs.value 对比）。
- 默认值：name/mappedName/wsdlLocation/lookup 空串；type `Object.class`；value `javax.xml.ws.Service` 本体。
- 回读：FIELD 全成员六元组；FIELD 裸标注六默认；METHOD setter（type+lookup）；TYPE（type+name）；equals/hashCode/toString。
- 负例：未标注类 null。

易错规范细节：
1. **value 的默认值是 `javax.xml.ws.Service` 这个 Class 对象本身**（"未指定具体 Service 子类"哨兵），不是 `WebServiceRef.class`，也不是 null。
2. **type 的默认值是 `Object.class`**（哨兵：从注入点静态类型推导），不是 Service。
3. JDK-8 的 Target **不含 PARAMETER**（部分新框架版本加了参数注入），回归断言防漂移。
4. value 的泛型边界 `Class<? extends javax.xml.ws.Service>` 可用 `getGenericReturnType().getTypeName().startsWith(...)` 锁定。
5. 载体 Service 子类（用于 value 赋值）用 `super(null, qn)` 构造（无 WSDL 服务），不会在类加载/实例化时联网——但注意 WebServiceClient 坑：null URL 会读类上 wsdlLocation，故子类要么不标 @WebServiceClient，要么接受实例化期行为；本测试仅反射、不实例化载体。
6. 注入语义（容器调用 setter / 赋字段）在 SE 下不可测，勿写依赖注入的断言。
