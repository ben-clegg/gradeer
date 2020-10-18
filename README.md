# Gradeer

Gradeer is a modular assessment tool with support for both automated and manual assessment. 

## Installation

### Prerequisites

#### JDK

Gradeer is compatible with JDK 1.8. We have also tested it with JDK 11; later versions should be compatible.

#### Maven

[Apache Maven](https://maven.apache.org/) is required to build and test Gradeer. We can confirm that version 3.6.0 can be used for this.  
Ant can be installed by `sudo apt install maven` on Ubuntu.

#### Apache Ant

Gradeer uses [Apache Ant](https://ant.apache.org/) to compile and execute Java solutions. We have tested Gradeer with Ant 1.10.5.
Ant can be installed by `sudo apt install ant` on Ubuntu.

Ensure that you set the `ANT_EXECUTABLE` environment variable to the location of your installed ant binary, e.g. `/usr/share/ant/bin/ant`.

#### PMD

In order to use PMD checks, [PMD](https://github.com/pmd/pmd/releases/tag/pmd_releases%2F6.24.0) must be installed. We officially support version 6.24.0, but newer versions may also work.

You must also set the environment variable `GRADEER_PMD_LOCATION` to point to PMD's directory (which contains the `bin` and `lib` directories), e.g. `/home/username/Software/pmd-bin-6.24.0/`

### Building

Clone and enter the repository.
```shell script
git clone https://github.com/ben-clegg/gradeer && cd gradeer
```

Run the build process with Maven. This will also run some unit tests; if any tests fail, check that the prerequisites are correctly installed and configured, and repeat the process.
```shell script
mvn clean package
```

Tests can be run alone with the following command:
```shell script
mvn test
```

## Usage

Gradeer is executed by:
```shell script
java -jar <path to gradeer jar with dependencies> <location of configuration JSON file>
```

This repository provides an example environment, called "liftPackaged." While standalone Gradeer binaries are available, we recommend building the tool from source (as above) so a copy of this enviroment is available.
This example environment can be executed from the root of the repository by:

```shell script
java -jar target/gradeer-0.98b-jar-with-dependencies.jar src/test/resources/testEnvironments/liftPackaged/gconfig-liftpackaged.json
```

Gradeer performs the following in the execution environment:
- Load checks, model solution, and "students'" solutions.
- Compile model and "students'" solutions.
- Run checks on model solution to identify and remove faulty checks (TestSuiteCheck_TestDummyB and TestSuiteCheck_TestDummyC are faulty). These are reported in the environment's `output` directory.
- Run remaining checks on "students'" solutions.
- Store grades and feedback in the environment's `output` directory.

### build.xml
The working directory where Gradeer is executed from (the repository's root directory in the above example) must contain build.xml.
This file directs Ant in compiling and executing solutions and tests.
This file can be modified to support different test configurations (e.g. JDK versions).

## Configuration

### Structure

When making a grading environment for Gradeer, it should be contained within its own directory. This should be structured similarly to the example environment, present in `src/test/resources/testEnvironments/liftPackaged`.

### Main Config File

The main configuration file is a JSON file, which defines Gradeer's execution parameters. 
Any path-based parameters can be defined in an absolute manner, or (preferably) relatively to this JSON file.
The following is an explanation of the example environment's configuration, `gconfig-liftpackaged.json`:
```
{
  // Location of students' solutions
  "studentSolutionsDirPath": "studentSolutions",
  // Location of known correct model solution(s)
  "modelSolutionsDirPath": "modelSolutions",
  // Location of test suites
  "testsDirPath": "testSuites",
  // Locations of check definition JSON files. Files can contain different types of checks, but it may be beneficial to group them.
  "checkJSONs": ["unittestChecks.json", "checkstyleChecks.json", "pmdChecks.json"],
  // Time budget for each test suite to execute. Tests are assumed to fail otherwise.
  "perTestSuiteTimeout": 120,
  // Location of checkstyle rule definitions
  "checkstyleXml": "checkstyle.xml"
}
```

### Checks

Checks can be defined across multiple JSON files, each containing an array of one or more check definitions.
Each check requires a name and a check type. Other parameters can also be defined, such as weights and feedback.
For example, the following would define a check for each currently implemented type of check:
```json
[
  {
    "type": "CheckstyleCheck",
    "name": "MethodName",
    "feedbackValues": [
      {"score": 0.0, "feedback":  "One or more method names not in camelCase."},
      {"score": 1.0, "feedback":  "Method names use correct camelCase."}
    ]
  },
  {
    "type": "PMDCheck",
    "name": "EmptyIfStmt",
    "weight": 0.5,
    "feedbackValues": [
      {"score": 1.0, "feedback": ""},
      {"score": 0.0, "feedback": "Your code contains empty if statements. This should be avoided as it reduces readability."}
    ],
    "maxViolations": 2
  },
  {
    "type": "TestSuiteCheck",
    "name": "TestLiftA",
    "weight": 8.0,
    "feedbackValues": [
      {"score": 0.0, "feedback":  "TestLiftA Incorrect"},
      {"score": 1.0, "feedback":  "TestLiftA Correct"}
    ]
  },
  {
    "type": "ManualCheck",
    "name": "Error Reporting",
    "prompt": "Quality of error reporting?",
    "weight": 4.0,
    "maxRange": 10,
    "feedbackValues": [
      {"score": 0.9, "feedback": "Excellent error reporting"},
      {"score": 0.7, "feedback": "Good error reporting"},
      {"score": 0.5, "feedback": "Average error reporting"},
      {"score": 0.0, "feedback": "Poor error reporting"}
    ]
  }
]
```

When executed, each check calculates a normalized score between 0 (complete failure) and 1 (perfect pass).
These scores are used to determine which band of feedback to provide, as defined by `"feedbackValues"`.

When calculating the final grade of a solution, the score of each check is multiplied by its weight, then the sum of these values is divided by the sum of checks' weights. 

## Acknowledgements 

Development of this tool was funded in part by The University of Sheffield.