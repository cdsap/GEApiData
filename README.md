## ge-api-data
Intermediate layer for Gradle Enterprise API:
* Paginates `/api/builds` allowing to request more than 1000 builds
* Consolidate attributes and cache performance endpoints
* Supports advanced query language requests for GE 2023.3

> This is not an official Gradle Enterprise library.
> This is a free interpretation of a layer that allows massive requests and consolidates builds information to be
> used in tooling/reports to help you understand better the state of the build in your projects

## Dependency
```
dependencies {
  implementation("io.github.cdsap:geapi-data:0.2.2")
}
```

## Simple Usage

### Define Repository
```kotlin
 val repository = GradleRepositoryRequest(GEClient(apiKey, url))
```
We need to specify the Gradle Enterprise url and the API token
### Retrieve Build Scans with Attributes
```kotlin
val getBuildScans = GetBuildsWithAttributesRequest(repository).get()
getBuildScans.forEach {
    ...
}
```
If you are using Gradle Enterprise 2023.3 you can use the variant:
```kotlin
val getBuildScans = GetBuildsFromQueryWithAttributesRequest(repository).get()
getBuildScans.forEach {
    ...
}
```

Returns a list of `ScanWithAttributes`:

| Property            | Description              |
|---------------------|--------------------------|
| id                  | Build Scan id            |
| projectName         | Project name             |
| requestedTasksGoals | Requested tasks or goals |
| tags                | List of tags             |
| hasFailed           | Build failed             |
| environment         | Environment user         |
| buildDuration       | Duration in ms           |
| buildTool           | Build Tool               |
| buildStartTime      | Build start time         |
| values              | Custom values            |


### Retrieve Build Scans with Cache Performance
```kotlin
val buildScans = GetBuildsWithAttributesRequest(repository).get()
val filter = Filter(requestedTask = "assembleDebug")
val builds = GetBuildsWithCachePerformanceRequest(repository).get(buildScans, filter)
build.forEach {
    ...
}
```
Returns a list of `Build`:

| Property                | Description               |
|-------------------------|---------------------------|
| builtTool               | Build Tool                |
| taskExecution           | List of tasks             |
| tags                    | List of tags              |
| requestedTask           | Requested Tasks           |
| id                      | Build Scan id             |
| buildDuration           | Build Duration            |
| avoidanceSavingsSummary | Avoidance Savings Summary |
| buildStartTime          | Build start time          |
| projectName             | Project name              |
| goalExecution           | List of goals             |
| values                  | Custom values             |

Both Request functions are `suspend`.

## Filter
Once we have seen the simple usage, we need to introduce the `Filter` entity. It's optional for `GetBuildsWithAttributesRequest`
and required for `GetBuildsWithCachePerformanceRequest`.
`Filter` is critical to reduce the overhead of the process. Cache requests are expensive, and we want to limit the requests based
in parameters like `project`,`tags` or `requestedTask`.

| Property                    | Description                                                                   | Default        |
|-----------------------------|-------------------------------------------------------------------------------|----------------|
| maxBuilds                   | Max builds requested                                                          | 50             |
| sinceBuildId                | Build Id where we'll start the request. If null, we start from the last build | null           |
| project                     | Project name                                                                  | null           |
| includeFailedBuilds         | Include failed builds in the request                                          | false          |
| requestedTask               | Requested Task                                                                | null           |
| tags                        | List of tags                                                                  | emptyList()    |
| user                        | User Build Scan                                                               | null           |
| exclusiveTags               | List of tags must match with the available tags in the Build Scan             | false          |
| concurrentCalls             | Concurrent calls for the Attributes request                                   | 10             |
| concurrentCallsConservative | Concurrent calls for Cache request                                            | 5              |
| clientType                  | Type of client, API or CLI                                                    | ClientType.API |

## Real Examples

### Last 5000 `ScanAttributes` filtering by project
Requires GE 2023.3
```
val repository = GradleRepositoryRequest(GEClient(apiKey, url))
val getBuildScans = GetBuildsFromQueryWithAttributesRequest(repository).get(Filter(maxBuilds = 5000)
```


### Last 5000 `ScanAttributes`
```
val repository = GradleRepositoryRequest(GEClient(apiKey, url))
val getBuildScans = GetBuildsWithAttributesRequest(repository).get(Filter(maxBuilds = 5000)
```

### Last 5000 `ScanAttributes` filtering by project
```
val repository = GradleRepositoryRequest(GEClient(apiKey, url))
val getBuildScans = GetBuildsWithAttributesRequest(repository).get(Filter(maxBuilds = 5000, project = "nowinandroid")
```

### Last 5000 `ScanAttributes` filtering by project including CI tag
```
val repository = GradleRepositoryRequest(GEClient(apiKey, url))
val getBuildScans = GetBuildsWithAttributesRequest(repository).get(Filter(maxBuilds = 5000, project = "nowinandroid", tags = listOf("CI","main), exclusiveTags = true)
```

### Last 5000 `ScanAttributes` filtering by project including CI and main tag being exclusive
```
val repository = GradleRepositoryRequest(GEClient(apiKey, url))
val getBuildScans = GetBuildsWithAttributesRequest(repository).get(Filter(maxBuilds = 5000, project = "nowinandroid", tags = listOf("CI"))
```
###  5000 `ScanAttributes` since BuildId x
```
val repository = GradleRepositoryRequest(GEClient(apiKey, url))
val getBuildScans = GetBuildsWithAttributesRequest(repository).get(Filter(maxBuilds = 5000, sinceBuildId = "x"))
```

###  Get Builds cache Performance from the last 5000 builds in project
```
val repository = GradleRepositoryRequest(GEClient(apiKey, url))
val filter = Filter(maxBuilds = 5000, project = "nowinandroid")
val scans = GetBuildsWithAttributesRequest(repository).get(filter)
val builds = GetBuildsWithCachePerformanceRequest(repository).get(scans, filter)
```


## Network Client Configuration
We can extend the `GEClient` configuration using `ClientConf`. This entity allows to change the default configuration
for retries and exponential backoff configuration depending on the scenario/server load.

| Property                    | Default |
|-----------------------------|---------|
| maxRetries                  | 200     |
| exponentialBase             | 2.0     |
| exponentialMaxDelay         | 60000   |

## Client Types
We define two different client types:
* API
* CLI

CLI provides a console feedback ideal for building CLI's(see next section):
```
Calculating Build Scans to retrieve
100% ################################################## -
Date first Build scan processed: 23/08/2023 15:35:36
Date last Build scan processed: 21/08/2023 23:53:42
Getting 50000 Build Scans Attributes
```

## Existing tooling using `ge-api-data`

### ProjectReport
https://github.com/cdsap/ProjectReport

CLI providing reports by user/task/project in a Gradle Enterprise instance
Using `GetBuildsWithAttributesRequest` and `GetBuildsFromQueryWithAttributesRequest`

### TaskReport
https://github.com/cdsap/TaskReport

This CLI provides general reports for duration/fingerprinting by type and path. Additionally, it offers single reports for
specific tasks.
Uses `GetBuildsWithAttributesRequest`, `GetBuildsFromQueryWithAttributesRequest` and `GetBuildsWithCachePerformanceRequest`

### CompareGEBuilds (WIP)
https://github.com/cdsap/CompareGEBuilds

This CLI compares two different build sequences and generates reports with durations based on metrics by variant.
Ideal for Performance Regressions.
