# org.omg.PortableServer.ServantRetentionPolicyOperations 契约覆盖设计（仅 JDK-8）

## API 功能
策略值读取操作接口：唯一方法 `value() -> ServantRetentionPolicyValue`；直接父接口仅 `org.omg.CORBA.PolicyOperations`。

## 使用方式
作为 ServantRetentionPolicy 的 Operations 基面，由客户端桩与服务端 tie 共同实现。

## 参考来源
- OMG IDL-to-Java 映射（interface 拆 Operations/继承体）；jdk8u372-b07 javap

## 测试用例设计 + 易错细节
1. value() 签名精确（返回 ServantRetentionPolicyValue、抽象）。
2. 自有方法恰 1 个；直接父接口恰 1 个（PolicyOperations）——IDL 双接口形态。
3. PolicyOperations 基面三方法计数锁 3。
