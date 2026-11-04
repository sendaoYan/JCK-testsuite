# java.util.ResourceBundle.Control 测试设计案（JDK-27）

> 覆盖目标：完整契约：候选 locale 序列、格式列表、fallback、toBundleName/toResourceName、getControl 缓存单例、非法 formats IAE、NPE 矩阵。

## 1. API 功能

- 工厂：getControl(List<String>)（仅 java.class/java.properties 组合，缓存单例）；getNoFallbackControl
- 候选与回退：getCandidateLocales/getFallbackLocale
- 定位：toBundleName/toResourceName；时间：getTimeToLive/needsReload；newBundle 实例化；getFormats

## 2. 使用方式

ResourceBundle.Control.getControl(List.of("java.class")) → getCandidateLocales("base", Locale.FRANCE) == [fr_FR, fr, ROOT]；非法 formats → IllegalArgumentException。

## 3. 参考来源

- Oracle javadoc `java/util/ResourceBundle.Control`（候选序列规范）
- JDK 源码 ResourceBundle.Control 单例与校验

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 候选 locale 序列 [fr_FR, fr, ROOT] 精确 | testControl |
| P0 | getFormats 顺序 + toBundleName/toResourceName 规则 | testControlContracts |
| P0 | getControl 非法/空 formats → IAE；null → NPE | testControlContracts |
| P1 | getControl 缓存同实例；getFallbackLocale null/default 语义 | testControlContracts |
| P0 | 契约深化：候选列表全序/格式表/回退语义/非法参数矩阵 | testControlContracts |
