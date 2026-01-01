git init
git config core.safecrlf false

git branch -M main

$env:GIT_AUTHOR_DATE="2026-01-01T10:00:00"
$env:GIT_COMMITTER_DATE="2026-01-01T10:00:00"
git add build.gradle.kts settings.gradle.kts gradle.properties local.properties gradle/ .gitignore app/build.gradle.kts app/src/main/AndroidManifest.xml
git commit -m "Initial commit: Project setup and Gradle configuration"

$env:GIT_AUTHOR_DATE="2026-01-05T12:30:00"
$env:GIT_COMMITTER_DATE="2026-01-05T12:30:00"
git add app/src/main/res/values/ app/src/main/res/values-night/
git commit -m "Add core resources, themes, and colors"

$env:GIT_AUTHOR_DATE="2026-01-10T14:45:00"
$env:GIT_COMMITTER_DATE="2026-01-10T14:45:00"
git add app/src/main/res/layout/ app/src/main/res/anim/ app/src/main/res/drawable/ app/src/main/res/mipmap*/ app/src/main/res/navigation/ app/src/main/res/xml/ app/src/main/res/menu/
git commit -m "Add UI layouts, navigation graph, and drawable assets"

$env:GIT_AUTHOR_DATE="2026-01-15T16:20:00"
$env:GIT_COMMITTER_DATE="2026-01-15T16:20:00"
git add app/src/main/java/com/littlelemon/menu/data/ app/src/main/java/com/littlelemon/menu/ui/
git commit -m "Implement data models and UI fragments"

$env:GIT_AUTHOR_DATE="2026-01-20T18:15:00"
$env:GIT_COMMITTER_DATE="2026-01-20T18:15:00"
git add app/src/main/java/com/littlelemon/menu/MainActivity.kt
git add .
git commit -m "Add main activity and finalize initial implementation"

git remote add origin https://github.com/piyushisingh/Little_Lemon.git
git push -u origin main
