# javax.management.openmbean.InvalidKeyException — 测试设计

## 1. API 功能
unchecked 异常（直接 extends IllegalArgumentException；**不是** KeyAlreadyExistsException 的子类，
两者是 IAE 下的兄弟）。构造链 ()/(String)；SUID=4224269443946322062L。

## 2. Java 用户典型用法
CompositeData.get(key)/getAll/remove 等按 item 名取值时 key 不存在。

## 3. 参考来源
- Oracle javadoc；OpenJDK 源码 CompositeDataSupport.get（key 校验两段：null/empty 与不存在）。

## 4. 测试用例设计 + 易错细节（InvalidKeyExceptionTest，批 G）
- 层级/unchecked/两构造器数/SUID/round-trip；与 KeyAlreadyExistsException 无继承关系断言。
- 真实触发点精确消息：get("missing") → "Argument key=\"missing\" is not an existing item name
  for this CompositeData instance."
- **对照**：get(null) → 普通 IllegalArgumentException "Argument key cannot be a null or empty
  String."（不是 InvalidKeyException）——空/坏 key 与不存在 key 走不同异常。
- 版本差异：两侧零差异。
