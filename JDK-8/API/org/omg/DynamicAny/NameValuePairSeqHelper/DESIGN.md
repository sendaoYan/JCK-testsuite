# org.omg.DynamicAny.NameValuePairSeqHelper

## API 功能
typedef sequence<NameValuePair> Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- alias→sequence→struct 三层链精确；Any/CDR 往返（含空序列边界）。
