# Rest using Vert.x Sample - WIP

This is a sample using Vert.x with the basic CRUD operation and REST services, I used the following technologies:

* Java 8
* Vertx 3.6.0
* HSQLDB 2.3.4
* JUnit 4.12
* Logback 1.2.3
* Maven 3.6.0

### How to run?

* Compile using the `package` command:
    ```
    mvn clean package
    ```
    If the compilation is successful, then it should have generated a folder called `generated` inside of `src/main/`

* Run using the properties file configuration
    ```
    mvn clean package
    java -jar target/sample-rest-vertx-1.0-SNAPSHOT-fat.jar -conf src/main/resources/application-conf.json
    ```

### Rest endpoints

I used the postman as a client to test the endpoints, you can import the collection, the file is in:

```
/resources/endpoints/collection.json
```