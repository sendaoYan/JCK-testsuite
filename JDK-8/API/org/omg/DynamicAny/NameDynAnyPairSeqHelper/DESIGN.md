# org.omg.DynamicAny.NameDynAnyPairSeqHelper

## API 功能
typedef sequence<NameDynAnyPair> Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- alias→sequence→struct 链精确；含本地 DynAny 的序列 insert → MARSHAL 负例。
