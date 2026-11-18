# Babel（巴别塔）

> 面向 Forge 1.20.1 的《明日方舟》风格战斗系统基础库 —— 把方舟的数值体系（元素损伤、目标生命、护盾、防御与法抗、N 难度）以及「集成战略」收藏品框架在 Minecraft 中实现，并附带一套可独立游玩的内容。

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-62B47A.svg)](#环境要求)
[![Forge](https://img.shields.io/badge/Forge-47.1.47%2B-orange.svg)](#环境要求)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](#环境要求)
[![Version](https://img.shields.io/badge/version-1.2.6-lightgrey.svg)](gradle.properties)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

---

## 简介

Babel 脱胎于「水月与深蓝之树」主题模组（Caerula Arbor），现定位为**公共库**：将明日方舟的战斗机制与肉鸽藏品在 Minecraft 中实现，供同系列内容模组复用。

它同时也是一个能直接游玩的模组：拥有百余件收藏品、十余种状态效果、坎诺特商人与其劫掠战、源石锭货币、`/ep` 与 `/lifepoint` 调试指令。

- **给玩家**：安装即获得方舟式战斗体验与收藏品玩法。
- **给开发者**：`com.susen36.babel.api` 提供可挂接的事件与注册入口，属性、Capability、网络同步、HUD、Jade 显示均已就绪。

## 特性

### 元素损伤（EP）

- 五类损伤：**神经**（`nervous`）、**侵蚀**（`corrosion`）、**灼燃**（`burn`）、**凋亡**（`necrosis`）、**狂躁**（`frenzy`），另有 `empty` 占位类型。
- 每种损伤独立累积、独立上限（`elemental_max_value` 属性），受 `elemental_resistance`、`elemental_modifier` 与实体自身的损伤倍率影响。
- 攒满即「爆条」：触发对应负面效果（如麻痹、破甲、虚弱等）并结算元素伤害（专属伤害类型），可致死。
- HUD 支持两种样式：图标条（类似饥饿值）或圆环，由配置项切换，位置可微调。
- 对其他模组的开放接口：`EPManager#hurtElemental` / `healElemental` / `getValue` / `getRemainProgress`，以及可取消的 `ElementEvent.Hurt`。

### 目标生命与护盾

- **目标生命**（life point）：独立于原版血量的第二生命条，玩家受到致命伤害时先行扣除以阻挡死亡，全局上限可配（默认 32767）。
- **护盾**（shield / `barrier` 属性）与目标生命并列显示，上限默认 99999。
- 全套 HUD 覆盖层：目标生命、护盾、元素损伤、冻结、麻痹、沉睡、残血等。
- 服务端权威计算 + 网络同步（`LivesSyncMessage` / `EPSyncMessage`），客户端只负责渲染。

### 战斗数值

注册在 `babel` 命名空间下的 9 个属性：

| 属性 | 作用 |
|---|---|
| `defense` | 物理防御，参与减伤公式 |
| `magic_resistance` | 法术抗性 |
| `elemental_resistance` | 元素损伤抗性 |
| `elemental_max_value` | 元素损伤上限 |
| `elemental_modifier` | 受到的元素损伤倍率 |
| `physical_missrate` / `magic_missrate` | 物理 / 法术闪避率 |
| `barrier` | 屏障 |
| `max_life_point` | 目标生命上限 |

减伤过程对外暴露 `ResistDamageEvent.Defense` / `.MagicResistance`，闪避触发时抛出 `DodgeEvent`，均可被其他模组挂接修改。

### N 难度

- 游戏规则 `difficultyLevel`：`0` 关闭倍率，`1–18` 参与计算，划分五档：

| 档位 | 数值区间 |
|---|---|
| `EASY` 简单 | 0–2 |
| `NORMAL` 普通 | 3–5 |
| `HARD` 困难 | 6–8 |
| `EXPERT` 专家 | 9–11 |
| `PERILOUS` 险境 | 12–18 |

- 收藏品数值、刷怪与掉落倍率均随难度变化；从低难度跳到 9 以上时会有对应提示。
- 开发者可用 `NDifficulty.difficultyLevel(level)`、`NDifficulty.multiplier(level, ...)` 直接取值。

### 收藏品（集成战略）

- 142 件收藏品，分四级：`NORMAL`、`RARE`、`ADVANCED`（可被配置禁用）、`CURSED`（诅咒，自动激活且名字染红）。
- 每件收藏品可有**层数**（`Levels(min, max, default)`）、**冷却**、**持续时间**（默认 576000 tick ≈ 8 小时 / 24 游戏日）。
- 获取途径：战利品表注入（默认 30% 概率）、怪物 / Boss 掉落（Boss 必掉高级藏品）、`/collectible give`。
- 自带收藏品界面（默认 `C` 键），可查看已持有藏品、层数、冷却；坎诺特的「失与得」界面可用已有藏品交换随机藏品。
- 某些藏品带有特殊功能，详情见`docs/collectibles-effect/`
- 注册 API 见下文[收藏品注册](#收藏品注册)。

### 状态效果

冻结、麻痹、眩晕、沉默、睡眠、束缚、脆弱、破甲（侵蚀）、失重、隐匿、物理闪避、法术闪避、元素抗性等，配套药水与酿造配方。

### 敌人与事件

- **坎诺特**（`babel:cannot`）：商人实体，按藏品稀有度以源石锭标价出售收藏品；攻击他会触发「劫掠」波次战，Boss 血条显示进度。波次配置在 `data/babel/robberies/*.json`，可自行扩展。
- 劫掠结束后有冷却期（默认 108000 tick），期间坎诺特不再理睬该玩家。
- 源石锭（`originium_ingot`）：基础货币物品，可在创造模式物品页签取用；实体死亡时由掉落修饰器按倍率产出，倍率可被 `OriginiumIngotDropEvent` 修改。

### 兼容与联动

- **Jade**（软依赖）：三个 provider，分别显示元素损伤、实体抗性、实体状态；未安装 Jade 不影响运行。
- **JEI**：仅编译期 API 依赖。
- **MixinExtras**：通过 jarJar 内嵌，无需玩家额外安装。

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.1.47 及以上（`forge_version_range = [47,)`，Minecraft 侧限定 `[1.20.1,1.21)`） |
| JDK | 17（Gradle toolchain 固定） |
| Gradle | 8.1.1（随 wrapper 提供，无需本机安装） |

## 构建

```bash
# Windows
gradlew.bat build

# macOS / Linux / Git Bash
./gradlew build
```

产物：`build/libs/babellib-1.20.1-forge-1.2.6.jar`。

常用任务：

| 任务 | 用途 |
|---|---|
| `gradlew build` | 编译 + 打包（含 `reobfJar` 重混淆） |
| `gradlew runClient` | 启动开发客户端 |
| `gradlew runServer` | 启动开发服务端 |
| `gradlew runData` | 运行数据生成器，输出到 `src/generated/resources` |
| `gradlew publishToMavenLocal` | 发布到本地 Maven 仓库，供其他模组依赖 |

> 首次构建需要联网下载 Forge、Mappings 与依赖，耗时较长。若 IDE 缺库，先 `gradlew --refresh-dependencies`。

**编译期检查**：项目启用 ErrorProne + NullAway，NullAway 违规按 **ERROR** 处理，会直接中断构建。`build.gradle` 中已关闭一批框架必然触发的误报（`ReferenceEquality`、`UnusedMethod`、`FutureReturnValueIgnored` 等），新增代码请勿依赖这些开关兜底。

## 安装

1. 安装 Minecraft 1.20.1 与 Forge 47.1.47 及以上。
2. 把 `babel-*.jar` 放进 `.minecraft/mods/`。
3. 单人游戏直接进入；服务器需**客户端与服务端都安装**（模组注册了方块、物品与实体，属 `side="BOTH"`）。Jade 可选。

## 命令

| 命令 | 所需权限 | 说明 |
|---|------|---|
| `/collectible give <players> <collectible> [layer]` | 2    | 发放收藏品，可指定层数 |
| `/collectible clear <players> <collectible>` | 2    | 移除收藏品 |
| `/ep <entities> <type> hurt <amount>` | 2    | 施加元素损伤，`type` 为 `nervous` / `corrosion` / `burn` / `necrosis` / `frenzy` |
| `/ep <entities> <type> heal <amount>` | 2    | 恢复元素损伤 |
| `/ep <entities> <type> check` | 0    | 查询当前元素损伤数值 |
| `/lifepoint <players> life_point <0–255>` | 2    | 设置目标生命 |
| `/lifepoint <players> max_life_point <1–255>` | 2    | 设置目标生命上限 |
| `/lifepoint <players> shield <0–999>` | 2    | 设置护盾 |

`<collectible>` 使用物品 ID，例如 `babel:omnikey`、`babel:hand_of_strangle`。

## 游戏规则

| 规则 | 默认 | 说明                         |
|---|---|----------------------------|
| `difficultyLevel` | `0` | N 难度等级，`0` 不启用等级，`1–18` 生效 |
| `targetLifeFunction` | `true` | 是否启用目标生命系统                 |

```mcfunction
/gamerule difficultyLevel 6
/gamerule targetLifeFunction false
```

## 配置

配置文件：`config/babel-common.toml`（COMMON 类型，改动后 `/reload` 或重启生效）。

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `ep_icon_style` | `false` | `true` 用图标条显示元素损伤，`false` 用圆环 |
| `ep_x_offset` / `ep_y_offset` | `0` | 元素损伤 HUD 偏移（右 / 下为正） |
| `life_point_x_offset` / `life_point_y_offset` | `0` | 目标生命 HUD 偏移 |
| `shield_x_offset` / `shield_y_offset` | `0` | 护盾 HUD 偏移（相对目标生命 UI） |
| `initial_life_point` | `6.0` | 初始目标生命 |
| `life_point_global_limit` | `32767` | 目标生命全局上限 |
| `player_shield_global_limit` | `99999` | 护盾全局上限 |
| `collectible_loot_chance` | `0.3` | 战利品表额外注入收藏品的概率 |
| `collectible_duration_ticks` | `576000` | 收藏品生效时长（tick） |
| `ban_advanced_collectibles` | `false` | 禁用 `ADVANCED` 级收藏品（无法激活） |
| `hand_strangle_recognizable` | `CrossbowMod2:item.woodenCrossbow` | 额外可触发「扼喉之手」的物品 |
| `hand_firework_recognizable` | `twilightforest:triple_bow` | 额外可触发「烟花之手」的物品 |
| `hand_engrave_recognizable` | `iceandfire:tide_trident` | 额外可触发「刻勋之手」的物品 |
| `royal_alliance_treaty_recognizable` | `cataclysm:ignis` 等 | 额外可触发「王庭盟约」的生物 |
| `wither_elemental_damage` | `true` | 凋灵攻击改为施加凋亡损伤 |
| `cold_to_frozen` | `true` | 两段式寒冷：先标记寒冷，再次冻结伤害转为冰冻效果 |
| `robbery_restriction_duration_ticks` | `108000` | 劫掠后坎诺特无视该玩家的时长 |

列表类配置支持 `*` 通配整个命名空间，例如 `tacz:*`、`babel:oceanized_*`。

## 开发者 API

完整的依赖引入、事件时机与扩展流程见 [`docs/developer-guide.md`](docs/developer-guide.md)。

对外接口集中在 `api`（事件）、`collectible`（收藏品框架）、`elemental`（EPManager 等）、`capability` 与 `difficulty`；`client`、`mixin`、`datagen`、`init` 属内部实现，升级时可能变动。

### 事件一览

| 事件 | 触发时机 | 可否取消 / 修改 |
|---|---|---|
| `ElementEvent.Hurt` | 元素损伤结算前 | 可取消；可改 `amount` |
| `ElementEvent.PostHurt` | 元素损伤结算后 | 只读 |
| `ElementEvent.Heal` | 元素损伤恢复前 | 可取消；可改 `amount` |
| `ElementEvent.Burst` | 元素损伤「爆条」时 | 只读 |
| `ResistDamageEvent.Defense` / `.MagicResistance` | 防御 / 法抗参与减伤时 | 可改有效属性值 |
| `DodgeEvent` | 触发物理或法术闪避时 | 只读 |
| `LifePointConsumeEvent` | 玩家受致命伤、扣除护盾与目标生命时 | 可供依赖模组挂钩自身机制 |
| `LivingAddEffectEvent` | 实体获得药水效果时 | 可整体替换效果实例（如延长时长） |
| `CollectibleEvent.Used` / `.Removed` / `.LayerChanged` | 收藏品使用 / 移除 / 层级变化 | 只读 |
| `CollectibleDropEvent` | 实体死亡结算收藏品掉落 | 可改掉落概率（设 0 阻止） |
| `CollectibleRegisterEvent` | 收藏品注册表就绪后 | 注入收藏品 |
| `OriginiumIngotDropEvent` | 结算源石锭掉落 | 可改倍率（设 0 阻止） |
| `LostAndFoundEvent` | 拾遗界面兑换时 | 可取消 |

### 收藏品注册

```java
public final class MyCollectibles {

    // 建议在 mod 构造阶段初始化（内部会向 mod 事件总线投递 CollectibleRegisterEvent）
    private static final CollectibleBuilder BUILDER =
            CollectibleBuilder.create("mymod", FMLJavaModLoadingContext.get().getModEventBus());

    public static final RegistryObject<Item> SAMPLE_RELIC = BUILDER.registerCollectible(
            "sample_relic",                       // 注册名 → mymod:sample_relic
            25,                                   // 使用耗时（tick）
            false,                                // 是否可无限次重复使用
            CollectibleTiers.RARE,                // 稀有度
            new CollectibleItem.Levels(0, 3, 1),  // 层数 min / max / 默认
            CollectibleActivation.standard(),     // 激活音效、粒子与是否自动使用
            (stack, level, player) -> {           // 激活效果
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
            });
}
```

只想从外部注入、不想替换注册表时，监听事件即可：

```java
@SubscribeEvent
public static void onCollectibleRegister(CollectibleRegisterEvent event) {
    event.builder().registerCollectible("other_relic",
            (stack, level, player) -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1)));
}
```

运行时读写（`Collectibles` 是玩家级 Capability，服务端权威）：

```java
Item relic = myRelic.get();

int layer = Collectibles.getLayerData(player).getLayer(relic);
Collectibles.addLayerAndSync(player, relic, 1);         // 改层数并同步客户端
boolean used = Collectibles.isUsed(player, relic);
long endTick = Collectibles.getCooldownData(player).getCooldownEndTick(relic);
Collectibles.activateAndSync(player, relic);           // 指令等场景直接激活
```

### 元素损伤与生命

```java
// 施加 200 点燃烧损伤（attacker 可为 null）
EPManager.hurtElemental(target, AbstractEPCapability.EPType.BURN, attacker, 200);

int value = EPManager.getValue(target, AbstractEPCapability.EPType.NERVOUS);
float progress = EPManager.getRemainProgress(target, AbstractEPCapability.EPType.NERVOUS);

// 目标生命 / 护盾
HealthCapability health = BabelCapability.getHealth(entity);
if (health != null) {
    health.setShield(health.getShield() + 50);
    health.setLives(health.getLives() - 1);
}
```

### 作为依赖引入

发布到本地仓库后依赖：

```groovy
dependencies {
    implementation fg.deobf("com.susen36.babel:babel-lib:1.2.6")
}
```

或直接使用 JitPack（分支构建，坐标为 `com.github.SuSen36:babel-lib:1.20.1-SNAPSHOT`，首次使用请以 JitPack 构建页显示的实际坐标为准）：

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation fg.deobf('com.github.SuSen36:babel-lib:1.20.1-SNAPSHOT')
}
```

## 项目结构

```text
babel-lib/
├─ src/main/java/com/susen36/babel/
│  ├─ api/            对外事件（订阅方唯一应直接依赖的包）
│  ├─ block/          水壶、王座基座、金杯、血族床等方块
│  ├─ capability/     EP / Health 两类 Capability 与注册
│  ├─ client/         HUD、界面、渲染、粒子、模型
│  ├─ collectible/    收藏品框架：注册、层级、冷却、界面数据
│  ├─ command/        命令实现
│  ├─ compat/jade/    Jade 插件
│  ├─ datagen/        数据生成器
│  ├─ difficulty/     N 难度、档位与倍率工具
│  ├─ effect/         状态效果
│  ├─ elemental/      元素损伤实现与 EPManager
│  ├─ entity/         坎诺特与劫掠机制
│  ├─ event/          内部事件监听与玩法逻辑
│  ├─ init/           DeferredRegister 集中注册
│  ├─ item/           收藏品实现
│  ├─ loot/           为藏品与源石锭添加战利品掉落
│  ├─ menu/           菜单界面
│  ├─ mixin/          17 个 Mixin（含 2 个客户端）
│  ├─ network/        同步与交互数据包
│  └─ util/           工具函数单元（使用冷却，判断，元素伤害，目标生命值）
├─ src/main/resources/
│  ├─ assets/babel/   模型、贴图、语言（`zh_cn` / `en_us` 为基准）、粒子、音效
│  └─ data/babel/     伤害类型、战利品表、劫掠配置、标签
├─ docs/              收藏品资料与抓取脚本（见下）
└─ tools/             开发辅助脚本
```

## 文档与工具

- [`docs/collectibles-wiki/`](docs/collectibles-wiki/README.md)：集成战略历代主题收藏品的中英文对照资料，由 `collect_wiki.ps1` 从 PRTS Wiki 与 arknights.wiki.gg 再生，是新增收藏品时的数值与文案来源。
- `tools/i18n_zh_trad.py`：由 `zh_cn.json` 生成繁中（`zh_tw` / `zh_hk` / `zh_mo`）；`zh_cn` 与 `en_us` 为**人工维护基准**，其余语种均为自动产物，不要手改。
- `tools/i18n_argos_translate.py`：由英文离线生成 `ru_ru` / `ja_jp` / `it_it` / `fr_fr` / `de_de` / `es_es`。
- `tools/ResourceLocationUpdater.java`：批量把 `new ResourceLocation(ns, path)` 迁移为 `ResourceLocation.fromNamespaceAndPath`。
- `tools/strip_png_metadata.py`、`tools/strip_trailing_newline.py`：批量清理贴图元数据与文件尾空行，提交前建议各跑一次。

## 常见问题

**收藏品用了没反应？**
确认自己不在旁观者模式（旁观时收藏品效果总闸关闭），并检查 `ban_advanced_collectibles` 是否禁用了高级藏品。

**元素损伤条没出现？**
只有积累了对应类型损伤的实体会显示；玩家侧可通过 `/ep @s nervous hurt 100` 验证，再用 `ep_icon_style` 切换显示样式。

**构建报大量 NullAway 错误？**
NullAway 在本项目是 ERROR 级。常见原因是新增字段未标注 `@Nullable` 或返回值可能为空；补注解或调整逻辑，不要直接关检查。

**服务器里玩家看不到 HUD / 界面？**
客户端必须安装本模组（HUD、界面、粒子均在客户端实现）；服务端只装服务端版本会出现同步正常但画面缺失的情况。

## 许可证

`gradle.properties` 中声明为 **GPL-3.0-or-later**（保留所有权利）。

本仓库源码公开可读，且可以用于复制、修改、再分发或商用许可，但修改后的代码仍需要遵循GPL协议开源。仓库内的 `TEMPLATE_LICENSE.txt` 为 Forge MDK 模板遗留文件，不构成本项目的授权声明。

## 致谢

- 玩法与数值原型：鹰角网络《明日方舟》及其「集成战略」模式。
- 收藏品资料：[PRTS Wiki](https://prts.wiki/)、[arknights.wiki.gg](https://arknights.wiki.gg/)。
- 构建脚手架：Forge MDK、MixinExtras（LlamaLad7）、Jade（Snownee）。
