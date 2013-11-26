# Qucosa Fedora Commons Repository Migration

A utility program to migrate document data and related security information over to a Fedora Commons Repository.

## Description

1. Extracts Qucosa information and generates FOXML files
2. Connects to Fedora Commons to ingest these files
3. Qucosa IDs that have been downloaded or ingested will not be ingested any more

## Building

The qucosa-fcrepo-migration program is a Maven project and as such can be build with the Maven package command:
```
$ mvn package
```

This will generate a runnable JAR file `target/qucosa-fcrepo-migration-<VERSION>.jar` for execution on the command line.

## Usage

You will have to provide a suitable JDBC driver via the Java Classpath in order to connect to databases.

a. Either run the packed Jar-File with the `java -jar` command
b. Or just run `mvn exec:java`

1. Write properties file to define Qucosa DB connection, Qucosa WebAPI
2. Define workspace directory
3. Add Fedora Commons Connection and Credentials
4. Select tenants to migrate

## Licence

The program is licenced under [GPLv3](http://www.gnu.org/licenses/gpl.html). See the COPYING file for details.

