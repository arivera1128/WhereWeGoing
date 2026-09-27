param(
    [string]$StatusPath = "docs/PROJECT_STATUS.md",
    [string]$HubPath = "docs/PROJECT_HUB.html"
)

$ErrorActionPreference = "Stop"

function Encode([string]$Value) {
    return [System.Net.WebUtility]::HtmlEncode($Value)
}

function Read-Field([string]$Body, [string]$Name) {
    $match = [regex]::Match($Body, "(?m)^- \*\*$([regex]::Escape($Name)):\*\*\s*(.+)$")
    if ($match.Success) { return $match.Groups[1].Value.Trim() }
    return ""
}

function Progress-Bar([int]$Percent, [string]$Label) {
    $safeLabel = Encode $Label
    return @"
<div class="progress-line">
  <div class="progress-label"><span>$safeLabel</span><strong>$Percent%</strong></div>
  <div class="progress-track" role="progressbar" aria-label="$safeLabel" aria-valuemin="0" aria-valuemax="100" aria-valuenow="$Percent">
    <span class="progress-value" style="width: $Percent%"></span>
  </div>
</div>
"@
}

function Status-Class([string]$Status) {
    switch -Regex ($Status.ToLowerInvariant()) {
        "done|accepted" { return "done" }
        "in progress" { return "progress" }
        "proposed" { return "proposed" }
        "blocked|open" { return "open" }
        "parked" { return "parked" }
        default { return "idea" }
    }
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$resolvedStatus = Join-Path $projectRoot $StatusPath
$resolvedHub = Join-Path $projectRoot $HubPath

if (-not (Test-Path -LiteralPath $resolvedStatus)) { throw "Status file not found: $resolvedStatus" }
if (-not (Test-Path -LiteralPath $resolvedHub)) { throw "Hub file not found: $resolvedHub" }

$status = Get-Content -LiteralPath $resolvedStatus -Raw -Encoding UTF8
$hub = Get-Content -LiteralPath $resolvedHub -Raw -Encoding UTF8

$lastReviewed = Read-Field $status "Last reviewed"
$primaryTarget = Read-Field $status "Primary target"

$steps = @()
$stepMatches = [regex]::Matches($status, "(?ms)^## Step (\d+): (.+?)\r?\n(.*?)(?=^## Step \d+:|^## Update procedure|\z)")
foreach ($match in $stepMatches) {
    $number = [int]$match.Groups[1].Value
    $title = $match.Groups[2].Value.Trim()
    $body = $match.Groups[3].Value
    $items = [regex]::Matches($body, "(?m)^- \[([xX ])\] (.+)$")
    $completed = @($items | Where-Object { $_.Groups[1].Value -match "[xX]" } | ForEach-Object { $_.Groups[2].Value.Trim() })
    $remaining = @($items | Where-Object { $_.Groups[1].Value -eq " " } | ForEach-Object { $_.Groups[2].Value.Trim() })
    $total = $items.Count
    $percent = if ($total -gt 0) { [int][math]::Round(($completed.Count / $total) * 100) } else { 0 }
    $weightText = Read-Field $body "Weight"
    $weight = if ($weightText -match "^\d+$") { [int]$weightText } else { 0 }
    $steps += [pscustomobject]@{
        Number = $number
        Title = $title
        Status = Read-Field $body "Status"
        Weight = $weight
        Phase = Read-Field $body "Phase"
        Summary = Read-Field $body "Summary"
        DependsOn = Read-Field $body "Depends on"
        NextResult = Read-Field $body "Next result"
        Completed = $completed
        Remaining = $remaining
        Percent = $percent
    }
}

if ($steps.Count -eq 0) { throw "No roadmap steps were parsed from $resolvedStatus" }

$totalWeight = ($steps | Measure-Object -Property Weight -Sum).Sum
$weightedProgress = if ($totalWeight -gt 0) {
    [int][math]::Round((($steps | ForEach-Object { $_.Percent * $_.Weight } | Measure-Object -Sum).Sum) / $totalWeight)
} else { 0 }

$milestones = @()
$milestoneBlock = [regex]::Match($status, "(?ms)^## Milestones\s*(.*?)(?=^## Workstreams)").Groups[1].Value
foreach ($line in ($milestoneBlock -split "\r?\n")) {
    if ($line -match '^\|\s*([^|]+?)\s*\|\s*(\d+)\s*\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|$' -and $Matches[1].Trim() -ne "Milestone") {
        $milestones += [pscustomobject]@{ Name=$Matches[1].Trim(); Percent=[int]$Matches[2]; Meaning=$Matches[3].Trim(); Exit=$Matches[4].Trim() }
    }
}

$workstreams = @()
$workstreamBlock = [regex]::Match($status, "(?ms)^## Workstreams\s*(.*?)(?=^## Step 1:)").Groups[1].Value
foreach ($line in ($workstreamBlock -split "\r?\n")) {
    if ($line -match '^\|\s*([^|]+?)\s*\|\s*(\d+)\s*\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|$' -and $Matches[1].Trim() -ne "Workstream") {
        $workstreams += [pscustomobject]@{ Name=$Matches[1].Trim(); Percent=[int]$Matches[2]; Evidence=$Matches[3].Trim(); Next=$Matches[4].Trim() }
    }
}

$milestoneCards = ($milestones | ForEach-Object {
    $bar = Progress-Bar $_.Percent $_.Name
    "<article class=`"card`"><h3>$(Encode $_.Name)</h3>$bar<p>$(Encode $_.Meaning)</p><p class=`"meta`"><strong>Exit:</strong> $(Encode $_.Exit)</p></article>"
}) -join "`n"

$stepBars = ($steps | ForEach-Object { Progress-Bar $_.Percent ("Step {0}: {1}" -f $_.Number, $_.Title) }) -join "`n"

$dashboard = @"
<section id="progress" data-generated="PROJECT_STATUS.md">
  <p class="eyebrow">Generated from PROJECT_STATUS.md</p>
  <h2>Path to live</h2>
  <div class="dashboard-head">
    <div>
      <span class="badge progress">Primary target</span>
      <h3>$(Encode $primaryTarget)</h3>
      <p>Overall weighted progress toward the current roadmap: <strong>$weightedProgress%</strong></p>
    </div>
    <div class="overall-ring" style="--value: $weightedProgress"><span>$weightedProgress%</span></div>
  </div>
  <p class="meta">Last reviewed: $(Encode $lastReviewed). Percentages reflect checked evidence in the tracker and should be revised when scope changes.</p>
  <div class="callout proposed" style="margin:1rem 0">
    <strong>Updating progress:</strong> edit <a href="PROJECT_STATUS.md">PROJECT_STATUS.md</a>, then run <code>.\tools\build_project_hub.ps1</code> from the project root. Progress sections on this page are regenerated automatically.
  </div>
  <div class="grid three milestone-grid">$milestoneCards</div>
  <h3 style="margin-top:1.5rem">Roadmap progress</h3>
  <div class="card">$stepBars</div>
</section>
"@

$roadmapItems = ($steps | ForEach-Object {
    $class = Status-Class $_.Status
    @"
<div class="phase">
  <div class="phase-no">Step $($_.Number)<br><span class="badge $class">$(Encode $_.Status)</span></div>
  <div>
    <h3>$(Encode $_.Title)</h3>
    $(Progress-Bar $_.Percent ("{0} complete" -f $_.Title))
    <p>$(Encode $_.Summary)</p>
    <p class="meta"><strong>Next result:</strong> $(Encode $_.NextResult)</p>
  </div>
</div>
"@
}) -join "`n"

$roadmap = @"
<section id="roadmap" data-generated="PROJECT_STATUS.md">
  <p class="eyebrow">Generated roadmap</p>
  <h2>Steps to a live app</h2>
  <p>Progress is calculated from the evidence checklist in <a href="PROJECT_STATUS.md">PROJECT_STATUS.md</a>. Step weights affect the overall percentage; they do not represent calendar estimates.</p>
  $roadmapItems
</section>
"@

$sessionItems = ($steps | ForEach-Object {
    $class = Status-Class $_.Status
    $completedItems = if ($_.Completed.Count -gt 0) { ($_.Completed | ForEach-Object { "<li>$(Encode $_)</li>" }) -join "`n" } else { "<li>Nothing is credited yet.</li>" }
    $remainingItems = if ($_.Remaining.Count -gt 0) { ($_.Remaining | ForEach-Object { "<li>$(Encode $_)</li>" }) -join "`n" } else { "<li>This checklist is complete.</li>" }
    $open = if ($_.Number -eq 1) { " open" } else { "" }
    @"
<details$open>
  <summary>Step $($_.Number) · $(Encode $_.Title) <span class="badge $class">$($_.Percent)%</span></summary>
  <div class="session-body">
    $(Progress-Bar $_.Percent ("Step {0} progress" -f $_.Number))
    <div class="session-meta">
      <div><strong>Phase</strong>$(Encode $_.Phase)</div>
      <div><strong>Depends on</strong>$(Encode $_.DependsOn)</div>
      <div><strong>Next result</strong>$(Encode $_.NextResult)</div>
    </div>
    <p>$(Encode $_.Summary)</p>
    <div class="grid two evidence-grid">
      <div><h3>Completed</h3><ul class="complete-list">$completedItems</ul></div>
      <div><h3>Still needed</h3><ul class="checklist">$remainingItems</ul></div>
    </div>
  </div>
</details>
"@
}) -join "`n"

$sessions = @"
<section id="sessions" data-generated="PROJECT_STATUS.md">
  <p class="eyebrow">One bounded step at a time</p>
  <h2>Progress details</h2>
  <p>Each percentage is calculated from the completed and remaining items below. Update the Markdown tracker, then regenerate this page.</p>
  <div class="session-list">$sessionItems</div>
</section>
"@

$workstreamRows = ($workstreams | ForEach-Object {
    "<tr><td><strong>$(Encode $_.Name)</strong></td><td>$(Progress-Bar $_.Percent $_.Name)</td><td>$(Encode $_.Evidence)</td><td>$(Encode $_.Next)</td></tr>"
}) -join "`n"

$workstreamSection = @"
<section id="workstreams" data-generated="PROJECT_STATUS.md">
  <p class="eyebrow">Parallel view</p>
  <h2>Workstream health</h2>
  <div class="table-wrap"><table>
    <thead><tr><th>Workstream</th><th>Progress</th><th>Current evidence</th><th>Next useful result</th></tr></thead>
    <tbody>$workstreamRows</tbody>
  </table></div>
</section>
"@

function Replace-Section([string]$Html, [string]$Id, [string]$Replacement) {
    $pattern = "(?s)<section id=`"$([regex]::Escape($Id))`".*?</section>"
    if (-not [regex]::IsMatch($Html, $pattern)) { throw "Section '$Id' was not found in the hub." }
    return [regex]::Replace($Html, $pattern, [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $Replacement }, 1)
}

if ($hub -match '<section id="progress"') {
    $hub = Replace-Section $hub "progress" $dashboard
} else {
    $hub = $hub.Replace('<section id="north-star">', "$dashboard`r`n<section id=`"north-star`">")
}
$hub = Replace-Section $hub "roadmap" $roadmap
$hub = Replace-Section $hub "sessions" $sessions
$hub = Replace-Section $hub "workstreams" $workstreamSection

$hub = [regex]::Replace($hub, '<p class="meta">Working hub · .*?</p>', "<p class=`"meta`">Generated dashboard · Last reviewed $(Encode $lastReviewed) · Product name remains TBD</p>")

[System.IO.File]::WriteAllText($resolvedHub, $hub, [System.Text.UTF8Encoding]::new($false))
Write-Host "Generated $resolvedHub"
Write-Host "Overall weighted progress: $weightedProgress%"
Write-Host "Parsed steps: $($steps.Count); milestones: $($milestones.Count); workstreams: $($workstreams.Count)"
