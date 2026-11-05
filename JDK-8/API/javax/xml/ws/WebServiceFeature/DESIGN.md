# WebServiceFeature 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.WebServiceFeature 是可启用特征抽象基类（JSR 224 §7.10，JAX-WS 2.2）：protected boolean enabled 字段承载开关；getID() 抽象（每特征唯一 URI/类名串）；isEnabled() 具体方法读字段。子类：RespectBindingFeature（根包）、soap.AddressingFeature、soap.MTOMFeature。特征实例经 getPort/create/createDispatch/publish 的 WebServiceFeature... 变参传递，或经对应注解（@RespectBinding/@Addressing/@MTOM）静态声明——两态经 @WebServiceFeatureAnnotation（spi 包）元注解桥接。

## 2. 使用方式
典型路径：`svc.getPort(portQn, Echo.class, new AddressingFeature()); Endpoint.create(impl, new MTOMFeature(true));`。本测试：结构面 + BoolFeature/AlwaysOnFeature 自造子类锁定 protected 契约 + 内置三特征共享面 + @WebServiceFeatureAnnotation 桥接。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServiceFeature.html）、JSR 224 (JAX-WS 2.2)、本仓库 WebServiceFeatureTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：抽象类；getID() 抽象 String、isEnabled() 具体 boolean（读 protected enabled 字段——反射验证 protected/boolean/instance）；protected 无参构造、无 public 构造、直接 newInstance 失败。
- 子类契约（P0）：protected 默认构造 → enabled false；protected 字段在子类内写（flip 方法）→ isEnabled 可见翻转；getID 覆写回读且跨调用稳定；isEnabled 覆写优先于字段（AlwaysOnFeature）；基类引用多态 getID/isEnabled；特征数组/空数组（变参形态）合法。
- 内置特征面：RespectBindingFeature（默认 true、getID==类名）；soap.AddressingFeature（默认 true）；soap.MTOMFeature（isEnabled 可调用）。
- **@WebServiceFeatureAnnotation 桥接（P0）**：@RespectBinding 上元注解存在；其 id()==特征.getID()、bean()==RespectBindingFeature.class（id/bean 双成员反射锁定）。
- 坑：protected 字段从测试外部类直接访问非法（须在子类方法内写）；不同 enabled 态 getID 不变。
