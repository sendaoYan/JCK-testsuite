# org.omg.DynamicAny.NameDynAnyPairHelper

## API 功能
NameDynAnyPair struct Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- type() 精确：tk_struct/2 成员；id 为 FieldName 别名、value 为 **tk_objref**（与 NameValuePair 的 tk_any 对照）。
- insert 本地 DynAny → MARSHAL（离线无法 marshal 本地对象引用）——这是该 Helper 的确定性负例。
