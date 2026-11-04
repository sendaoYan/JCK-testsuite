# java.security.KeyStore.CallbackHandlerProtection 黑盒行为测试设计

## 1. API 功能
ProtectionParameter 实现，包装 `javax.security.auth.callback.CallbackHandler`，唯一方法 `getCallbackHandler()`；用于 PKCS12 等需要在回调里取口令的装载场景。

## 2. 使用方式
`new KeyStore.CallbackHandlerProtection(handler)` → 传给 `KeyStore.Builder.newInstance(ks, pp)` 或 `ks.getEntry(alias, prot)`；handler 内自行响应 PasswordCallback。

## 3. 参考来源
- javadoc `java.security.KeyStore.CallbackHandlerProtection`。
- OpenJDK `test/jdk/java/security/KeyStore/CallbackHandlerProtection/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 构造：null handler → NullPointerException；正常构造 getCallbackHandler 身份往返；计数 handler 记录 handle 调用次数。
- 结构：public、具体、非 final（可被继承）；identity 相等（无 equals/hashCode 覆写）。
- PasswordCallback 契约：`getPassword()` 每次返回新克隆（两次调用 `!=`）；clearPassword 以空格覆写而非置 null。
- Builder 集成：先 `getKeyStore()` 再 `getProtectionParameter`（顺序颠倒 → IllegalStateException）；getEntry("missing", chp) 返回 null（missing alias 不触发回调）。
- 易错点：JDK-8 下 JKS getEntry 用 CHP 取既有 key 条目会 UnsupportedOperationException——UOE 只对存在条目抛出。

断言入口：`CallbackHandlerProtectionTest.java` 的 `testContractDeep()`（原有结构断言保留）。
- 补充：JCEKS 既有 key 条目 getEntry(CHP) → UOE；PasswordCallback prompt/echoOn 往返；空 Callback 数组同样调用 handler。
