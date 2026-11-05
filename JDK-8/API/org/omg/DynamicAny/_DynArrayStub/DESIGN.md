# org.omg.DynamicAny._DynArrayStub

## API 功能
对应 DynAny 类型的客户端桩。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- `new` 不抛；_ids() **整表精确** `IDL:omg.org/DynamicAny/DynArray:1.0, IDL:omg.org/DynamicAny/DynAny:1.0`（派生 id 在前、基类 id 依序在后）；离线操作 → BAD_OPERATION。
