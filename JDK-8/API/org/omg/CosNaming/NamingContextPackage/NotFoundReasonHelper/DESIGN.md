# org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper

## API 功能
NotFoundReason 枚举 Helper。
## 使用方式
静态 insert/extract/read/write、type()/id()。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- type tk_enum、member_count==3、成员名顺序精确；**三常量循环** Any+CDR 双通道 == 归一全表覆盖。
