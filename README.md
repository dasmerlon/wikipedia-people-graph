# Wikipedia People Graph

This project is the result of a practical training of our university (Universität Hamburg). \
The goal of the project is to create a tool to interactively explore the relationships between People listed on Wikipedia.

Everything is based on Wikimedia's regular database dumps of the English Wikipedia, which can be found [here](https://dumps.wikimedia.org/enwiki/).

Click [here](http://basecamp-demos.informatik.uni-hamburg.de:8080/peoplegraph/) to view a demo of the web application.

![Wikipedia People Graph](https://raw.githubusercontent.com/dasmerlon/images/master/wikipedia-people-graph.png "Wep Application")

# Project Structure

The whole project is divided into two subprojects.

1. Hadoop Jobs, which extract all necessary and interesting information from the Wikimedia dumps. 
2. A web server, for interactive exploration. 

## Hadoop Jobs

There are four different Hadoop jobs, which are used to incrementally parse and process the original data. \
Most of the database dumps can be ignored, since we're only interested in a small subset of that data.

### PersonArticleExtractor

This job is located at `./mapreduce-jobs/PersonArticleExtractor`. \
It's purpose is to filter all Wikipedia articles, that aren't related to any real-world persons.

The result of this job is a significantly reduced database dump.
For reference, the original Wikimedia dump has nearly 6 million entries, the new dump has about 1.5 million entries.

**Attention:** \
All following jobs are always executed on this new dump!

### PersonData

This job is located at `./mapreduce-jobs/PersonData`. \
`PersonData` is responsible for collecting all information about a person from their article's metadata.

Wikimedia has various notations for metadata, which allows us to extract this data with a semi-sophisticated parser.

The result set is a collection of information for all people on Wikipedia.
It's a simple CSV file with some special formatting, and a pre-defined field order.

For more information, please look into the PersonData project's code.

### TitleExtractor and Relationships

These jobs are strongly connected. Their locations are `./mapreduce-jobs/TitleExtractor` and `./mapreduce-jobs/Relationships`.

At first, the TitleExtractor is run. This job simply returns a file with all titles of all Articles. \
The idea behind this, is that the title of an article can be seen as the primary key of an article.
What's nice about this, is that every link on Wikipedia uses this exact title/key to link to other articles.

The next step is the `Relationship` job.
This job takes the output of `TitleExtractor`, walks to the article and checks for each link whether the link points to a person or not. \
If a link points to that person, a new relationship is created.

## Web Server

The website provides the user interface of this application. We use the spring framework to build the web application (spring boot application).\
The structure and logic of this website is defined in the HTML-, CSS-, JavaScript- and Java-code. 

The `index.html` file defines the inputs, buttons, infobox and containers for both visualisations. The `main.js` file contains the logic to build the timeline and network-graph in the corresponding HTML-containers. We use the AnyChart library to build the visualisations. To receive the data, `main.js` needs to display in it's functions `buildTimeline` and `buildGraph`, the functions call one of the Controller-classes (Java) `PersonController`, `RelatedPersonController` or `GraphController`. The controller-classes get the data from a MySQL-Database using the `MySQLconnect`-class. The `GraphController` additionally converts the data into the needed structure and gets additional data (for the second layer of relationships). 

The `MySQLconnect`-class creates a connection to the MySQL-database, which is defined in the `credentials.txt`-file. The `MySQLconnect`-class moreover uses the `ResultSetConverter`-class to convert the returned SQL-ResultSet into JSON-Format.

# Installation

## Hadoop Jobs

All Hadoop jobs are built with Maven. \
It's highly recommended building the project via the IntelliJ IDEA editor, since this is the way this project has been developed!

1. Open the project.
2. Run maven compilation steps.
3. Copy the jar file to your Hadoop cluster node where it can be executed.

This is the same for all Hadoop projects. \
There's also a `run_test.sh` file, which can be used for reference on how to execute this. \
More on this in the section `Deploy and Execution`.

## Web Server
Before deployment, you have to define your credentials for the MySQL-database.
To be able to deploy the website on a web server, you need to package the project 
into a file-format your webserver expects. We use an Apache Tomcat Server and 
packaged the project into a WAR-file.

1. Copy the `credentials.txt.template` from the folder `backend-server-master`, adjust your login data and move it to `backend-server-master/src/main/resources/credentials.txt`.
2. Run and build the project with maven to get the WAR-file.

# Deployment and Execution

## Hadoop Jobs Deployment

In the following, we expect that you have:

1. Build all hadoop projects.
2. Uploaded all compiled `*.jar` files to the home (`~`) under the respective name of the project, e.g. `PersonArticle.jar`.

## Execution

### PersonArticleExtractor

1. Download the Wikimedia dump. It is important to use the English dump since other dumps may have different notations. 
   We work with the `enwiki-latest-pages-meta-current.xml` dump.
   Upload it onto your hadoop cluster to a location of your choice.
   From now on, this location will be referred to as `$WIKI_DUMP`.
2. Create a hadoop directory `$OUTPUT`, which will be used to store the results.
3. Run the `PersonArticleExtractor` job on that dump.
    ```
    hadoop jar ~/PersonArticle.jar $WIKI_DUMP $OUTPUT/PersonArticle loglevel debug
    ```
4. Next, you have to combine all results of that job into a single XML file named `person_article.xml` and put it onto the hadoop cluster.
    ```
    hadoop fs -getmerge $OUTPUT/PersonArticle person_article.xml
    hadoop fs -copyFromLocal person_article.xml $OUTPUT/person_article.xml
    ```

### PersonData

1. Run the `PersonData` job on the `person_article.xml`.
    ```
    hadoop jar ~/PersonData.jar $OUTPUT/person_article.xml $OUTPUT/PersonData loglevel debug
    ```
2. Combine all results of that job into a single CSV file named `person_data.csv` and put it onto the hadoop cluster.
    ```
    hadoop fs -getmerge $OUTPUT/PersonArticles person_data.csv
    hadoop fs -copyFromLocal person_data.csv $OUTPUT/person_data.csv
    ```

### TitleExtractor

1. Run the `TitleExtractor` job on the `person_article.xml`.
    ```
    hadoop jar ~/TitleExtractor.jar $OUTPUT/person_article.xml $OUTPUT/TitleExtractor loglevel debug
    ```
2. Combine all results of that job into a single TXT file named `titles.txt` and put it onto the hadoop cluster.
    ```
    hadoop fs -getmerge $OUTPUT/TitleExtractor titles.txt
    hadoop fs -copyFromLocal titles.txt $OUTPUT/titles.txt
    ```

### Relationships

To use the `titles.txt` of the TitleExtractor, you have to adjust the Hadoop cacheFile path in `./mapreduce-jobs/Relationships/src/main/java/Relationships.java`.

Change the line 43, so it fits your local environment:
```
job.addCacheFile(new Path("hdfs:///$OUTPUT/titles.txt").toUri());
```

Then build the file and upload it as described in the `Deployment` section.

1. Run the `Relationsips` job on the `person_article.xml`.
    ```
    hadoop jar ~/Relationsips.jar $OUTPUT/person_article.xml $OUTPUT/Relationships loglevel debug
    ```
2. Combine all results of that job into a single CSV file named `relationship.csv` and put it onto the hadoop cluster.
    ```
    hadoop fs -getmerge $OUTPUT/Relationships relationship.csv
    hadoop fs -copyFromLocal relationship.csv $OUTPUT/relationship.csv
    ```

## Database

### Setup

1. Setup up a MySQL Instance.
2. Create a new database. 
3. Create the schema by simply running:
   ```
   cat schema.sql | mysql $YOUR_DB_NAME
   ```

### Data Import

Now that the schema is created, you can go ahead and import the previously created data into your database.

First, you have to import the PersonData. This is done by running the following command on your MySQL database:
```
LOAD DATA LOCAL INFILE '$OUTPUT/person_data.csv' 
INTO TABLE PersonData
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '>>>>' 
LINES TERMINATED BY '\n';
```

Then, you have to import the Relationships. This is done by running the following command on your MySQL database:
```
LOAD DATA LOCAL INFILE '$OUTPUT/relationship.csv' 
INTO TABLE Relationships
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '>>>>' 
LINES TERMINATED BY '\n';
```

The database has been populated and is ready for use.

## Web Server

To deploy the website on a web server, you need to upload your packaged file (in our deployment a WAR-file) to your web server and instruct your web server to deploy the website. We use an Apache Tomcat Server to host the website. 
