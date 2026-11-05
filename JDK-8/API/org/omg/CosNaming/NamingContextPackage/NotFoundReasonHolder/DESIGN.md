# org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder

## API 功能
NotFoundReason 枚举 Holder。
## 使用方式
默认 null；_read/_write；_type() tk_enum。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- **三常量循环**逐一过流并 == 归一断言（全表 wire 覆盖，不只单值）。
