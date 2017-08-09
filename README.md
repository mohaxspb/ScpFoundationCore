[ ![Download](https://api.bintray.com/packages/mohaxspb/scp-core/scp-core/images/download.svg) ](https://bintray.com/mohaxspb/scp-core/scp-core/_latestVersion)

# Scp Core

This module used by [ScpFoundationRu](https://github.com/mohaxspb/ScpFoundationRu) project as a core, where most logic is

# How to use

It is published in `jcenter` so you can just use this in `build.gradle(module app)`

    //use transitive=true to get all lib dependencies
    compile ('ru.kuchanov.scp:core:1.0.15@aar'){ transitive=true }
    
Do not forget to check latest version of module in [releases](https://github.com/mohaxspb/ScpCore/releases) list in this repo.
    
# How to update

Use git-flow system for making changes and adding features
To update lib in maven call
 
     gradlew install
     
and then 

    gradlew bintrayUpload
    
Of course, you need to add auth info to `local.properties` file in project to be able to upload new version

```# for publishing to jcenter

bintray.user={USER_NAME_IN_https://bintray.com}
bintray.apikey={API_KEY_FROM_https://bintray.com}
bintray.gpg.password={PASS_FOR_KEY_THAT_WAS_CREATED_BY_gpg}`