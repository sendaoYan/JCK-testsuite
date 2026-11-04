# CryptoPrimitive — 设计案

## 1. API 功能
密码原语枚举（10 常量，为 AlgorithmConstraints/SSLParameters 提供用途分类）：MESSAGE_DIGEST, SECURE_RANDOM, BLOCK_CIPHER, STREAM_CIPHER, MAC, KEY_WRAP, PUBLIC_KEY_ENCRYPTION, SIGNATURE, KEY_ENCAPSULATION, KEY_AGREEMENT。

## 2. 使用方式
`Set<CryptoPrimitive>` 传入 AlgorithmConstraints.permits / TLS 算法约束；EnumSet 组合。

## 3. 参考来源
- javadoc `java.security.CryptoPrimitive`（常量表与用途说明）。
- 反射实测声明序。

## 4. 测试用例设计 + 易错规范细节
- 常量面：恰 10 常量、名字精确；**声明序锁定 ordinal**：MD=0, SR=1, BC=2, SC=3, MAC=4, KEY_WRAP=5, PKE=6, SIGNATURE=7, KE=8, KA=9（KEY_WRAP 在 MAC 之后，不是末位——易错）。
- Enum 契约：valueOf 每常量、compareTo 与 ordinal 一致、toString==name、equals 恒等、values() 每次新数组、getDeclaringClass 为本枚举、getEnumConstants 镜像。
- 负例：大小写敏感 IAE（消息含名字）、无名常量 IAE、null NPE。
- 序列化：按 name 还原单例（==）。
- 组合：EnumSet.of/allOf/range（BLOCK_CIPHER..MAC 恰 3 个）、不相交分组。
