# javax.xml.ws.spi.WebServiceFeatureAnnotation — 设计文档

## 一、API 功能
`@WebServiceFeatureAnnotation` 是特征注解的元注解：恰 2 个必填成员——`id`（String，特征 URI/标识，无默认值）与 `bean`（Class<? extends WebServiceFeature>，对应特性类，无默认值）。@Target 恰 {ANNOTATION_TYPE}（只能标在注解类型上），RUNTIME 保留，@Documented。@Addressing/@MTOM/@RespectBinding 均以它标注。

## 二、使用方式
自定义特性注解时：`@WebServiceFeatureAnnotation(id="urn:my", bean=MyFeature.class)` 元标注自己的 @interface；运行时按它发现特性 bean 并实例化。测试验证成员无默认值（必填）、@Target 精确集合、三个标准注解的 id/bean 联动、自定义元标注回读、非特性注解（@WebService）不携带。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.spi.WebServiceFeatureAnnotation
- JSR 224 §9.6.2
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.feature至此由 WSService 解析
- 本仓库测试：JDK-8/API/javax/xml/ws/spi/WebServiceFeatureAnnotation/WebServiceFeatureAnnotationTest.java

## 四、测试用例设计 + 易错规范细节
- 实测纠偏：@Target 是 ANNOTATION_TYPE 而非 TYPE（与普通注解不同）；id/bean 均无默认值（defaultValue()==null）。
- bean 泛型签名精确："java.lang.Class<? extends javax.xml.ws.WebServiceFeature>"。
- 联动表：Addressing → AddressingFeature.ID/bean；MTOM → MTOMFeature.ID；RespectBinding → "javax.xml.ws.RespectBindingFeature"（简式 id，非 URI）；三者 bean 均为 WebServiceFeature 子类。
- 自定义元标注 @interface 需在测试文件 import 本注解（否则不能用作元标注）。
- 易错点：RespectBinding 的 id 是类名式而非 URI——断言按注解实际值而非想当然 URI。
