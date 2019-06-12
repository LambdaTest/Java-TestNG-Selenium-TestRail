### Environment Setup

1. Global Dependencies
    * Install [Maven](https://maven.apache.org/install.html)
    * Or Install Maven with [Homebrew](http://brew.sh/) (Easier)
    ```
    $ install maven
    ```
    ```
2. Project Dependencies
    * checkout the repository
    * Check that packages are available
    ```
    $ cd Java-TestNG-Selenium-TestRail



    ```
    * You may also want to run the command below to check for outdated dependencies. Please be sure to verify and review updates before editing your pom.xml file as they may not be compatible with your code.
    ```
    $ mvn versions:display-dependency-updates
    ```
    
### Running Tests

```
To run single test
    $ mvn test -P single

Once Test gets run, it logs result in TestRail as output is here:
Jun 12, 2019 2:34:50 PM org.openqa.selenium.remote.ProtocolHandshake createSession
INFO: Detected dialect: OSS
here
add_result_for_case/215/3351
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 35.235 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  40.209 s
[INFO] Finished at: 2019-06-12T14:35:10+05:30
[INFO] ------------------------------------------------------------------------
```


