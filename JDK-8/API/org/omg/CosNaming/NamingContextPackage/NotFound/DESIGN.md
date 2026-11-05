# org.omg.CosNaming.NamingContextPackage.NotFound

## API 功能
resolve/unbind 等查名字失败时抛出的 UserException：字段 why(NotFoundReason) + rest_of_name(NameComponent[])。
## 使用方式
三构造链：()/ (NotFoundReason,NameComponent[]) / (String,NotFoundReason,NameComponent[])。
## 参考来源
JDK-8 rt.jar idlj 异常映射；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- idlj 消息模式：无 reason → getMessage()==repository id；有 reason → `repoId + "  " + reason`（两个空格）——toString 精确锁死。
- 字段构造数组引用原样保存；no-arg 字段均 null；why 常量 ==。
