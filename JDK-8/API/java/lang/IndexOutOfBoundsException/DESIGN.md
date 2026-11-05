# IndexOutOfBoundsException — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
继承 `RuntimeException`。**构造器版本差异（javap 双侧）**：JDK-8 仅 `()`、`(String)`；JDK-27 另有 `(int)`、`(long)`（JDK 9+ 加入），消息 "Index out of range: N"。无 `(String,Throwable)`（双侧均无，反射断言）。
`serialVersionUID = 234122996006267687L`（双侧一致）。

## 2. 使用方式
集合/字符序列越界的公共父类型（AIOOBE、StringIndexOutOfBoundsException 都是其子类）；27 侧可用 (int)/(long) 快速构造带索引消息。

## 3. 参考来源
Oracle javadoc（JDK 9 起 (int) 构造器）；双侧实测触发源消息（ArrayList.add/subList 双侧一致；ArrayList.set、charAt、codePointAt、subSequence 双侧不同）。

## 4. 测试用例设计 + 易错规范细节
- 8 侧：反射断言 (int)/(long) 构造器 ABSENT（NoSuchMethodException 精确）；27 侧直测 (int)/(long) 消息 "Index out of range: 5"/"-3"/"2147483647"/long MIN。
- 触发源矩阵：`Arrays.asList("a").get(5)` → **AIOOBE**（8 消息 "5"、27 "Index 5 out of bounds for length 1"）；`"abc".charAt(7)` → SIOOBE（8 "String index out of range: 7"、27 "Index 7 out of bounds for length 3"）；StringBuilder.charAt → SIOOBE。
- 集合族：`ArrayList.add(5,x)` → "Index: 5, Size: 0"（**双侧一致直锁**）、`subList(0,5)` → "toIndex = 5"（双侧一致）、`set(3,x)` 双侧不同（8 "Index: 3, Size: 1"、27 "Index 3 out of bounds for length 1"）、`codePointAt(9)`/`subSequence(1,9)` 双侧不同。
- 层级：AIOOBE/SIOOBE 父类都精确 == IOOBE；in-bounds 正例。
- 通用契约 + SUID + 序列化。
- 断言数 7 → 81（27 侧 21 → 100）。
