# WebServiceContext 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.WebServiceContext 是服务端注入接口（JSR 224 §7.8.1）：容器以 @Resource 注入实现类字段，运行期提供——getMessageContext()（当前请求的消息上下文，handler/service 层共享）、getUserPrincipal()/isUserInRole()（安全主体与角色）、getEndpointReference(Element...) 两种形态（端点引用导出）。

## 2. 使用方式
典型路径：`@WebService class Impl { @Resource WebServiceContext ctx; void m() { MessageContext mc = ctx.getMessageContext(); ... } }`。本测试：自造 SimpleContext（HashMap 支撑的 MessageContext + Principal + 角色集合）注入视角锁定消息上下文 put/get、scope 语义、principal/role 矩阵；EPR 形态以 UnsupportedOperationException 记录（容器职责）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServiceContext.html）、JSR 224 (JAX-WS 2.2)、本仓库 WebServiceContextTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：接口、恰 5 声明方法；getMessageContext→MessageContext、getUserPrincipal→Principal、isUserInRole(String)→boolean、getEndpointReference(Element...) 两种（**注意 varargs 形态没有无参 getMethod**，反射要用 Element[].class）。
- 消息上下文闭环（P0）：put/get 跨 getMessageContext() 调用可见；containsKey/size/remove；非 String 值。
- MessageContext.Scope：**恰 2 值 APPLICATION/HANDLER**（JAX-WS 2.2 无 SYSTEM，实测枚举长度 2）；setScope/getScope 往返（自造实现存 map）；默认 HANDLER。
- 安全契约：未认证 principal null、未知角色 false；注入 principal 同一实例回读；角色集合 contains 语义（含空串 false）。
- 坑：getEndpointReference varargs 反射签名必须带 Element[] 参数类型；注入场景（@Resource）本测试不做，以自造实现锁定接口契约。
