# org.omg.DynamicAny._DynValueStub

## API 功能
DynValue 客户端桩。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- _ids() **3 元素整表**：DynValue → DynValueCommon → DynAny（派生在前）；离线操作 → BAD_OPERATION。
