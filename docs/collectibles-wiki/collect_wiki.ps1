param([string]$dir = $PSScriptRoot)
$ErrorActionPreference = 'Stop'

# PS 5.1 按 ANSI 解析无 BOM 脚本，本文件含中文字面量，必须保留 UTF-8 BOM
$tmp = $env:TEMP
$prtsApi = 'https://prts.wiki/api.php'
$ggApi = 'https://arknights.wiki.gg/api.php?action=query&prop=revisions&rvprop=content&rvslots=main&redirects=1&format=json&formatversion=2&titles='
$ggUa = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36'
$tmpGg = Join-Path $tmp 'gg_en_flavor'
if (-not (Test-Path $tmpGg)) { New-Item -ItemType Directory -Path $tmpGg | Out-Null }

# 七代主题的单一数据源：阶段 1 的再生清单与阶段 2 的比对清单共用；新增主题只需在此表追加条目
$themes = @(
    @{ Code = '00'; File = '00-刻俄柏的灰蕈迷境.md'; Name = '刻俄柏的灰蕈迷境'; Page = '刻俄柏的灰蕈迷境/收藏品图鉴'; Idx = 0; EnNote = '' },
    @{ Code = '01'; File = '01-傀影与猩红孤钻.md'; Name = '傀影与猩红孤钻'; Page = '傀影与猩红孤钻/长生者宝盒'; Idx = 1; EnNote = '' },
    @{ Code = '02'; File = '02-水月与深蓝之树.md'; Name = '水月与深蓝之树'; Page = '水月与深蓝之树/生物制品陈设'; Idx = 2; EnNote = '' },
    @{ Code = '03'; File = '03-探索者的银凇止境.md'; Name = '探索者的银凇止境'; Page = '探索者的银凇止境/仪式用品索引'; Idx = 3; EnNote = '' },
    @{ Code = '04'; File = '04-萨卡兹的无终奇语.md'; Name = '萨卡兹的无终奇语'; Page = '萨卡兹的无终奇语/想象实体图鉴'; Idx = 4; EnNote = '' },
    @{ Code = '05'; File = '05-岁的界园志异.md'; Name = '岁的界园志异'; Page = '岁的界园志异/珍玩集册'; Idx = 5; EnNote = '' },
    @{ Code = '06'; File = '06-沉沦者的黑流树海.md'; Name = '沉沦者的黑流树海'; Page = '沉沦者的黑流树海/拟造物质编目'; Idx = 6; EnNote = '（IS6 部分英文名待 wiki.gg 收录后补全）' }
)

function Get-Prtswikitext([string]$page) {
    $u = "$prtsApi" + '?action=parse&prop=wikitext&format=json&formatversion=2&page=' + [uri]::EscapeDataString($page)
    $file = Join-Path $tmp ('wikitext_' + ([Math]::Abs($page.GetHashCode())) + '.json')
    curl.exe -s -o $file $u
    return (([System.IO.File]::ReadAllText($file) | ConvertFrom-Json).parse.wikitext)
}

# ============================================================
# 阶段 1：解析 PRTS 图鉴页 wikitext 生成全部主题文档，
#         并同步抓取 wiki.gg 的英文名与英文图鉴描述写入条目
# ============================================================

# ---------- 通用工具 ----------
function ConvertFrom-WikiTemplate {
    # 解析最外层 {{...}} 模板为参数有序字典（处理嵌套 {{}} 与 [[]]）
    param([string]$Text, [string]$StartMarker)
    $results = New-Object System.Collections.ArrayList
    $i = 0
    while (($idx = $Text.IndexOf($StartMarker, $i)) -ge 0) {
        $depth = 0
        $j = $idx
        $end = -1
        while ($j -lt $Text.Length - 1) {
            $two = $Text.Substring($j, 2)
            if ($two -eq '{{') { $depth++; $j += 2; continue }
            if ($two -eq '}}') { $depth--; $j += 2; if ($depth -eq 0) { $end = $j; break }; continue }
            $j++
        }
        if ($end -lt 0) { break }
        $inner = $Text.Substring($idx + 2, $end - $idx - 4)
        # 顶层按 | 分割
        $parts = New-Object System.Collections.ArrayList
        $d = 0; $cur = New-Object System.Text.StringBuilder
        for ($k = 0; $k -lt $inner.Length - 1; $k++) {
            $two = $inner.Substring($k, 2)
            if ($two -eq '{{') { $d++; [void]$cur.Append($two); $k++; continue }
            if ($two -eq '}}') { $d--; [void]$cur.Append($two); $k++; continue }
            if ($two -eq '[[') { $d++; [void]$cur.Append($two); $k++; continue }
            if ($two -eq ']]') { $d--; [void]$cur.Append($two); $k++; continue }
            if ($two[0] -eq '|' -and $d -eq 0) { [void]$parts.Add($cur.ToString()); $cur = New-Object System.Text.StringBuilder; continue }
            [void]$cur.Append($two[0])
        }
        [void]$cur.Append($inner[$inner.Length - 1])
        [void]$parts.Add($cur.ToString())
        $dict = [ordered]@{}
        foreach ($p in $parts) {
            $eq = $p.IndexOf('=')
            if ($eq -gt 0) { $dict[$p.Substring(0, $eq).Trim()] = $p.Substring($eq + 1).Trim() }
        }
        [void]$results.Add([pscustomobject]@{ Start = $idx; End = $end; Params = $dict })
        $i = $end
    }
    return $results
}

