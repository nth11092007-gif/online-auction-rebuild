$xml = [xml](Get-Content 'd:\BigAssignment_MDHH\target\checkstyle-result.xml')
Write-Host "Total violations:" $xml.checkstyle.file.error.Count
Write-Host ""
Write-Host "--- By source (check type) ---"
$xml.checkstyle.file.error | Group-Object { $_.source.Split('.')[-1] } | Sort-Object Count -Descending | ForEach-Object { Write-Host ("{0,5}  {1}" -f $_.Count, ($_.Name -replace 'Check$','')) }
Write-Host ""
Write-Host "--- By file ---"
$xml.checkstyle.file | ForEach-Object { Write-Host ("{0,5}  {1}" -f $_.error.Count, ($_.name -replace '.*\\src\\main\\java\\','')) }
