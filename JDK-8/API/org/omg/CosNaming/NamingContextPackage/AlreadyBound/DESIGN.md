# org.omg.CosNaming.NamingContextPackage.AlreadyBound

## API 功能
bind 目标已存在时抛出的 UserException。
## 使用方式
构造链 ()/ (String)。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- 消息模式 repoId（双空格 reason）；toString 精断言。
