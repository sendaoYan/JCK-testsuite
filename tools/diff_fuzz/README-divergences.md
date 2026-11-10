# 差分模糊分歧分类说明（JDK-8 vs JDK-27）

本文件是对 `run_diff.py` 产出的 `out/diff.json` / `out/diff.txt` 中每一条分歧的
三分类说明。分类同时以机器可读形式写入 `diff.json` 的 `bucket` 字段与
`bucket_counts` 汇总，本文件给出分类依据与代表性样例。

- JDK-8 : 1.8.0_372（Temurin，默认 locale provider = COMPAT/JRE）
- JDK-27: 27.0.1.0.2（Temurin，默认 locale provider = CLDR）
- 随机种子: `0x51D1FF2026L`，`Locale`/`TimeZone`/`Charset` 全部固定，无墙钟。

## 三分类定义

| bucket   | 含义 | 处理建议 |
|----------|------|----------|
| `expected` | 规范允许的演进：JDK-8 不支持某构造（抛 `PatternSyntaxException`）、异常类型/诊断信息不同、校验被有意收紧 | 无需修，属预期演进 |
| `candidate` | 疑似回归或未 triage 的行为分支：同一构造在两端都成功但结果不同 | 需要人工复核，可能影响兼容性 |
| `env` | 非代码行为差异：locale/CLDR-vs-COMPAT 数据、货币格式、时区数据等 | 环境数据升级，非回归 |

## 统计总览

- 总用例数: `34409`
- 分歧总数: `452`

| bucket | 数量 |
|--------|------|
| `env` | 405 |
| `expected` | 42 |
| `candidate` | 5 |

按类别：

| 类别 | 分歧 / 总数 | bucket |
|------|------------|--------|
| SDF.format-text | 249 / 1188 | env |
| SDF.parse-loc | 70 / 1440 | env |
| NumberFormat.format-rand | 52 / 400 | 51 env + 1 candidate |
| DecimalFormat.format | 13 / 182 | env |
| DecimalFormat.parse | 13 / 260 | env |
| NumberFormat.format | 9 / 72 | env |
| Regex.match-seed | 28 / 2184 | 24 expected + 4 candidate |
| Pattern.compile(-ci/-uc) | 2+2+2 / 182 | expected |
| Pattern.compile-rand | 5 / 600 | expected |
| Regex.match-rand | 5 / 600 | expected |
| URI | 1 / 51 | expected |
| URL | 1 / 34 | expected |

注：`SDF.format`（2160 例）与 `SDF.parse`（4928 例）**零分歧** —— 纯数字字段的
`SimpleDateFormat` 格式化/解析在两端行为一致，可作为“无代码级回归”的基线证据。

---

## env（405）—— CLDR vs COMPAT 本地化数据

JDK-9 起默认 locale provider 由 COMPAT/JRE 改为 CLDR，导致文本符号、货币格式、
NaN 符号等本地化输出变化。这些不是代码回归，是数据源升级。

### 1. SimpleDateFormat 文本符号（249 + 70）

- 意大利语全称月份首字母大小写变化（CLDR 改为小写）：
  ```
  pat=MMMM|loc=ITALY|millis=0|tz=NY
    jdk8 : ok=Dicembre
    jdk27: ok=dicembre
  ```
- 德语缩写月份 “Jan.”（CLDR 带句点）vs “Jan”（COMPAT 不带），解析方向恰好互斥：
  ```
  pat=MMM d yyyy|loc=GERMANY|in=Jan 15 2024|lenient
    jdk8 : ok=1705276800000（接受 "Jan"）
    jdk27: EXC=ParseException（只认 "Jan."）
  pat=MMM d yyyy|loc=GERMANY|in=Jan. 15 2024|lenient
    jdk8 : EXC=ParseException（不认 "Jan."）
    jdk27: ok=1705276800000
  ```
- 其它差异：日语 `MMM`（COMPAT `12` vs CLDR `12月`）、俄语 `MMMM/MMMMM` 大小写、
  各语言 `zzzz` 全称时区名（如 en-US `Central European Time` vs
  `Central European Standard Time`）、`a`（UK 的 `PM` vs `pm`）、`G` 纪元名等。

### 2. NaN 符号（DecimalFormat 26 + NumberFormat 4）

COMPAT 的 NaN 符号是 `�`（�），CLDR 为字面 `NaN`，并因此改变了解析行为：

