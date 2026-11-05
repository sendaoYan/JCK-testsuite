# org.omg.CosNaming.NamingContextExtHelper

## API 功能
NamingContextExt（URL/字符串名字扩展接口）对象引用 Helper。
## 使用方式
同 NamingContextHelper 模式。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- id `IDL:omg.org/CosNaming/NamingContextExt:1.0`、tk_objref；nil 三通道；narrow(null)→null；负例 MARSHAL。
