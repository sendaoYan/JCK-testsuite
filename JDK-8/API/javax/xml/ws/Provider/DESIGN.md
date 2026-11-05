# Provider 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.Provider<T> 是服务端消息级（非 SEI）端点接口（JSR 224 §7.3）：实现类以 @WebServiceProvider 标注，接收/返回整条消息（MESSAGE 模式：Source/SOAPMessage）或仅负载（PAYLOAD 模式：Source 或 JAXB 支撑对象），@ServiceMode 决定粒度（默认 PAYLOAD）。

## 2. 使用方式
典型路径：`@WebServiceProvider @ServiceMode(MESSAGE) class P implements Provider<Source> { public Source invoke(Source req) { ... } }` → Endpoint.publish。本测试：结构面 + UpperProvider（大写变换记录型）invoke 回读 + Source 形态恒等回环 + lambda 形态 + 异常透传 + 无状态复用。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/Provider.html）、JSR 224 (JAX-WS 2.2) §7.3、本仓库 ProviderTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：接口、泛型 <T>、恰 1 声明方法 invoke(T)→T（擦除 Object/Object）、public abstract、无受检异常（实现只能抛非受检）。
- invoke 闭环：UpperProvider("hello")→"HELLO"；请求原值记录保序；null 请求宽容返回 null；三次调用计数。
- Source 形态：Provider<Source> 恒等往返（同引用）；SOAPMessage 载荷类型存在性。
- lambda 形态：Provider<Integer> 双倍器 42→84。
- 异常透传：实现抛 ProtocolException("proto-err") 原样冒泡。
- 关联：@ServiceMode/@BindingType 注解存在性（详细断言在各自目录）；Service.Mode 枚举可达。
- 坑：invoke 无 throws 声明——受检异常必须包装；Provider 不能与 SEI 混用于同一端点。
