## 契约深化（2026-11）

### API 功能
`java.lang.TypeNotPresentException(String typeName, Throwable cause)`：当应用试图访问以其名字表示的类型而该类型不存在时抛出（典型场景：注解成员的 Class 值引用的类缺失、反射泛型解析）。

## 使用方式
```java
throw new TypeNotPresentException("com.gone.Type", cause);
// getMessage() == "Type com.gone.Type not present"
```

## 参考来源
Oracle javadoc java.lang.TypeNotPresentException；javap -public（8/27 同构：(String,Throwable) 单构造器 + typeName()）。

## 测试用例设计 + 易错规范细节
- 消息模板 `Type <typeName> not present` 逐字；null 名 → `Type null not present`；空名 → `Type  not present`（双空格）。
- **(String, null) 构造也算"已设置 cause"**：再 initCause → IllegalStateException `Can't overwrite cause ...`（与 message-only 构造可 initCause 的行为相对）。
- getCause() 同一实例；instanceof RuntimeException。
- svuid = -5101214195716534496（两侧一致）；序列化往返 typeName/cause/suppressed 全保持。
- printStackTrace 含 "Suppressed:" 行。
- TypeNotPresentExceptionTest.java 现有 PASS 断言：37。
