# Git Instructions for PlaceableItemsReworked

## ⚠️ IMPORTANT WARNING

**ALL FILES WERE DELETED** by running:
- `git reset --hard` - reset all tracked changes
- `git clean -fd` - deleted all untracked files

Since there were no commits, git had no history to restore from.

## 📝 CRLF Configuration

To configure CRLF settings:

```bash
git config --global core.autocrlf true
```

## 🧹 Cleaning Git Repository Safely

**ALWAYS COMMIT FIRST BEFORE CLEANING!**

```bash
git add .
git commit -m "Initial commit"
```

Then clean safely:

```bash
git rm --cached -r .gradle/
git rm --cached -r .idea/
git rm --cached -r build/
```

## 📁 Recommended .gitignore

```
.gradle/
build/
.idea/
*.iml
*.class
*.jar
```

## 🔄 Recovery Options

1. **Check Recycle Bin** - files might be there
2. **System Restore** - try Windows System Restore
3. **File Recovery Tools** - use tools like Recuva
4. **Backup** - restore from backup if available

## 🚀 Starting Fresh

1. Recreate project structure
2. Add .gitignore file
3. Commit regularly
4. Use `git status` before cleaning