function Clean-WikiText([string]$s) {
    if ([string]::IsNullOrWhiteSpace($s)) { return '' }
    $s = [regex]::Replace($s, '\{\{r\+\|([^|}]+)\|([^}]+)\}\}', '$2')          # {{r+|tip|show}} -> show
    $s = [regex]::Replace($s, '\{\{color\|[^|}]+\|([^}]+)\}\}', '$1')          # {{color|c|t}} -> t
    $s = [regex]::Replace($s, '\{\{mdi\|[^}]*\}\}', '')                        # 图标
    $s = [regex]::Replace($s, '\{\{[^|}]*\}\}', '')                            # 其余无参模板
    $s = [regex]::Replace($s, '\[\[[^\]|]+\|([^\]]+)\]\]', '$1')               # [[a|b]] -> b
    $s = [regex]::Replace($s, '\[\[([^\]]+)\]\]', '$1')                        # [[a]] -> a
    $s = $s.Replace("'''", '').Replace("''", '')
    $s = [regex]::Replace($s, '<[^>]+>', '')                                   # 残余 HTML 标签
    $s = [regex]::Replace($s, '\s*※\s*', '※')                                 # 备注前缀收紧
    return $s.Trim()
}

function Split-EffectBranches([string]$rawEffect) {
    # 按 <br> 拆分支，返回清洗后的分支数组
    $clean = Clean-WikiText $rawEffect
    if (-not $clean) { return @() }
    $branches = $clean -split '<br\s*/?>'
    # Clean 已去 HTML 标签，改用原文分割
    $raw = $rawEffect -split '<br\s*/?>'
    $out = @()
    foreach ($b in $raw) {
        $c = Clean-WikiText $b
        if ($c) { $out += $c }
    }
    if ($out.Count -eq 0 -and $clean) { $out = @($clean) }
    return $out
}

# ---------- 角标映射 ----------
$badgeWt = Get-Prtswikitext '模板:收藏品/角标'
$badges = @{}
[regex]::Matches($badgeWt, '(?s)\|\s*([a-z_0-9]+)\s*=\s*\n?<span.*?data-trigger="mouseenter[^"]*"[^>]*>(.*?)</span>') | ForEach-Object {
    $txt = [regex]::Replace($_.Groups[2].Value, '<[^>]+>', ' ')
    $txt = [regex]::Replace($txt, '\s+', ' ').Trim()
    $badges[$_.Groups[1].Value] = $txt
}
if ($badges.Count -eq 0) { throw 'badge mappings empty: 模板:收藏品/角标 page parse failed' }
Write-Output ("badge mappings loaded: {0}" -f $badges.Count)

# ---------- 英文名对照：优先复用现有文档标题「中文名 英文名」 ----------
$zhToEn = @{}
foreach ($t in $themes) {
    $docPath = Join-Path $dir $t.File
    if (-not (Test-Path $docPath)) { continue }
    foreach ($line in [System.IO.File]::ReadLines($docPath, [System.Text.Encoding]::UTF8)) {
        $m = [regex]::Match($line, '^### No\.([A-Za-z0-9]+)\s+(.+)$')
        if ($m.Success) {
            # 标题为「中文名 英文名」：从右向左剥离无 CJK 的词作为英文名
            $parts = $m.Groups[2].Value.Trim() -split ' '
            $enWords = New-Object System.Collections.Generic.List[string]
            while ($parts.Count -gt 1 -and -not [regex]::IsMatch($parts[$parts.Count - 1], '[\u4e00-\u9fff]')) {
                $enWords.Insert(0, $parts[$parts.Count - 1])
                $parts = $parts[0..($parts.Count - 2)]
            }
            $zh = ($parts -join ' ').Trim()
            $en = ($enWords -join ' ').Trim()
            if ($zh -and $en -and -not $zhToEn.ContainsKey($zh)) { $zhToEn[$zh] = $en }
        }
    }
}
Write-Output ("zh->en pairs from existing docs: {0}" -f $zhToEn.Count)

