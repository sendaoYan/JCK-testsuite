# org.omg.CosNaming.NamingContextPackage.CannotProceed

## API 功能
解析中断异常：字段 cxt(NamingContext) + rest_of_name(NameComponent[])。
## 使用方式
三构造链：()/ (NamingContext,NameComponent[]) / (String,NamingContext,NameComponent[])。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 字段名是 **cxt**（非 ctx）；null cxt 合法；数组引用原样；消息模式同 NotFound（repoId 双空格 reason）。
