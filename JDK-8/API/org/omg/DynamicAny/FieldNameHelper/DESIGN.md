# org.omg.DynamicAny.FieldNameHelper

## API 功能
typedef FieldName（string 别名）Helper。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- alias→tk_string；insert 后 Any 保留别名类型；Any/CDR 往返 + 空串边界 + Latin-1 非 ASCII 往返（离线码集 ISO-8859-1，CJK 会 DATA_CONVERSION）。
