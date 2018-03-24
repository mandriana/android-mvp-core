## 1.0.10
onTerminate couldn't be used because notifications were observed. Instead use a doAfterNext and check isCompleted or isError.

## 1.0.9
Fixed the call of onTerminate action which was done after the original observable completion instead of the replay completion.
This caused the isCompleted notification never be retrieved if received while view was not available.

## 1.0.8
Fixed presenter destruction.
"cancelAll" is now final and protected to be called in presenter.

## 1.0.7 

Fixed pom publication in aar.
Added missing annotation Nullable, NonNull ... (for better kotlin support).

## 1.0.6 

Upgraded dependencies.

## 1.0.5 (Core only)

Upgraded dependencies versions.
Fixed tag in AbstractSubscriptionProxy.

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
