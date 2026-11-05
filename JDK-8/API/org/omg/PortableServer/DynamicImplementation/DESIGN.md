# org.omg.PortableServer.DynamicImplementation 契约覆盖设计（仅 JDK-8）

## API 功能
DSI（Dynamic Invocation）servant 基类：extends Servant，唯一自有抽象方法 `invoke(ServerRequest)`。

## 使用方式
匿名/命名子类实现 invoke + _all_interfaces；ORB 将请求整包交给 invoke。

## 参考来源
- OMG POA DSI；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 仅 1 个自有声明方法 invoke（abstract）；extends Servant 精确。
2. 匿名子类闭环：_all_interfaces 原样返回；invoke 直调可路由。
3. 未关联 ORB 继承 Servant 的 BAD_INV_ORDER 契约（消息精确）。
