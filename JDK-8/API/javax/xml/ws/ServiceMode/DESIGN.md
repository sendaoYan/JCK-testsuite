# ServiceMode 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.ServiceMode 是 @Target({TYPE}) @Retention(RUNTIME) 注解（JSR 224 §7.3.1）：标注 Provider 实现类，value() 返回 Service.Mode——MESSAGE（整条协议消息含头）或 PAYLOAD（仅消息负载）。未标注默认 PAYLOAD。

## 2. 使用方式
典型路径：`@ServiceMode(Service.Mode.MESSAGE) @WebServiceProvider class P implements Provider<Source> {...}`。本测试：注解元数据（value 类型/Retention/Target）+ 标注回读（MESSAGE/PAYLOAD/未标注三态）+ Service.Mode 枚举全语义（values/valueOf/ordinal/name/compareTo/防御副本）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/ServiceMode.html 与 Service.Mode.html）、JSR 224 (JAX-WS 2.2) §7.3、本仓库 ServiceModeTest。

## 4. 测试用例设计 + 易错规范细节
- 注解结构：接口+注解、唯一成员 value()→Service.Mode；@Retention RUNTIME（运行时反射可读）；@Target 恰 {TYPE}（实测）。
- 标注回读：MESSAGE 形态 value()==Mode.MESSAGE；PAYLOAD 形态同理；未标注类注解为 null（容器按 PAYLOAD 解释——规范默认）；annotationType 同一性。
- **Mode 枚举精确语义（P0）**：values() 长度恰 2、顺序 [MESSAGE, PAYLOAD]；name/ordinal（0/1）；valueOf 精确（小写 "payload" 抛 IAE——大小写敏感）；compareTo ==-1/0/+1；equals/hashCode/toString（==name）；getDeclaringClass；values() 返回防御副本（改副本不影响后续）。
- 坑：enum compareTo 按声明序（ordinal 差）；Mode 是 Service 的嵌套类（Service$Mode），与 @ServiceMode 注解是两个不同类型——注解目录测注解、枚举语义此处一并锁定（Service/Mode 目录另有独立测试勿冲突）。