# ---------- 详情页数据（售价/解锁条件，按主题 N 段提取） ----------
function Get-DetailData([string[]]$names, [string]$theme) {
    $map = @{}
    # 第二轮对未命中的名称追加消歧后缀（如 “复仇者”→“复仇者”(收藏品)）
    $trySets = @(@{ Names = $names; Suffix = $false }, @{ Names = $names; Suffix = $true })
    foreach ($set in $trySets) {
        $pending = @($set.Names | Where-Object { -not $map.ContainsKey($_) })
        if ($pending.Count -eq 0) { continue }
        for ($off = 0; $off -lt $pending.Count; $off += 50) {
            $batch = $pending[$off..([Math]::Min($off + 49, $pending.Count - 1))] | Select-Object -Unique
            $titles = ($batch | ForEach-Object { if ($set.Suffix) { $_ + '(收藏品)' } else { $_ } } | ForEach-Object { [uri]::EscapeDataString($_) }) -join '|'
            $url = "$prtsApi" + '?action=query&prop=revisions&rvprop=content&rvslots=main&redirects=1&format=json&formatversion=2&titles=' + $titles
            $file = "$tmp\detail_batch_$($set.Suffix)_$off.json"
            curl.exe -s -o $file $url
            $obj = [System.IO.File]::ReadAllText($file) | ConvertFrom-Json
            foreach ($p in $obj.query.pages) {
                if (-not $p.revisions) { continue }
                $wt = $p.revisions[0].slots.main.'*'
                if (-not $wt) { $wt = $p.revisions[0].slots.main.content }
                $tpl = ConvertFrom-WikiTemplate -Text $wt -StartMarker '{{收藏品/common'
                if ($tpl.Count -eq 0) { continue }
                $d = $tpl[0].Params
                for ($n = 1; $n -le 12; $n++) {
                    if ($d["主题$n"] -eq $theme) {
                        $key = if ($set.Suffix) { $p.title -replace '\(收藏品\)$', '' } else { $p.title }
                        $key = $key -replace '\(收藏品\)$', ''
                        $map[$key] = [pscustomobject]@{
                            Price  = (Clean-WikiText $d["售价$n"])
                            Unlock = (Clean-WikiText $d["解锁条件$n"])
                            Obtain = (Clean-WikiText $d["获取条件$n"])
                        }
                        break
                    }
                }
            }
        }
    }
    return $map
}

# ---------- wiki.gg 英文图鉴描述抓取 ----------
function Get-TopLevelParams([string]$inner) {
    $parts = New-Object System.Collections.Generic.List[string]
    $cur = New-Object System.Text.StringBuilder
    $depth = 0
    $k = 0
    while ($k -lt $inner.Length) {
        if ($k -lt $inner.Length - 1) {
            $two = $inner.Substring($k, 2)
            if ($two -eq '{{' -or $two -eq '[[') { $depth++; [void]$cur.Append($two); $k += 2; continue }
            if ($two -eq '}}' -or $two -eq ']]') { $depth--; [void]$cur.Append($two); $k += 2; continue }
        }
        if ($inner[$k] -eq '|' -and $depth -eq 0) {
            [void]$parts.Add($cur.ToString())
            [void]$cur.Clear()
            $k++
            continue
        }
        [void]$cur.Append($inner[$k])
        $k++
    }
    [void]$parts.Add($cur.ToString())
    return $parts
}

function Get-EnDescFromWikitext([string]$wt) {
    $marker = '{{Item description'
    $idx = $wt.IndexOf($marker)
    if ($idx -lt 0) { return '' }
    $depth = 0
    $j = $idx
    $end = -1
    while ($j -lt $wt.Length - 1) {
        $two = $wt.Substring($j, 2)
        if ($two -eq '{{') { $depth++; $j += 2; continue }
        if ($two -eq '}}') { $depth--; $j += 2; if ($depth -eq 0) { $end = $j; break }; continue }
        $j++
    }
    if ($end -lt 0) { return '' }
    $inner = $wt.Substring($idx + 2, $end - $idx - 4)
    $parts = Get-TopLevelParams $inner
    $enDesc = ''
    $positional = 0
    for ($i = 1; $i -lt $parts.Count; $i++) {
        $p = $parts[$i]
        $eq = $p.IndexOf('=')
        if ($eq -gt 0 -and $p.Substring(0, $eq).Trim() -match '^(desc|flavor|text|description)$') {
            if (-not $enDesc) { $enDesc = $p.Substring($eq + 1) }
        } elseif ($eq -le 0 -or $p.Substring(0, $eq).Trim() -notmatch '^\w+$') {
            $positional++
            if ($positional -eq 2 -and -not $enDesc) { $enDesc = $p }
        }
    }
    return $enDesc
}