```
pat=0|v=NaN      -> jdk8: ok=�        jdk27: ok=NaN
pat=0|in=NaN     -> jdk8: ParseException  jdk27: ok=NaN
```

### 3. 货币负数格式（NumberFormat 5 + 51）

COMPAT en-US 货币负数用括号 `($1.00)`，CLDR 用前置负号 `-$1.00`：

```
currency|v=-1.0 -> jdk8: ok=($1.00)  jdk27: ok=-$1.00
```

---

## expected（42）—— 规范允许的演进

### 1. 正则构造：JDK-8 不支持（40 例）

JDK-9 起新增、JDK-8 一律抛 `PatternSyntaxException` 的构造，两端“抛 vs 值”：
- `\X`（扩展字素簇）：JDK-8 `EXC=PatternSyntaxException`，JDK-27 编译/匹配成功。
- `\N{...}`（按 Unicode 名称匹配字符）：同上。
- `\p{Script=Greek}` / `\p{sc=Latin}`（JDK-8 仅接受 `Is`/`In`/小写 `script=`/`sc=` 等旧写法）。

```
\\X                        -> jdk8: EXC=PatternSyntaxException  jdk27: ok
\\N{LATIN SMALL LETTER A}  -> jdk8: EXC=PatternSyntaxException  jdk27: ok（匹配 a）
```

这些符合“JDK-8 不支持某构造”，是 JDK-9 特性回填的预期分歧。

### 2. URI 诊断索引精度（1 例）

同为 `URISyntaxException`、同一 reason，仅 `getIndex()` 从 7 变 11（指向更精确的
非法字符位置）：
```
http://host with space/
  jdk8 : idx=7（authority 起点）
  jdk27: idx=11（实际空格位置）
```

### 3. URL 主机名校验收紧（1 例）

JDK-27 对 URL host 做更严格校验，拒绝含空格的 host（JDK-8 放行）：
```
http://exa mple.com
  jdk8 : ok（host="exa mple.com"）
  jdk27: EXC=MalformedURLException
```
这是有意的安全加固，属预期演进。

---

## candidate（5）—— 需复核的行为分支

### 1. `\b` / `\B` 词边界对非 ASCII 的语义变化（4 例）

同一构造两端都成功，但边界位置判断不同。JDK-8 的 `\b`/`\B` 按 Unicode 字母/数字
判定词边界（把 `é`、`İ`、`ı`、`中` 当单词字符），而 JDK-27 与默认 ASCII `\w`
一致（这些字符视为非单词字符）：

```
pat=\B|subj=İı
  jdk8 : find=true|start=1|end=1
  jdk27: find=true|start=0|end=0
pat=\B|subj=Aé
  jdk8 : find=true|start=1|end=1
  jdk27: find=true|start=2|end=2
pat=\b\w++\b|subj=Aé
  jdk8 : find=false
  jdk27: find=true|start=0|end=1（匹配 "A"）
```

这很可能对应 JDK-9+ 对 `\b`/`\B` 与 `\w` 语义不一致的修正（JDK-8 中 `\w` 为
ASCII 而 `\b` 却按 Unicode 判定）。是否影响用户需复核：对依赖旧边界行为的代码是
潜在回归，故归入 `candidate`。

### 2. `NumberFormat.format(double)` 大数舍入变化（1 例）

普通数字格式对大 `double` 的最短十进制表示不同（其余 51 例 currency 已归 env）：

```
number|bits=c3cbb4d19abd25ff
  jdk8 : ok=-3,992,901,994,721,050,100
  jdk27: ok=-3,992,901,994,721,050,000
```

疑似 JDK-19+ 引入 Ryu/DoubleToDecimal 后 double→十进制转换策略变化。属行为变化，
需确认新表示是否符合预期（通常更短/更精确，但会改变输出）。

---

## 结论

1. 405/452（约 90%）的分歧来自 CLDR-vs-COMPAT 本地化数据升级，属环境差异，非回归。
2. 42 例为 JDK-8 不支持的正则构造与有意收紧的 URL 校验，属预期演进。
3. 仅 5 例为需要人工复核的行为分支：非 ASCII 词边界语义（4）+ 大数 double 舍入（1）。
4. `SimpleDateFormat` 纯数字字段格式化/解析在 JDK-8 与 JDK-27 之间零分歧。
