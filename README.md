identity-test-users
===================

See https://sites.google.com/a/guardian.co.uk/guardan-identity/identity/test-users for more details.

# Moving to main

The `master` branch has now been renamed to `main`. If you work with this repository, you need to make some changes to your local repository. We recommend you run the following sequence of commands, which will rename your master branch to main and set main as your default branch.

```
git fetch --all
git remote set-head origin -a
git branch master --set-upstream-to origin/main
git branch -m master main
```