function Clear-WikiMarkup([string]$s) {
    if ([string]::IsNullOrWhiteSpace($s)) { return '' }
    $s = [regex]::Replace($s, '(?s)<!--.*?-->', '')
    $s = [regex]::Replace($s, '(?s)<ref[^>]*>.*?</ref>', '')
    $s = [regex]::Replace($s, '<ref[^>]*/>', '')
    $s = [regex]::Replace($s, '\{\{color\|[^|}]+\|([^}]*)\}\}', '$1')
    $s = [regex]::Replace($s, '\{\{O\|([^|}]*)\|([^}]*)\}\}', '$2')
    $s = [regex]::Replace($s, '\{\{O\|([^}]*)\}\}', '$1')
    $s = [regex]::Replace($s, '\{\{([^{}|]*)\|([^{}]*)\}\}', '$2')
    $s = [regex]::Replace($s, '\{\{[^{}]*\}\}', '')
    $s = [regex]::Replace($s, '\[\[[^\]|]+\|([^\]]+)\]\]', '$1')
    $s = [regex]::Replace($s, '\[\[([^\]]+)\]\]', '$1')
    $s = [regex]::Replace($s, '\[https?://[^\s\]]+\s+([^\]]+)\]', '$1')
    $s = [regex]::Replace($s, '\[https?://[^\]]+\]', '')
    $s = $s.Replace("'''", '').Replace("''", '')
    $s = [regex]::Replace($s, '<[^>]+>', '')
    $s = $s.Replace('&nbsp;', ' ')
    $s = $s.Replace('&ndash;', [string][char]0x2013).Replace('&mdash;', [string][char]0x2014)
    $s = $s.Replace('&quot;', '"').Replace('&#39;', "'").Replace('&lt;', '<').Replace('&gt;', '>').Replace('&amp;', '&')
    $s = [regex]::Replace($s, '\s+', ' ')
    return $s.Trim()
}

function Resolve-Alias([hashtable]$alias, [string]$name) {
    $cur = $name
    for ($i = 0; $i -lt 4; $i++) {
        if (-not $alias.ContainsKey($cur) -or $alias[$cur] -eq $cur) { break }
        $cur = $alias[$cur]
    }
    return $cur
}

function Get-EnDescMap([string[]]$names, [bool]$withSuffix, [int]$round) {
    $map = @{}
    if ($names.Count -eq 0) { return $map }
    for ($off = 0; $off -lt $names.Count; $off += 50) {
        $batch = @($names[$off..([Math]::Min($off + 49, $names.Count - 1))] | Select-Object -Unique)
        $titles = ($batch | ForEach-Object { $t = $_; if ($withSuffix) { $t = $t + ' (Collectible)' }; [uri]::EscapeDataString($t) }) -join '|'
        $file = Join-Path $tmpGg ('batch_{0}_{1}.json' -f $round, $off)
        curl.exe -s -A $ggUa -o $file ($ggApi + $titles)
        $obj = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
        $alias = @{}
        if ($obj.query.normalized) { foreach ($n in $obj.query.normalized) { $alias[$n.from] = $n.to } }
        if ($obj.query.redirects) { foreach ($r in $obj.query.redirects) { $alias[$r.from] = $r.to } }
        $byTitle = @{}
        foreach ($p in $obj.query.pages) {
            if (-not $p.revisions) { continue }
            $wt = $p.revisions[0].slots.main.content
            if (-not $wt) { $wt = $p.revisions[0].slots.main.'*' }
            if (-not $wt) { continue }
            $enDesc = Clear-WikiMarkup (Get-EnDescFromWikitext $wt)
            if ($enDesc) { $byTitle[$p.title] = $enDesc }
        }
        foreach ($name in $batch) {
            $target = $name
            if ($withSuffix) { $target = $name + ' (Collectible)' }
            $resolved = Resolve-Alias $alias $target
            if ($byTitle.ContainsKey($resolved)) { $map[$name] = $byTitle[$resolved] }
            elseif ($byTitle.ContainsKey($target)) { $map[$name] = $byTitle[$target] }
        }
    }
    return $map
}

function Get-OtherNames([string]$wt) {
    # othername may span several bullet lines inside the infobox
    $m = [regex]::Match($wt, '(?m)^\|\s*othername\s*=([\s\S]*?)(?=^\s*\|\s*\w+\s*=)')
    if (-not $m.Success) { return @() }
    $out = @()
    foreach ($ln in ($m.Groups[1].Value -split "`r?`n")) {
        $t = $ln.Trim()
        if (-not $t) { continue }
        $t = $t -replace '^\*+', ''
        $t = $t.Trim().Trim('"')
        if ($t) { $out += $t }
    }
    return $out
}

