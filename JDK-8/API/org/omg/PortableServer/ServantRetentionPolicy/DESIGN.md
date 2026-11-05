# org.omg.PortableServer.ServantRetentionPolicy 契约覆盖设计（仅 JDK-8）

## API 功能
POA 策略对象接口（IDL interface 继承体）：extends `{ServantRetentionPolicyOperations, org.omg.CORBA.Policy, IDLEntity}`；
本体**不声明任何方法**，value() 在 ServantRetentionPolicyOperations 上。

## 使用方式
POA.create_*_policy(...) 工厂产出；调用方读 value() 取策略值、policy_type() 读注册号（SERVANT_RETENTION_POLICY_ID==21）。
接口离线不可直接实现（Policy 继承 ObjectOperations 大面）——用 java.lang.reflect.Proxy 做行为面。

## 参考来源
- OMG POA policy 模型；jdk8u372-b07 javap + 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. 形态：本体自有方法数==0；extends 恰三接口且**声明顺序精确**（Operations/Policy/IDLEntity）。
2. Operations 面：恰一自有方法 value()（抽象、返回类型精确到 ServantRetentionPolicyValue）；父接口仅 PolicyOperations；
   PolicyOperations 恰三方法（policy_type:int / copy:Policy / destroy:void）。
3. Proxy 行为面：value()/policy_type() 调用确实路由到 InvocationHandler（计数证明）。
4. **易错**：策略接口本体 extends Policy（含 ObjectOperations 全套），匿名类实现不现实，Proxy 是标准通道。
