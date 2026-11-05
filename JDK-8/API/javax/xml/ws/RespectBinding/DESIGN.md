# javax.xml.ws.RespectBinding 测试设计

## 1. API 功能

`javax.xml.ws.RespectBinding` 声明"是否尊重 WSDL 中的绑定声明"：单成员 `enabled()`（boolean，默认 true）。它可标注于 TYPE、METHOD、FIELD 三类位置，是 JAX-WS 声明式特性注解家族的一员——通过 `@WebServiceFeatureAnnotation` 与 `javax.xml.ws.RespectBindingFeature` 关联，可被运行时翻译为 Feature 对象参与 `Service.getPort` / `Endpoint` 的特性协商。语义上 enabled=true 时运行时必须读取 `@WebService.wsdlLocation` 指向的 WSDL 并遵循其绑定扩展（如 SOAP 版本、MTOM）。

## 2. 使用方式

```java
@WebService(serviceName = "rbOn", wsdlLocation = "file:///path/real.wsdl")
@RespectBinding(enabled = true)
public static class RbOnImpl { public String echo(String s) { return s; } }

Endpoint e = Endpoint.create(new RbOnImpl());
e.publish("http://localhost:0/rb");  // publish 时解析 wsdlLocation
```
本测试做三通道：①反射面（TYPE/FIELD/METHOD 三种合法位置回读 + enabled 取值矩阵）；②元数据面（第四个声明元注解 `spi.WebServiceFeatureAnnotation` 的 `bean() == RespectBindingFeature.class`）；③运行时面（坏 wsdlLocation 在 publish 期触发 WebServiceException；无 wsdlLocation 正常发布-停止闭环）。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/RespectBinding.html
- JSR 224 (JAX-WS 2.2) §4.2.3（RespectBindingFeature）；javax.xml.ws.spi.WebServiceFeatureAnnotation
- OpenJDK 8u RI（`com.sun.xml.internal.ws.server.EndpointFactory` 按 wsdlLocation 装配）
- 本仓库测试：`JDK-8/API/javax/xml/ws/RespectBinding/RespectBindingTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：单成员 enabled（primitive boolean，默认 true）；Target 恰 `{TYPE, METHOD, FIELD}`（**无 PARAMETER**）；**声明元注解是 4 个**（Target+Retention+Documented+WebServiceFeatureAnnotation）——注解族里唯一。
- 特性关联：`WebServiceFeatureAnnotation.bean() == RespectBindingFeature.class`，`id()` 非空。
- 回读：TYPE 上 enabled=true、FIELD 上 enabled=false、METHOD 裸标注默认 true；equals/hashCode/toString。
- 运行时：create 级不解析 WSDL（容忍坏 wsdlLocation）；publish 无 wsdlLocation → 成功闭环；publish 坏 wsdlLocation（enabled=true）→ WebServiceException。
- 回归：JDK-8 中 `RespectBindingFeature` 类自身**未**回标 `@RespectBinding`。

易错规范细节：
1. **getDeclaredAnnotations().length == 4 而非 3**——WebServiceFeatureAnnotation 是 spi 包的关联器，容易在"声明面元注解恰 3 个"的族模式上踩空。
2. **RI 与规范的差异**：规范说 enabled=false 时运行时**可**忽略 wsdlLocation；JDK-8 RI 实测 enabled=false + 坏 wsdlLocation 的 publish **同样抛**（ServerRtException → WebServiceException）。测试对 enabled=false 分支采用双容忍断言（成功 stop 或抛 WebServiceException 均记录为通过），并在 DESIGN 锁定该差异。
3. publish 失败抛的是 `com.sun.xml.internal.ws.server.ServerRtException`（extends JAXWSExceptionBase extends **javax.xml.ws.WebServiceException**）——断言用 WebServiceException，勿锁实现类名。
4. enabled 返回 primitive boolean，getDefaultValue 回读为 Boolean 装箱值 true。
