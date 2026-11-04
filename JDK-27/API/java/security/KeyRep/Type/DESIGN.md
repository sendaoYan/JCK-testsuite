# java.security.KeyRep.Type 黑盒行为测试设计

## 1. API 功能
- 枚举：`SECRET`、`PUBLIC`、`PRIVATE`；`values()`、`valueOf(String)`。

## 2. Java 用户如何使用
作为 `KeyRep` 构造的第一个参数，指明要重建的密钥种类。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/KeyRep.java`：readResolve 按 Type 分支（SECRET/PUBLIC/PRIVATE）。
- `test/jdk/java/security/KeyRep/Serial.java`：KeyRep(Type, ...) 构造。

## 4. 测试用例设计 + 自动化实现
- values 长度 3；valueOf("SECRET"/"PUBLIC"/"PRIVATE") 与常量一致；name/valueOf 往返；ordinal 顺序稳定。
- 白盒：enum + SECRET/PUBLIC/PRIVATE 常量 + valueOf/values。

## 5. 追加契约维度（深化批）
- ordinal 锁定声明序 SECRET=0/PUBLIC=1/PRIVATE=2；compareTo 与 ordinal 一致。
- values() 每次新数组；getDeclaringClass 为本枚举；getEnumConstants 镜像。
- 负例：valueOf 大小写敏感/无名常量 → IAE（消息含名字）、null → NPE。
- 枚举序列化按 name 还原同一单例（==）；EnumSet of/allOf 组合。
