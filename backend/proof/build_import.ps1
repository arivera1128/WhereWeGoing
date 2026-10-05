$ErrorActionPreference = 'Stop'
function Sql-Text([string]$value) { return "'" + $value.Replace("'", "''") + "'" }
$restaurants = @(Import-Csv -LiteralPath (Join-Path $PSScriptRoot 'restaurants.csv'))
$locations = @(Import-Csv -LiteralPath (Join-Path $PSScriptRoot 'locations.csv'))
$ids = @{}
$keys = @{}
$sql = [System.Collections.Generic.List[string]]::new()
$sql.Add('-- Development fixtures only. Generated from CSV; inserts never overwrite existing rows.')
$sql.Add('begin;')
foreach ($row in $restaurants) {
    $id = ([guid]::Parse($row.restaurant_id)).ToString()
    if ($ids.ContainsKey($id) -or $keys.ContainsKey($row.import_key) -or [string]::IsNullOrWhiteSpace($row.name)) { throw 'Invalid or duplicate restaurant' }
    $ids[$id] = $true; $keys[$row.import_key] = $true
    $values = @($id, $row.import_key, $row.name) | ForEach-Object { Sql-Text $_ }
    $sql.Add('insert into ww_proof.restaurant(restaurant_id,import_key,name) values (' + ($values -join ',') + ') on conflict do nothing;')
}
$locationIds = @{}; $locationKeys = @{}
foreach ($row in $locations) {
    $id = ([guid]::Parse($row.location_id)).ToString()
    $restaurant = ([guid]::Parse($row.restaurant_id)).ToString()
    if (-not $ids.ContainsKey($restaurant) -or $locationIds.ContainsKey($id) -or $locationKeys.ContainsKey($row.import_key) -or [string]::IsNullOrWhiteSpace($row.address)) { throw 'Invalid or duplicate location' }
    $locationIds[$id] = $true; $locationKeys[$row.import_key] = $true
    $values = @($id, $restaurant, $row.import_key, $row.address, $row.source_note) | ForEach-Object { Sql-Text $_ }
    $sql.Add('insert into ww_proof.location(location_id,restaurant_id,import_key,address,source_note) values (' + ($values -join ',') + ') on conflict do nothing;')
}
$sql.Add('commit;')
Set-Content -LiteralPath (Join-Path $PSScriptRoot '002_import.sql') -Value $sql -Encoding utf8
Write-Output "Prepared $($restaurants.Count) restaurants and $($locations.Count) locations. No remote database was changed."
