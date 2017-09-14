# Android MVP Core

MVP Core is an Android Library which will help you to set up a MVP architecture easily. It also provides :
- task continuation through orientation change, application paused with result notification to your view as soon as the view is available
- task cancellation

This version supports RxJava 2.

# Download

Available on JCenter and MavenCentral. In your module :
```groovy
compile 'com.github.mandriana:mvp-core-rx2:1.0.4'
// To use base views library
compile 'com.github.mandriana:mvp-base-views-rx2:1.0.4'
// To use base views with dagger module library
compile 'com.github.mandriana:mvp-base-views-rx2-dagger:1.0.4'
```

# CHANGELOG

## 1.0.4

Upgraded dependencies versions.
Fixed a bug when retrieving a presenter from the cache when multiple views with presenters were created in a very short time.
Added logs and ability to activate/deactivate them.

## 1.0.2

startOnViewAttached now accepts an Action1 to provide the view as action parameter.
Upgraded dependencies versions.

## 1.0.1

Fixed the startOnViewAttached method so that the action is started instead of added to the queue if the view is already attached.

## 1.0.0

Project available

# Community

Looking for contributors, feel free to fork !

Tell me if you're using my library in your application, I'll share it in this README

# Dependencies

- mvp-core :
  - android support annotations
  - io.reactivex:rxandroid
  -io.reactivex:rxjava
- mvp-base-views :
  - android support appcompat-v7
- mvp-base-views-dagger
  - android support appcompat-v7
  - dagger 2
  
# Credits

Author: Michael Andrianarimanga

License
--------

    Copyright 2017 michaelandria, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
