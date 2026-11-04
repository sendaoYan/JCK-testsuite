# java.security.DigestException

## 1. API 功能
消息摘要（MessageDigest）相关异常，继承 `java.security.GeneralSecurityException`。四个构造器：
- 无参 `DigestException()`
- `DigestException(String msg)`
- `DigestException(String message, Throwable cause)`（@since 1.5）
- `DigestException(Throwable cause)`（@since 1.5）

## 2. Java 用户如何使用
`MessageDigest.digest(byte[], int, int)` 等带输出缓冲区的方法在缓冲区不足或不支持分片摘要时抛出；用户捕获后读取 `getMessage()` 获得具体原因。
```java
try {
    md.digest(buf, 0, len);
} catch (DigestException e) {
    System.out.println(e.getMessage());
}
```

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/MessageDigestSpi.java:185` — `throw new DigestException("partial digests not returned");`
- `src/java.base/share/classes/java/security/MessageDigestSpi.java:187` — `throw new DigestException("insufficient space in the output ...");`
- `src/java.base/share/classes/java/security/MessageDigest.java:71`（javadoc 示例）— `throw new DigestException("couldn't make digest of partial content");`
- `test/jdk/java/security/MessageDigest/TestSameValue.java:95` — `throws DigestException`（自定义 SPI 引擎声明）

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖四个构造器：
- 无参：非 null、`getMessage()==null`、`getCause()==null`。
- String：`getMessage().equals("boom")`、`getCause()==null`。
- (String, Throwable)：`getMessage().equals("msg")`、`getCause()==t`。
- (Throwable)：`getCause()==t`、`getMessage()!=null`。
- 继承：`instanceof GeneralSecurityException`。
白盒断言：public、具体类、父类 `GeneralSecurityException`、无接口、4 个构造器、无声明方法。


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（5821450303093652515，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- 真实路径判定：`len < digestLength` → DigestException（消息含 "16"）；而 `offset+len > buf.length` → **IllegalArgumentException**（易混淆点，两条路径分别断言）。
