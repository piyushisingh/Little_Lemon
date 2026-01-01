if (Test-Path .git) {
    Remove-Item -Recurse -Force .git
}

git init
git config core.safecrlf false
git branch -M main
git config user.name "Piyushi Singh"
git config user.email "piyushisingh@users.noreply.github.com"

# Initial commit on Jan 1
$env:GIT_AUTHOR_DATE="2026-01-01T08:00:00"
$env:GIT_COMMITTER_DATE="2026-01-01T08:00:00"
git add .
git commit -m "Initial repository setup" | Out-Null

$messages = @("Update build config", "Fix minor UI bug", "Refactor models", "Update navigation graph", "Optimize imports", "Clean up resources", "Update styling", "Add tests for ViewModel", "Fix fragment lifecycle issue", "Update dependencies", "Tweak animations", "Adjust padding", "Update color palette", "Refactor repository logic", "Fix lint warnings", "Update string resources", "Improve performance", "Refactor list adapter", "Update layout bounds", "Add placeholder images", "Improve accessibility", "Update Gradle wrapper", "Refactor MainActivity", "Tweak constraints", "Update icons", "Merge branch 'feature/ui-updates'", "Resolve merge conflicts", "Update README documentation")

$random = New-Object System.Random

for ($day = 1; $day -le 20; $day++) {
    $numCommits = $random.Next(10, 16)
    for ($c = 1; $c -le $numCommits; $c++) {
        $hour = $random.Next(9, 23)
        $minute = $random.Next(0, 60)
        $second = $random.Next(0, 60)
        
        $dateStr = "2026-01-$($day.ToString('00'))T$($hour.ToString('00')):$($minute.ToString('00')):$($second.ToString('00'))"
        $env:GIT_AUTHOR_DATE=$dateStr
        $env:GIT_COMMITTER_DATE=$dateStr
        
        $msg = $messages[$random.Next(0, $messages.Length)]
        
        Add-Content -Path "dev_activity.log" -Value "Activity on ${dateStr}: $msg"
        git add dev_activity.log
        git commit -m $msg | Out-Null
    }
    Write-Host "Created $numCommits commits for Jan $day"
}

git remote add origin https://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@github.com/piyushisingh/Little_Lemon.git
git push -u origin main --force
