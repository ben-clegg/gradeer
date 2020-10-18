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

Ensure that you set the `ANT_HOME` environment variable to the location of your installed ant binary, e.g. `/usr/share/ant/bin/ant`.

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
java -jar 
```



## Acknowledgements 

Development of this tool was funded in part by The University of Sheffield.