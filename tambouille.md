# Tambouille

Le script [`tambouille.sh`](tambouille.sh) sert à renommer le projet en "Cosmic Money", et à modifier le logo.

Dans l'idée, à chaque nouvelle version de `Money Buster`, ce dépôt est mis à jour, puis modifié via le script nommé ci-avant.

Voici les étapes à suivre :

```shell
# Ménage
git reset HEAD^ && find . -name 'cosmicmoney*' -o -name 'bobotig' | /bin/xargs rm -rf && git reset --hard

# Mise à jour
# git remote add upstream https://gitlab.com/eneiluj/moneybuster.git      
git fetch upstream master && git rebase upstream/master

# Modifications
bash tambouille.sh && git add .gitignore && git add -A && git commit -m 'feat: Cosmic Money!'
```

Pour générer les fichiers APK, dans Android Studio : Menu > Build > "Generate App Signed Bundle/APK...".

Afin de partager les fichiers :

```shell
# Release
python3 -m http.server -d ./app/normal [PORT]

# Dev
python3 -m http.server -d ./app/dev [PORT]
```
