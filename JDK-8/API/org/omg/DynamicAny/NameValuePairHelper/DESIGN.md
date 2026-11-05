# org.omg.DynamicAny.NameValuePairHelper

## API 功能
NameValuePair struct Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- type() 精确解剖：tk_struct/名/2 成员；**id 成员是 FieldName 别名**（tk_alias）而非裸 string——IDL 原文用 FieldName。
- Any 往返返回**新拷贝** Any（wire 拷贝语义）；CDR 通道同验。
