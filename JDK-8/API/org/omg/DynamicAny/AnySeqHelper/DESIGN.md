# org.omg.DynamicAny.AnySeqHelper

## API 功能
typedef sequence<any> Helper（create_dyn_any(TypeCode) 重载参数序列）。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- alias→sequence→**tk_any** 链精确；Any/CDR 双通道往返（异构元素 long+string；空序列边界）。
