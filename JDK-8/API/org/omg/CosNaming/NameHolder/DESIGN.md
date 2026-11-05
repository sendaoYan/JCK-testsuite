# org.omg.CosNaming.NameHolder

## API 功能
Name（NameComponent[] typedef）Holder。
## 使用方式
默认 null；_type() tk_alias；_read/_write 数组往返。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 2 元素数组往返逐字段；与 NameHelper.write 流互通。
