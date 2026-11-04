# KeyRep.Type — 设计案

## 1. API 功能
KeyRep 的枚举载荷类型：SECRET/PUBLIC/PRIVATE（声明序即 ordinal 0/1/2）+ values()/valueOf(String)。final enum，extends Enum&lt;KeyRep.Type&gt;。

## 2. 使用方式
`KeyRep.Type.PUBLIC` 等常量作 KeyRep 构造第一参；枚举标准契约（name/ordinal/compareTo/EnumSet/switch/序列化按 name 还原单例）。

## 3. 参考来源
- javadoc `java.security.KeyRep.Type`。
- OpenJDK `test/jdk/java/security/KeyRep`（思路借鉴）。

## 4. 测试用例设计 + 易错规范细节
- 常量面：恰 3 常量、名字精确、ordinal 按声明序（SECRET=0/PUBLIC=1/PRIVATE=2，实测锁定）。
- Enum 契约：valueOf 每常量身份相等、name()↔valueOf 往返、compareTo 与 ordinal 一致、toString==name、equals 恒等语义、hashCode 稳定。
- 数组契约：values() 每次新数组（可自由改写）、内容一致；getEnumConstants 镜像 values()。
- 负例：valueOf 大小写敏感（"secret"→IAE 且消息含名字）、无名常量 IAE、null→NPE。
- 序列化：枚举按 name 还原同一单例（==）。
- 组合：EnumSet.of/allOf 正确；switch 分派全分支。
