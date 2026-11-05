# javax.xml.ws.soap.MTOM — 设计文档

## 一、API 功能
`@MTOM` 是 SOAP MTOM（Message Transmission Optimization Mechanism，XOP 附件优化）特性注解：成员 `enabled`（默认 true）、`threshold`（默认 0，字节阈值——超过才内联为 MIME 部分）。元标注 @WebServiceFeatureAnnotation(id=MTOMFeature.ID, bean=MTOMFeature.class)。

## 二、使用方式
标注在服务实现类/SEI 上启用 MTOM；编程式用 MTOMFeature。threshold 控制优化开关的体积下限。测试覆盖默认值、@Target 放置、注解↔feature 默认值互通、成员显式赋值回读。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.soap.MTOM
- JSR 224 §9.6.2；SOAP MTOM 1.0 (W3C)
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.encoding.MtomCodec
- 本仓库测试：JDK-8/API/javax/xml/ws/soap/MTOM/MTOMTest.java

## 四、测试用例设计 + 易错规范细节
- 默认值：enabled=true、threshold=0（实测；与 MTOMFeature 无参构造一致——两处断言交叉锁定）。
- @Target {TYPE, METHOD, FIELD}；RUNTIME 保留；@Documented；非 Inherited。
- 显式成员：enabled=false、threshold=4096 精确回读；注解在接口方法/字段上放置合法。
- 与 feature 桥接：MTOMFeature.ID 等于注解 id；bean 为 MTOMFeature.class；new MTOMFeature() 与注解默认态等值。
- 易错点：threshold 单位是字节且 0 表示"总是优化"；负 threshold 在 feature 构造抛 WebServiceException（注解层编译期常量不受校验）。
