# hpo2gforms


hpo2gforms is a Java command-line application that creates Google Apps code that can be used on the Google platform to create a Google Forms questionnaire.

### Requirements

hpo2forms requires Java version 21 or higher. Java JDKs can be downloaded from many places including [azul](https://www.azul.com/downloads/#zulu){:target="_blank"}.


### Build

hpo2forms is built with [maven](https://maven.apache.org/){:target="_blank"}. First use git to clone the code and then build it.

```
git clone https://github.com/P2GX/hpo2gforms.git
cd hpo2gforms
mvn clean package
```

This will create an executable called "hpo2forms.jar" in a new subdirectory called "target". This jar file can be moved to whatever location you desire or can be left where it is. Adjust the path as desired.


## Running hpo2jar

The commands can be seen by the following

```
$ java -jar target/hpo2gforms.jar 
Usage: hpo2gforms [-hV] [COMMAND]
HPO to Google Forms
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  download  Download HPO JSON file.
  gforms    Create Google Forms Code
```

Running the ``download``command will download the hp.json file needed for the gforms command.

```
$ java -jar target/hpo2gforms.jar download
```

Thuis command creates a new directory called ``data``and downloads the hp.json file to this.
When running the app, always be sure to download the latest hp.json file using this command.

## Target term

Now choose the target term -- the base term for creating the questionnare. For instance, to choose
[Abnormal nonverbal communicative behavior HP:0000758](https://hpo.jax.org/browse/term/HP:0000758){:target="_blank"}, enter the "-t" argument as 0000758.

```
$ java -jar target/hpo2gforms.jar gforms -t 0000758 [-l]
```

This will create a new file with the Google Apps code. 
See [Google Apps](gapps.md) for details on how to run this in the Google apps console.
The ``-l`` flag (for limit) causes the app to output only three questions and is intended to be used for testing.
