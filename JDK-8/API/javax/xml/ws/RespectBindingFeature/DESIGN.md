# RespectBindingFeature 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.RespectBindingFeature 是 WSDL 绑定尊重特征（JSR 224 §7.9.1，JAX-WS 2.2）：启用时运行时必须尊重 WSDL 绑定中声明的策略/特性（如 wsaw:UsingAddressing）。ID 常量 "javax.xml.ws.RespectBindingFeature"；对应注解 @RespectBinding（root 包）经 @WebServiceFeatureAnnotation 桥接。

## 2. 使用方式
典型路径：`svc.getPort(portQn, Echo.class, new RespectBindingFeature());` 或 SEI/impl 上 `@RespectBinding(enabled=true)`。本测试：构造矩阵（()/true/false）+ ID 常量/结构面 + @RespectBinding 注解联动（默认 enabled true、Retention/Target、元注解 id/bean 对偶）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/RespectBindingFeature.html 与 RespectBinding.html）、JSR 224 (JAX-WS 2.2)、本仓库 RespectBindingFeatureTest。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵（P0）：() 默认 enabled **true**；(true)/(false) 精确回读；getID 与 enabled 态无关恒 "javax.xml.ws.RespectBindingFeature"。
- 结构面：final 类、extends WebServiceFeature、ID public static final String==getID()、getID 本类覆写、isEnabled 继承基类（未覆写）、恰 2 public 构造器（无参+boolean）。
- **注解联动（P0）**：@RespectBinding meta-annotated @WebServiceFeatureAnnotation（spi 包）且 id==特征 ID、bean==RespectBindingFeature.class；@RespectBinding.enabled() 默认 **TRUE**（反射 getDefaultValue）；三态标注回读（默认/true/false）；未标注类 null。
- 注解元数据：@Retention RUNTIME；**@Target {TYPE, METHOD, FIELD}**（实测三元素，勿断言仅 TYPE）。
- 坑：ID 串恰为全限定类名（非 URI 形态，与 AddressingFeature 等不同）；enabled 默认 true（多数特征如此，但 MTOM 变体亦有 false 构造，勿泛化）。
