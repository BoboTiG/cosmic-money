# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

## 0.1.20 – 2024-08-11

### Changed

- Remove declaring foreground service permissions @thgoebel [!60](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/60)

## 0.1.19 – 2024-07-21

### Added

- Add support for per-app language preference @thgoebel [!56](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/56)

### Changed

- Bump AGP to 8.3.0 @AndyScherzinger [!53](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/53)
- Bump cmd tools and compile target to latest @AndyScherzinger [!54](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/54)
- Update dependencies and AGP @AndyScherzinger [!55](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/55)
- Replace foreground SyncService with SyncWorker @thgoebel [!58](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/58)
- Set timestamp of duplicated bill to now @thgoebel [!57](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/57)

## 0.1.18 – 2024-02-25

### Added

- Monochrome app icon
- Ability to delete members of local projects
- Help texts if the project has no members or no bills (#117)
- Support for Croatian tax QR codes (#129)

### Changed

- Material 3 design and other UI polishing
- Show all projects in navigation drawer
- Reorder "New Bill/Edit Bill" screen to show important fields at the top (#126)
- Auto-focus on "Amount" field rather than "Title" field in "New Bill" screen (#126)
- Allow 0 as an amount (#125)
- Use new Cospend API if available
- Remove smart sync settings since it is always used
- Use Sdk 34 and make necessary adjustments
- Import Cert4Android instead of having it as a git submodule

### Fixed

- Request notification permission on Android 13 + 14 (fixes notifications not shown) (#138, #139)
- Remove spurious space in CSV export (#111)
- Autofill for email and password in the "Add project" screen (#119)

## 0.1.17 – 2024-01-03

### Changed

- target sdk 33
- switch from me.dm7.barcodescanner:zxing to com.github.yuriy-budiyev:code-scanner
- detect and handle cospend:// links in new project form just like IHM invitation links
  [#108](https://gitlab.com/eneiluj/moneybuster/issues/108) @ghost1

### Fixed

- Fix disabled deletion status retrieval
  [!24](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/24) @nicofrand

## 0.1.16 – 2022-03-08
### Added
- accept IHM invitation links in new project form and when clicked or scanned
  [#91](https://gitlab.com/eneiluj/moneybuster/issues/91) @almet
- support for scanning austrian tax office QR codes to prefill some fields
  [!20](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/20) @vauvenal5

### Changed
- prevent bill deletion when it is disabled in the project
  [#99](https://gitlab.com/eneiluj/moneybuster/issues/99) @Shining-cat
- sync my access level, check it before adding/editing a member, WARNING: will work with Cospend >= v1.4.4  
  [#98](https://gitlab.com/eneiluj/moneybuster/issues/98) @macapple194
- handle cospend+http:// and ihatemoney+http:// links
  [#91](https://gitlab.com/eneiluj/moneybuster/issues/91) @almet

### Fixed
- fix bill deletion/edition in pushLocalChanges when remote bill does not exist
  [#99](https://gitlab.com/eneiluj/moneybuster/issues/99) @Shining-cat
- fix bug in category edition when using old payment modes
  [#98](https://gitlab.com/eneiluj/moneybuster/issues/98) @macapple194
- fix change payment mode detection
  [#98](https://gitlab.com/eneiluj/moneybuster/issues/98) @macapple194
- fix stats for IHM projects  
  [#101](https://gitlab.com/eneiluj/moneybuster/issues/101) @rootsh0pf
- use port in cospend:// and ihatemoney:// links
  [#91](https://gitlab.com/eneiluj/moneybuster/issues/91) @almet

## 0.1.15 – 2021-11-20
### Added
- simple math operations in amount field
  [#83](https://gitlab.com/eneiluj/moneybuster/issues/83) @mbbert
- new option to disable/hide all nextcloud-related stuff
  [#77](https://gitlab.com/eneiluj/moneybuster/issues/77) @Holoserica
- currency management
  [!18](https://gitlab.com/eneiluj/moneybuster/-/merge_requests/18) @ma11753
  
### Changed
- show confirmation dialog when pressing back on bill edition (if values have changed)
  [#84](https://gitlab.com/eneiluj/moneybuster/issues/84) @mbbert
  
### Fixed
- bump SSO lib to 0.6.0 to work with NC Files 3.18.0
  [89](https://gitlab.com/eneiluj/moneybuster/issues/89) @mrclschstr @daufinsyd

## 0.1.13 – 2021-10-01
### Changed
- new category and payment mode filter entries
  [#74](https://gitlab.com/eneiluj/moneybuster/issues/74) @OSevangelist

### Fixed
- payment mode list everywhere
  [#74](https://gitlab.com/eneiluj/moneybuster/issues/74) @OSevangelist

## 0.1.12 – 2021-09-28
### Added
- bill duplication from bill edition
- "comment" field for local and cospend projects
  [#70](https://gitlab.com/eneiluj/moneybuster/issues/70) @mMuck1
- new theme setting choice to follow system settings
  [#69](https://gitlab.com/eneiluj/moneybuster/issues/69) @Mynacol

### Changed
- remove project deletion button in project sync error dialog
  [#71](https://gitlab.com/eneiluj/moneybuster/issues/71) @jedie
- remove per-member bill counter in sidebar
- improve request error messages, inform if server is in maintenance mode
- now possible to create bills with only one member
- improve project selector design, add type icons
- adapt to new custom payment modes management
  [#74](https://gitlab.com/eneiluj/moneybuster/issues/74) @OSevangelist

### Fixed
- make import more robust, consider lines with empty values as empty lines
  [#73](https://gitlab.com/eneiluj/moneybuster/issues/73) @kyzkazk
- fix missing field in exported dummy bills
  [#73](https://gitlab.com/eneiluj/moneybuster/issues/73) @kyzkazk
- fix project creation using NC account, don't store the password locally
  [#58](https://gitlab.com/eneiluj/moneybuster/issues/58) @call-me-matt

## 0.1.11 – 2021-08-03
### Fixed
- bug in Android 5, project sync with spaces in passwords
[#72](https://gitlab.com/eneiluj/moneybuster/issues/72) @hugodu69

## 0.1.9 – 2021-08-01
### Changed
- adjust some UI messages
- add project name in search bar text
- update translations
- upgrade gradle plugin
- bump dependencies
- change color of sync toast
- adapt CI to latest build tools

### Fixed
- qrcode scanner with Android API < 24 (< Android 7 Nougat)
- bump SSO lib to snapshot to make it work with API >= 30 (Android >= 11)

## 0.1.8 – 2020-11-08
### Added
- ability to open/scan cospend:// and ihatemoney:// links to automatically import projects

### Changed
- bump gradle, gradle plugin, cert4android

### Fixed
- settlement table text and spinner color

## 0.1.7 – 2020-07-06
### Fixed
- crash when creating a bill
[#57](https://gitlab.com/eneiluj/moneybuster/issues/57) @cyko69

## 0.1.6 – 2020-07-03
### Changed
- new design (from NC Files and NC Notes)
- allow to move app to external storage
[#55](https://gitlab.com/eneiluj/moneybuster/issues/55) @call-me-matt

## 0.1.5 – 2020-06-05
### Added

### Changed
- remove 'read contacts' permission request, SSO works without it
[#45](https://gitlab.com/eneiluj/moneybuster/issues/45) @jwsp1
- use NC account server URL by default for new projects

### Fixed
- missing avatar updates in some cases
- CSV project import was missing some bills
- get rid of takisoft stuff to be able to update gradle

## 0.1.4 – 2020-05-19
### Fixed
- bug when editing/creating bills with decimal amount
[#53](https://gitlab.com/eneiluj/moneybuster/issues/53) @helfio

## 0.1.2 – 2020-05-09
### Added
- display real avatars for members linked with NC users
- new payment mode: online service
[#49](https://gitlab.com/eneiluj/moneybuster/issues/49) @jwsp1
- now able to center settlement on one member
[#28](https://gitlab.com/eneiluj/moneybuster/issues/28) @patxiku

### Changed
- remove time display in bill list items
[#50](https://gitlab.com/eneiluj/moneybuster/issues/50) @jwsp1
- better notification management with one main channel and one for each project
[#51](https://gitlab.com/eneiluj/moneybuster/issues/51) @jwsp1

### Fixed
- avoid scientific notation when sending/displaying amount
[#48](https://gitlab.com/eneiluj/moneybuster/issues/48) @uniqdom

## 0.1.1 – 2020-04-12
### Added
- dialog to remove project when sync has failed
[#44](https://gitlab.com/eneiluj/moneybuster/issues/44) @jwsp1

### Changed
- remove hardcoded categories for Cospend projects

### Fixed
- many stats filter problems

## 0.1.0 – 2020-03-26
### Changed
- lots of design improvement, specially in sidebar
[#41](https://gitlab.com/eneiluj/moneybuster/issues/41) @jwsp1

### Fixed
- always update last sync timestamp

## 0.0.19 – 2020-03-19
### Added
- support for time in bills (Cospend and local projects)
- ability to create projects with configured Nextcloud account authentication (Cospend >= 3.4.0)

## 0.0.18 – 2020-03-10
### Added
- optional periodical sync service with otpional notifications
- bank transfer payment mode
- ability to export projects to CSV files
- ability to import CSV files to local project

### Changed
- local projects can now use category and payment mode
- min Android version is now 5 (API 21, Lollipop)

### Fixed
- balance check when adding member to sidebar
- gplay complaints
[#32](https://gitlab.com/eneiluj/moneybuster/issues/32) @AndyScherzinger
[#33](https://gitlab.com/eneiluj/moneybuster/issues/33) @AndyScherzinger

## 0.0.17 – 2020-01-23
### Added
- custom currencies support
- select dialog to choose what to do when no project/account
- get NC color if account configured, optionnaly use it as main app color

### Changed
- disabled avatar
- updated screenshots (english/french)
- default app color is nextcloudish blue
- updated AUTHORS

### Fixed
- coherence between Cospend and MoneyBuster behaviour regarding disabled members

## 0.0.16 – 2020-01-15
### Added
- now able to access projects with Nextcloud credentials
- automatically add projects from Nextcloud account
- 'reimbursement' bill category
- show custom categories (Cospend >= 0.3.2)
- member color edition
[#18](https://gitlab.com/eneiluj/moneybuster/issues/18) @nicoe

### Changed
- new optional way of syncing with Cospend, just get what's newer than last sync
- show category/payment mode icons in stat filters and bill edition form
[Cospend#58](https://gitlab.com/eneiluj/cospend-nc/issues/58) @archit3kt
- launcher icons
- show member avatars in sidebar

### Fixed
- old date dialog (on some Android versions) closing on click
[#29](https://gitlab.com/eneiluj/moneybuster/issues/29) @almereyda
- write avatar letter in black if color is too bright

## 0.0.15 – 2019-11-03
### Added
- now able to change local password
- use member colors from Cospend if possible
- search by name with prefixes (+payer -ower @payer-OR-ower)
[#22](https://gitlab.com/eneiluj/moneybuster/issues/22) @patxiku
- help dialog explaining how to search

### Changed
- improve new project URL check, add https:// prefix if absent
[#51](https://gitlab.com/eneiluj/cospend-nc/issues/51) @doronbehar
- red background for invalid fields in new project form
[#51](https://gitlab.com/eneiluj/cospend-nc/issues/51) @eneiluj
- instantly add project after scanning/browsing valid link
[#51](https://gitlab.com/eneiluj/cospend-nc/issues/51) @eneiluj
- give focus to password field when scanning/browsing link with no password
[#51](https://gitlab.com/eneiluj/cospend-nc/issues/51) @eneiluj
- select member in sidebar: show bill involving this member as payer OR ower
[#22](https://gitlab.com/eneiluj/moneybuster/issues/22) @patxiku

### Fixed
- avoid having server URL overriden by default URL after scanning a QRCode
- fix link browsing project type choice
- balance color bug with number ending with 0.00
[#22](https://gitlab.com/eneiluj/moneybuster/issues/22) @patxiku

## 0.0.14 – 2019-10-19
### Added
- search by payer name
[#22](https://gitlab.com/eneiluj/moneybuster/issues/22) @patxiku
- lots of german and italian translations

### Fixed
- sync new cospend bill, forgot to put paymentmode and category parameters

## 0.0.13 – 2019-10-13
### Added
- new categories
- filters in project statistics
- button to select all bills in selection mode
[#24](https://gitlab.com/eneiluj/moneybuster/issues/24) @patxiku
[#23](https://gitlab.com/eneiluj/moneybuster/issues/23) @patxiku
- now search by ower name
[#22](https://gitlab.com/eneiluj/moneybuster/issues/22) @patxiku
- privacy policy
- automatic settlement bills creation
[#27](https://gitlab.com/eneiluj/moneybuster/issues/27) @leoossa

### Fixed
- project name when sharing stats and settlement for local project
- hide 'share project' button for local projects
[#25](https://gitlab.com/eneiluj/moneybuster/issues/25) @PEPERSO
- bill selection
- category and payment mode were not set for new bills
- bump SSO lib to 0.4.1, now working with Nextcloud dev accounts

## 0.0.12 – 2019-09-14
### Added
- qr scanner to import projects
[#20](https://gitlab.com/eneiluj/moneybuster/issues/20) @denics
- web link in sharing dialog
[cospend#42](https://gitlab.com/eneiluj/cospend-nc/issues/42) @jreybert
- show total payed in stats dialog
- able to catch BROWSE intent for ihatemoney.org URLs
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh
- offline mode to only sync when user asks
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh
- Nextcloud account settings to be able to import Cospend projects from there
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh
[#20](https://gitlab.com/eneiluj/moneybuster/issues/20) @denics
- tooltip for FAB
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh
- welcome dialogs (one first and one for each release)
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eneiluj
- show bill payment mode and category for cospend projects

### Changed
- settlement algorithm: use the same as Cospend and IHateMoney (from https://framagit.org/almet/debts)
[#15](https://gitlab.com/eneiluj/moneybuster/issues/15) @nicocool84
- improve CI script, now can sign release apk
- tap on project name when there is none => create one
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh
- make sync success toast more discreet
- "new project" screen is now a more intuitive form
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh
- able to scan QRCode from "new project" screen
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh
- move many buttons, make add-project/share/settle/stats more visible
[#21](https://gitlab.com/eneiluj/moneybuster/issues/21) @eMerzh

### Fixed
- hide useless buttons when there is no project
- owner avatar display in bill edition for small screens

## 0.0.11 – 2019-07-12
### Added
- ability to manipulate recurring bills (cospend projects only)
[!6](https://gitlab.com/eneiluj/moneybuster/merge_requests/6) @AndyScherzinger
- avatar in bill edition
[!10](https://gitlab.com/eneiluj/moneybuster/merge_requests/10) @AndyScherzinger

### Changed
- change project type icon in new project form
[!7](https://gitlab.com/eneiluj/moneybuster/merge_requests/7) @AndyScherzinger
- respect locale when formatting dates
[!9](https://gitlab.com/eneiluj/moneybuster/merge_requests/9) @AndyScherzinger and @eneiluj
- update cert4android and dependencies

### Fixed
- trim project URL
[!8](https://gitlab.com/eneiluj/moneybuster/merge_requests/8) @AndyScherzinger
- crash when validating certificate

## 0.0.10 – 2019-07-04
### Added

### Changed
- settlement: sort members like IHateMoney does to get same results
[#15](https://gitlab.com/eneiluj/moneybuster/issues/15) @nicocool84
- show/hide all/none buttons dynamically
- UI improvements in sidebar
- show bill edition validate button only if something changed and values are valid

### Fixed
- negative number rounding
[#14](https://gitlab.com/eneiluj/moneybuster/issues/14) @jeisonp
- apply new theme/color immediately
- disable ability to refresh list layout when no network connectivity
- bill list item layout are now displayed correctly with big font size
[#17](https://gitlab.com/eneiluj/moneybuster/issues/17) @Aldarone

## 0.0.9 – 2019-05-08
### Added

### Changed
- improve bill edition form design
- improve bill list items
- remove bill info dialog
- improve settlement/stats dialogs design
- improve settings theme icon

### Fixed
- use our own icons instead of system ones (which can change)
- prevent fields autofill
- don't show sync icon for local projects
[#13](https://gitlab.com/eneiluj/moneybuster/issues/13) @Nuntius0
- remove duplicated ways to validate/delete/cancel in bill edition

## 0.0.8 – 2019-05-04
### Added
- FAB button to save bill
[#10](https://gitlab.com/eneiluj/moneybuster/issues/10) @Nuntius0

### Changed
- improve keyboard/selection behaviour in forms/preferences screens
[#11](https://gitlab.com/eneiluj/moneybuster/issues/11) @Nuntius0

### Fixed
- apply color change after backpressed from preferences

## 0.0.7 – 2019-04-24
### Added
- ability to select all/none bill owers in bill edition
- remember project last payer and use it for new bills
[#9](https://gitlab.com/eneiluj/moneybuster/issues/9) @zonque

### Changed
- dark theme: real black
- click on current project name label => select project dialog
- bill edition form is now a...form
[#9](https://gitlab.com/eneiluj/moneybuster/issues/9) @zonque
- dev flavout icon color: orange

### Fixed
- accept comas for member weight and bill amount
[#5](https://gitlab.com/eneiluj/moneybuster/issues/5) @polkillas1
- bill list background
- black theme issues with black as app main color
- reduce top sidebar part height
[#7](https://gitlab.com/eneiluj/moneybuster/issues/7) @Obigre
- project sync on startup

## 0.0.6 – 2019-03-09
### Added
- translations

### Changed
- use different app flavours for F-Droid and CI builds to be able to install both versions in parallel

## 0.0.5 – 2019-03-08
### Added
- able to generate QRCode to share a project to another phone with MoneyBuster installed
- able to catch VIEW intent when a "moneybuster" link is visited to import a project

### Changed
- bump gradle plugin version
- allow negative bill amount values
[#4](https://gitlab.com/eneiluj/moneybuster/issues/4) @Michael-Hofer

### Fixed
- declare server address field as textURI
[#3](https://gitlab.com/eneiluj/moneybuster/issues/3) @Salamandar

## 0.0.4 – 2019-03-01
### Added
- new option to set app main color
- now able to receive VIEW attempts for URI like cospend://my.nextcloud.org/projectid

### Changed
- get rid of butterknife
- bump to androidx
- update cert4android
- CI : keep debug apk only

## 0.0.3 – 2019-02-15
### Added

### Changed
- moved floating buttons
- better default project URL, type-specific
- payback -> cospend

### Fixed
- use compat alert dialog
- huge bug when searching default URL for new project

## 0.0.2 – 2019-02-08
### Added
- able to sync with Nextcloud Payback
- source svg icon

### Changed
- a few icons

### Fixed
- stop animation when create project fails
- sort member correctly
- sort members for settlement to get same results than Payback

## 0.0.1 – 2019-01-23
### Added
- new app !

### Fixed
- the world
