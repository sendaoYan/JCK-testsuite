# java.security.MessageDigestSpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`MessageDigest` 外观的 SPI 后端；public 无参构造。
- 3 个抽象方法：`engineReset()`、`engineUpdate(byte)`、`engineUpdate(byte[],int,int)`、`engineDigest()`（JDK-8 共 4 个抽象）。
- 默认实现：`engineGetDigestLength()`（默认返回 0）、`engineDigest(byte[],int,int)`（默认依赖 `engineDigest()` 拷贝）、`engineUpdate(ByteBuffer)`（默认转调数组变体）、`clone()`（默认抛 `CloneNotSupportedException`）。

## 2. Java 用户如何使用
provider 继承 MessageDigestSpi 实现 4 个抽象方法，经 `Provider` 服务注册后由 `MessageDigest.getInstance()` 装配；用户只接触 `MessageDigest` 外观。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/MessageDigestSpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/MessageDigest/`
- SUN provider 实现 `sun.security.provider.DigestBase`（engineUpdate(ByteBuffer) 批量语义参照）

## 4. 测试用例设计 + 自动化实现
- 结构：abstract 类、公共无参构造、抽象方法数与名字精确、`engineGetDigestLength` 非 abstract。
- 透明记录子类：覆写全部抽象方法记录实参，断言 `engineUpdate(b)`/`engineUpdate(buf,off,len)` 精确透传（记录 off/len 而非 0,len）。
- 默认 `engineGetDigestLength()` → 0。
- 默认 `engineDigest(buf,off,len)` 异常矩阵：len<digestLen → `DigestException("partial digests not returned")`；buf 过小/off 越界 → `DigestException("insufficient space in the output buffer to store the digest")`；**buf 为 null → NPE（先解引用 buf.length，javadoc 未写明）**；成功路径在 off 处拷贝并返回长度。
- 默认 `engineUpdate(ByteBuffer)`：heap 与 direct 均一次性转调数组变体（1 次 arrCall），空 buffer 为 no-op。
- 默认 `clone()` → `CloneNotSupportedException`。
- 外观闭环：`MessageDigest.getInstance("SHA-256")` 摘要 "abc" 等于已知向量 `ba7816bf...15ad`（真实 SUN SPI 经公开类触达）。

断言入口：`MessageDigestSpiTest.testContractDeep()`（8→33 断言）。
