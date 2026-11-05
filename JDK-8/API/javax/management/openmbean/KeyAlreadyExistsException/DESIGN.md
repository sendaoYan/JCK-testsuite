# javax.management.openmbean.KeyAlreadyExistsException — 测试设计

## 1. API 功能
unchecked 异常（直接 extends IllegalArgumentException）。构造链 ()/(String)；
SUID=1845183636745282866L。

## 2. Java 用户典型用法
TabularData 写路径索引冲突：put(CompositeData)、putAll(CompositeData[])、Map 视图
put(Object[],CompositeData)。

## 3. 参考来源
- Oracle javadoc（TabularData.put/putAll 契约）；OpenJDK 源码 TabularDataSupport（internalPut、
  putAll 批内查重顺序）。

## 4. 测试用例设计 + 易错细节（KeyAlreadyExistsExceptionTest，批 G）
- 层级/unchecked/两构造器数/SUID/round-trip。
- **真实触发点与语义锁定（易错）**：
  - putAll 批内两行同索引（表原本为空）→ 精确消息 "Argument elements values[1] and values[0]
    have the same indexes, calculated according to this TabularData instance's tabularType."
  - put/putAll 撞表中已有行、以及 Map 视图 put(Object[],v) → 精确消息 "Argument value's index,
    calculated according to this TabularData instance's tabularType, already refers to a value in
    this table."
  - **put 不是替换语义**：重复索引一律抛异常（连 Map 视图也不替换）；remove 后才可重放。
  - putAll 失败零副作用：非冲突行也不会插入（先全量校验再变更）。
- 版本差异：两侧零差异。
