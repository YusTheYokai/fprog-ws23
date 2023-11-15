# War and Peace
Classify whether a chapter is about war or peace.

## Prerequisites

- Java 17 or above (`sudo apt intall openjdk-17-jdk`)
- Maven (`sudo apt install maven`)

## Compile

- `mvn clean package`

Include the `-DskipTests` flag to not run tests.

## Run
### Everywhere, if jar is present

- `java -jar war-and-peace-1.0.jar war_and_peace.txt`

### In project context after compiling

- `java -jar target/war-and-peace-1.0.jar src/main/resources/war_and_peace.txt`

## Test

- `mvn test`
