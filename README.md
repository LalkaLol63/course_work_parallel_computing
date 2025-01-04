First of all, you need to have Java installed. If you don't have it yet, you can download the latest version here: [Java](https://www.oracle.com/java/technologies/downloads/).

Also if you want to build the project yourself, you need an installed [Apache Maven](https://maven.apache.org/install.html).

## How to build the Project

1. Clone the repository to your local machine:
```bash
git clone https://github.com/LalkaLol63/course_work_parallel_computing
```   
2. Navigate to the сloned repository:
```bash
cd course_work_parallel_computing
```
3. Compile and package the project using Maven:
```bash
mvn package
```
This command will download the required dependencies, compile the source code, and package it into JAR files. The generated JAR files will be available in the target directory.

## How to run the project

Build the project yourself using the previous instructions or download one of the jar files from the [releases](https://github.com/LalkaLol63/course_work_parallel_computing/releases): client or server.

Then you need to open a terminal in the directory where the jar file is located.

To start the server, enter: 
```bash
java -jar lohvin-server.jar
```
To start the cleint, enter:
```bash
java -jar lohvin-client.jar
```
Then follow the instructions of the program.