function Get-SearchData([string[]]$names) {
    # 文档缺失或条目无英文名时，用 wiki.gg 站内搜索 + othername 别名验证定位条目，同时取回英文名与英文图鉴描述
    $result = @{}
    foreach ($name in $names) {
        $q = 'insource:"' + $name.Replace('"', '\"') + '" hastemplate:"Collectible infobox"'
        $u = 'https://arknights.wiki.gg/api.php?action=query&list=search&format=json&formatversion=2&srlimit=3&srsearch=' + [uri]::EscapeDataString($q)
        $file = Join-Path $tmpGg ('search_' + ([Math]::Abs($name.GetHashCode())) + '.json')
        curl.exe -s -A $ggUa -o $file $u
        $obj = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
        if (-not $obj.query.search) { continue }
        $candidates = @($obj.query.search | ForEach-Object { $_.title })
        $titles = ($candidates | ForEach-Object { [uri]::EscapeDataString($_) }) -join '|'
        $file2 = Join-Path $tmpGg ('searchpage_' + ([Math]::Abs($name.GetHashCode())) + '.json')
        curl.exe -s -A $ggUa -o $file2 ($ggApi + $titles)
        $obj2 = [System.IO.File]::ReadAllText($file2, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
        foreach ($p in $obj2.query.pages) {
            if (-not $p.revisions) { continue }
            $wt = $p.revisions[0].slots.main.content
            if (-not $wt) { continue }
            $aliases = Get-OtherNames $wt
            $hit = $false
            foreach ($a in $aliases) {
                if ($a -eq $name -or $a -eq ($name + ' (Collectible)')) { $hit = $true; break }
            }
            if (-not $hit) { continue }
            $result[$name] = [pscustomobject]@{
                En = ($p.title -replace ' \(Collectible\)$', '')
                EnDesc = (Clear-WikiMarkup (Get-EnDescFromWikitext $wt))
            }
            break
        }
    }
    return $result
}

# ---------- 解析图鉴页 wikitext 并生成文档 ----------
function Build-ThemeDoc([string]$code, [string]$themeTitle, [string]$prtsPage, [string]$outFile, [string]$enNote) {
    $wt = Get-Prtswikitext $prtsPage

    # 分组标题序列
    $sections = @()
    foreach ($m in [regex]::Matches($wt, '==\s*(?:编号\.|No\.)?\s*([0-9A-Za-z]+)-([0-9A-Za-z]+)\s+([^=]+?)\s*==')) {
        $sections += [pscustomobject]@{ NoFrom = $m.Groups[1].Value; NoTo = $m.Groups[2].Value; Title = $m.Groups[3].Value.Trim() }
    }

    # 收藏品模板（参数含 ID=）
    $tpls = @(ConvertFrom-WikiTemplate -Text $wt -StartMarker '{{收藏品' | Where-Object { $_.Params.Contains('ID') -and $_.Params['ID'] })
    if ($tpls.Count -eq 0) { throw ("[{0}] 收藏品模板解析为 0，中止以免覆盖既有文档" -f $code) }
    Write-Output ("[{0}] templates parsed: {1}, sections: {2}" -f $code, $tpls.Count, $sections.Count)

    # 详情页补售价/解锁
    $names = @($tpls | ForEach-Object { $_.Params['名称'] })
    $detail = Get-DetailData -names $names -theme $themeTitle
    Write-Output ("[{0}] detail pages matched: {1}/{2}" -f $code, $detail.Count, $names.Count)

    # 英文名：优先复用现有文档标题对照，缺失的经 wiki.gg 搜索补齐（命中时顺带取回英文图鉴描述）
    $entryEn = @{}
    foreach ($t in $tpls) {
        $zhPre = (Clean-WikiText $t.Params['名称']) -replace '\(收藏品\)$', ''
        $entryEn[$t.Params['ID']] = $zhToEn[$zhPre]
    }
    $enDescMap = @{}
    $pendingZh = @($tpls | Where-Object { -not $entryEn[$_.Params['ID']] } | ForEach-Object { (Clean-WikiText $_.Params['名称']) -replace '\(收藏品\)$', '' } | Select-Object -Unique)
    if ($pendingZh.Count -gt 0) {
        Write-Output ("[{0}] en names via search: {1}" -f $code, $pendingZh.Count)
        $searched = Get-SearchData -names $pendingZh
        foreach ($t in $tpls) {
            $zhPre = (Clean-WikiText $t.Params['名称']) -replace '\(收藏品\)$', ''
            if (-not $entryEn[$t.Params['ID']] -and $searched.ContainsKey($zhPre)) {
                $enFound = $searched[$zhPre].En
                $entryEn[$t.Params['ID']] = $enFound
                if ($searched[$zhPre].EnDesc) { $enDescMap[$enFound] = $searched[$zhPre].EnDesc }
            }
        }
    }

    # 英文图鉴描述：直查标题 → (Collectible) 后缀
    $enNames = @($entryEn.Values | Where-Object { $_ } | Select-Object -Unique)
    $enDescs1 = Get-EnDescMap -names @($enNames | Where-Object { -not $enDescMap.ContainsKey($_) }) -withSuffix $false -round 1
    foreach ($k in $enDescs1.Keys) { $enDescMap[$k] = $enDescs1[$k] }
    $pending = @($enNames | Where-Object { -not $enDescMap.ContainsKey($_) })
    $enDescs2 = Get-EnDescMap -names $pending -withSuffix $true -round 2
    foreach ($k in $enDescs2.Keys) { $enDescMap[$k] = $enDescs2[$k] }
    Write-Output ("[{0}] en names: {1}/{2}, en descriptions: {3}/{4}" -f $code, $enNames.Count, $tpls.Count, $enDescMap.Count, $enNames.Count)

    $missingEn = New-Object System.Collections.ArrayList
    $missingDetail = New-Object System.Collections.ArrayList
    $badKeys = New-Object System.Collections.ArrayList
    $rarityMap = @{ '0' = '低稀有度'; '1' = '中稀有度'; '2' = '高稀有度' }

    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine(('# 集成战略「{0}」收藏品图鉴' -f $themeTitle))
    [void]$sb.AppendLine('')
    [void]$sb.AppendLine(('> 数据来源：[PRTS Wiki - {0}]({1})  ' -f $prtsPage, "https://prts.wiki/w/$([uri]::EscapeDataString($prtsPage))"))
    [void]$sb.AppendLine(('> 采集日期：{0} ｜ 共 {1} 件收藏品{2}' -f (Get-Date -Format 'yyyy-MM-dd'), $tpls.Count, $enNote))
    [void]$sb.AppendLine('')

    $secIdx = 0
    $first = $true
    foreach ($t in $tpls) {
        $id = $t.Params['ID']
        while ($secIdx -lt $sections.Count) {
            $s = $sections[$secIdx]
            $idCmp = $id.PadLeft(4, '0')
            if (($idCmp -ge $s.NoFrom.PadLeft(4, '0')) -and ($idCmp -le $s.NoTo.PadLeft(4, '0'))) { break }
            $secIdx++
        }
        if ($secIdx -lt $sections.Count -and $first) {
            [void]$sb.AppendLine(('## No.{0}-{1} {2}' -f $sections[$secIdx].NoFrom, $sections[$secIdx].NoTo, $sections[$secIdx].Title))
            [void]$sb.AppendLine('')
            $first = $false
        }
        elseif ($secIdx -lt $sections.Count -and $sections[$secIdx].NoFrom.PadLeft(4, '0') -eq $id.PadLeft(4, '0')) {
            [void]$sb.AppendLine(('## No.{0}-{1} {2}' -f $sections[$secIdx].NoFrom, $sections[$secIdx].NoTo, $sections[$secIdx].Title))
            [void]$sb.AppendLine('')
        }

        $zh = (Clean-WikiText $t.Params['名称']) -replace '\(收藏品\)$', ''
        $en = $entryEn[$id]
        if (-not $en) { $en = ''; [void]$missingEn.Add(("No.{0} {1}" -f $id, $zh)) }

        $titleLine = if ($en) { '### No.{0} {1} {2}' -f $id, $zh, $en } else { '### No.{0} {1}' -f $id, $zh }
        [void]$sb.AppendLine($titleLine)
        [void]$sb.AppendLine('')
        [void]$sb.AppendLine(('- **英文名**：{0}' -f $(if ($en) { $en } else { '待补（wiki.gg 尚未收录）' })))

        $rarity = $rarityMap[$t.Params['稀有度'].Trim()]
        if ($rarity) { [void]$sb.AppendLine(('- **稀有度**：{0}' -f $rarity)) }

        $d = $null
        if ($detail.ContainsKey($zh)) { $d = $detail[$zh] }
        $price = $t.Params['售价']
        if (-not $price -and $d) { $price = $d.Price }
        if ($price) { [void]$sb.AppendLine(('- **基础售价**：{0} 源石锭' -f $price)) }

        # 效果
        $branches = @(Split-EffectBranches $t.Params['效果'])
        if ($branches.Count -eq 0) { [void]$sb.AppendLine('- **效果**：（待补）') }
        elseif ($branches.Count -eq 1) { [void]$sb.AppendLine(('- **效果**：{0}' -f $branches[0])) }
        else {
            [void]$sb.AppendLine('- **效果**：')
            foreach ($b in $branches) { [void]$sb.AppendLine(('  - {0}' -f $b)) }
        }

        # 标记（角标）
        $marks = @()
        foreach ($bm in [regex]::Matches($t.Params['角标'], '\{\{收藏品/角标\|([a-z_0-9]+)\}\}')) {
            $k = $bm.Groups[1].Value
            if ($badges.ContainsKey($k)) { $marks += $badges[$k] } else { $marks += $k; [void]$badKeys.Add(("No.{0} {1}" -f $id, $k)) }
        }
        if ($marks.Count -gt 0) { [void]$sb.AppendLine(('- **标记**：{0}' -f ($marks -join '；'))) }

        # 解锁/获取条件（图鉴页条件 → 图鉴页获取 → 详情页解锁/获取）
        $cond = Clean-WikiText $t.Params['条件']
        if (-not $cond) { $cond = Clean-WikiText $t.Params['获取'] }
        if (-not $cond -and $d) { $cond = if ($d.Unlock) { $d.Unlock } else { $d.Obtain } }
        if ($cond) { [void]$sb.AppendLine(('- **解锁/获取条件**：{0}' -f $cond)) }

        # 图鉴描述（英）紧随中文描述之后
        $desc = Clean-WikiText $t.Params['描述']
        if ($desc) {
            [void]$sb.AppendLine(('- **图鉴描述**（{0} / {1}）：{2}' -f $zh, $(if ($en) { $en } else { '待补' }), $desc))
            if ($en -and $enDescMap.ContainsKey($en)) { [void]$sb.AppendLine(('- **图鉴描述（英）**：{0}' -f $enDescMap[$en])) }
        }

        [void]$sb.AppendLine('')
        if (-not $d) { [void]$missingDetail.Add(("No.{0} {1}" -f $id, $zh)) }
    }

    $text = $sb.ToString().TrimEnd()
    [System.IO.File]::WriteAllText((Join-Path $dir $outFile), $text, (New-Object System.Text.UTF8Encoding($false)))
    Write-Output ("[{0}] written: {1} ({2} chars)" -f $code, $outFile, $text.Length)
    if ($missingEn.Count -gt 0) { Write-Output ("[{0}] missing EN names ({1}): {2}" -f $code, $missingEn.Count, ($missingEn[0..([Math]::Min(9, $missingEn.Count - 1))] -join '、')) }
    if ($badKeys.Count -gt 0) { Write-Output ("[{0}] unknown badges: {1}" -f $code, (($badKeys | Select-Object -Unique) -join '、')) }
    if ($missingDetail.Count -gt 0) { Write-Output ("[{0}] no detail data ({1}): {2}" -f $code, $missingDetail.Count, ($missingDetail[0..([Math]::Min(9, $missingDetail.Count - 1))] -join '、')) }
}

foreach ($t in $themes) {
    Build-ThemeDoc -code $t.Code -themeTitle $t.Name -prtsPage $t.Page -outFile $t.File -enNote $t.EnNote
}

# ============================================================
# 阶段 2：比对全部主题文档，生成 ∩-通用收藏品.md
# ============================================================

function Get-ChineseName([string]$title) {
    $tokens = $title -split ' '
    $i = $tokens.Count - 1
    while ($i -ge 0 -and $tokens[$i] -notmatch '[\u4e00-\u9fff]') { $i-- }
    if ($i -lt 0) { return $title.Trim() }
    return (($tokens[0..$i] -join ' ').Trim())
}

$entries = New-Object System.Collections.ArrayList
foreach ($t in $themes) {
    $lines = Get-Content -Path (Join-Path $dir $t.File) -Encoding UTF8
    $cur = $null
    $inEffect = $false
    foreach ($line in $lines) {
        if ($line -match '^### No\.([A-Za-z]*\d+)\s+(.+)$') {
            if ($null -ne $cur) { [void]$entries.Add($cur) }
            $rawNo = $Matches[1]
            $entryName = $Matches[2].Trim()
            $sortKey = if ($rawNo -match '^\d+$') { 'A' + ([int]$rawNo).ToString('D4') } else { 'B' + $rawNo }
            $cur = [pscustomobject][ordered]@{ Theme = $t.Name; ThemeIdx = $t.Idx; No = $rawNo; SortKey = $sortKey; Name = $entryName; ZhName = Get-ChineseName $entryName; EnName = ''; Rarity = ''; Price = ''; Effect = ''; Unlock = ''; Desc = ''; EnDesc = '' }
            $inEffect = $false
        } elseif ($null -ne $cur) {
            if ($line -match '^- \*\*英文名\*\*：(.*)$') { $cur.EnName = $Matches[1].Trim(); $inEffect = $false }
            elseif ($line -match '^- \*\*稀有度\*\*：(.*)$') { $cur.Rarity = $Matches[1].Trim(); $inEffect = $false }
            elseif ($line -match '^- \*\*基础售价\*\*：(.*)$') { $cur.Price = $Matches[1].Trim(); $inEffect = $false }
            elseif ($line -match '^- \*\*效果\*\*：\s*(.*)$') { $cur.Effect = $Matches[1].Trim(); $inEffect = $true }
            elseif ($line -match '^- \*\*解锁/获取条件\*\*：(.*)$') { $cur.Unlock = $Matches[1].Trim(); $inEffect = $false }
            elseif ($line -match '^- \*\*图鉴描述\*\*(?:（[^）]*）)?：(.*)$') { $cur.Desc = $Matches[1].Trim(); $inEffect = $false }
            elseif ($line -match '^- \*\*图鉴描述（英）\*\*：(.*)$') { $cur.EnDesc = $Matches[1].Trim(); $inEffect = $false }
            elseif ($inEffect -and $line -match '^\s+- (.+)$') { $sub = $Matches[1].Trim(); $cur.Effect = if ($cur.Effect) { $cur.Effect + '；' + $sub } else { $sub } }
        }
    }
    if ($null -ne $cur) { [void]$entries.Add($cur) }
}

$common = @($entries | Group-Object -Property ZhName | Where-Object { @($_.Group | ForEach-Object { $_.Theme } | Select-Object -Unique).Count -ge 2 })
$common = @($common | Sort-Object -Property { [int]($_.Group[0].ThemeIdx) }, { $_.Group[0].SortKey })
Write-Output ("common collectibles (>=2 themes): {0}" -f $common.Count)

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('# 集成战略通用收藏品图鉴')
[void]$sb.AppendLine('')
[void]$sb.AppendLine('> 由脚本比对全部主题文档的收藏品名称自动生成，收录**至少出现在 2 个主题**的收藏品（含诅咒等负面收藏品），同名条目按主题列出各项数据。')
[void]$sb.AppendLine('')
[void]$sb.AppendLine(('检索基准：{0} ｜ 通用收藏品共 **{1}** 件。' -f (Get-Date -Format 'yyyy-MM-dd'), $common.Count))
[void]$sb.AppendLine('')
[void]$sb.AppendLine('## 详情')
foreach ($g in $common) {
    $occ = @($g.Group | Sort-Object -Property 'ThemeIdx', 'SortKey')
    $en = @($occ | Where-Object { $_.EnName -ne '' -and $_.EnName -notmatch '^待补' } | Select-Object -ExpandProperty EnName -Unique)
    $heading = if ($en.Count -ge 1) { '{0} {1}' -f $g.Name, $en[0] } else { $g.Name }
    $descName = if ($en.Count -ge 1) { '{0} / {1}' -f $g.Name, $en[0] } else { '{0} / 待补' -f $g.Name }
    [void]$sb.AppendLine('')
    [void]$sb.AppendLine(('### {0}' -f $heading))
    [void]$sb.AppendLine('')
    [void]$sb.AppendLine('| 主题 | 编号 | 稀有度 | 基础售价 | 效果 | 解锁/获取条件 |')
    [void]$sb.AppendLine('|---|---|---|---|---|---|')
    foreach ($e in $occ) {
        [void]$sb.AppendLine(('| {0} | No.{1} | {2} | {3} | {4} | {5} |' -f $e.Theme, $e.No, $e.Rarity, $e.Price, $e.Effect, $e.Unlock))
    }
    $descs = @($occ | Where-Object { $_.Desc -ne '' } | Select-Object -ExpandProperty Desc -Unique)
    if ($descs.Count -eq 1) {
        [void]$sb.AppendLine('')
        [void]$sb.AppendLine(('- **图鉴描述**（{0}）：{1}' -f $descName, $descs[0]))
    } else {
        # 各主题间常仅省略号写法不同（...... / ……），按归一化文本去重，避免重复输出同一条描述
        $distinct = @($descs | Group-Object -Property { ($_ -replace '…', '...' -replace '\s+', '').Trim() } | ForEach-Object { $_.Group[0] })
        [void]$sb.AppendLine('')
        if ($distinct.Count -eq 1) {
            [void]$sb.AppendLine(('- **图鉴描述**（{0}）：{1}' -f $descName, $distinct[0]))
        } else {
            foreach ($e in ($occ | Where-Object { $_.Desc -ne '' })) {
                [void]$sb.AppendLine(('- **图鉴描述**（{0}｜{1}）：{2}' -f $e.Theme, $descName, $e.Desc))
            }
        }
    }
    $enDescs = @($occ | Where-Object { $_.EnDesc -ne '' } | Select-Object -ExpandProperty EnDesc -Unique)
    if ($enDescs.Count -eq 1) {
        [void]$sb.AppendLine(('- **图鉴描述（英）**：{0}' -f $enDescs[0]))
    } elseif ($enDescs.Count -gt 1) {
        # 与中文描述同策略：按归一化文本去重
        $enDistinct = @($enDescs | Group-Object -Property { ($_ -replace '\s+', '').Trim() } | ForEach-Object { $_.Group[0] })
        if ($enDistinct.Count -eq 1) {
            [void]$sb.AppendLine(('- **图鉴描述（英）**：{0}' -f $enDistinct[0]))
        } else {
            foreach ($e in ($occ | Where-Object { $_.EnDesc -ne '' })) {
                [void]$sb.AppendLine(('- **图鉴描述（英）**（{0}｜{1}）：{2}' -f $e.Theme, $descName, $e.EnDesc))
            }
        }
    }
}

$text = $sb.ToString().TrimEnd()
$outPath = Join-Path $dir '∩-通用收藏品.md'
[System.IO.File]::WriteAllText($outPath, $text, (New-Object System.Text.UTF8Encoding($false)))
Write-Output ("written: {0} ({1} chars)" -f $outPath, $text.Length)