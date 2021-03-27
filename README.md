# Wikipedia People Graph


Hi!

This project is the result of a practical training of our university (Universität Hamburg). \
The goal of the project is to create a tool to interactively explore the relationships between People listed on Wikipedia.

Everything is based on Wikimedia's regular database dumps of the English, which can be found [here](https://dumps.wikimedia.org/enwiki/).


# Project Structure

The whole project is divided into two subprojects.

1. Hadoop Jobs, which extract all necessary and interesting information from the Wikimedia dumps.
2. A web server, for interactive exploration.

## Hadoop Jobs

There are four different Hadoop jobs, which are used to incrementally parse and process the original data. \
Most of the database dumps can be ignored, since we're only interested in a small subset of that data.

## PersonArticleExtractor

This job is located at `./mapreduce-jobs/PersonArticleExtractor`. \
It's purpose is to filter all Wikipedia articles, that aren't related to any real-world persons

The result of this job is a significantly reduced database dump.
For reference, the original Wikimedia dump has nearly 6 million entries, the new dump has about 1.5 million entries.

**Attention:** \
All following jobs are always executed on this new dump!

## PersonData

This job is located at `./mapreduce-jobs/PersonData`. \
`PersonData` is responsible for collecting all information about a person from their article's metadata.

Wikimedia has various notations for metadata, which allows us to extract this data with a semi-sophisticated parser.

The result set is a collection of information for all people on Wikipedia.
It's a simple CSV file with some special formatting, and a pre-defined field order.

For more information, please look into the PersonData project's code.

## TitleExtractor and Relationships

These jobs are strongly connected. Their locations are `./mapreduce-jobs/TitleExtractor` and `./mapreduce-jobs/Relationships`.

At first, the TitleExtractor is run. This job simply returns a file with all titles of all Articles. \
The idea behind this, is that the title of an article can be seen as the primary key of an article.
What's nice about this, is that every link on Wikipedia uses this exact title/key to link to other articles.

The next step is the `Relationship` job.
This job takes the output of `TitleExtractor`, walks to the article and checks for each link whether the link points to a person or not. \
If a link points to that person, a new relationship is created.


# Installation

## Hadoop jobs

All Hadoop jobs are built with Maven. \
It's highly recommended building the project via the IntelliJ IDEA editor, since this is the way this project has been developed!

1. Open the project.
2. Run maven compilation steps
3. Copy the jar file to your Hadoop cluster node where it can be executed.

This is the same for all Hadoop projects. \
There's also a `run_test.sh` file, which can be used for reference on how to execute this. \
More on this in the section `Deploy and Execution`.

# Deployment and Execution

## Hadoop Jobs Deployment

In the following, we expect that you have:

1. Build all hadoop project
2. Uploaded all compiled `*.jar` files to the home (`~`) under the respective name of the project, e.g. `PersonArticle.jar`.

## Execution

## PersonArticle

1. Download the Wikimedia dump. We work with the `enwiki-latest-pages-meta-current.xml` dump.
   Upload it onto your hadoop cluster to a location of your choice.
   From now on, this location will be referred to as `$WIKI_DUMP`.
2. Create a hadoop directory `$OUTPUT`, which will be used to store the results.
3. Run the `PersonArticle` job on that dump.
    ```
    hadoop jar ~/PersonArticle.jar $WIKI_DUMP $OUTPUT/PersonArticle loglevel debug
    ```
4. Next, we combine all results of that job into a single XML file named `person_article.xml` and put it onto the hadoop cluster.
    ```
    hadoop fs -getmerge $OUTPUT/PersonArticles person_article.xml
    hadoop fs -copyFromLocal person_article.xml $OUTPUT/person_article.xm
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
    hadoop fs -copyFromLocal titles.txt OUTPUT/titles.txt
    ```

### Relationships

To use the `titles.txt` of the TitleExtractor, you have to adjust the Hadoop cacheFile path in `./mapreduce-jobs/Relationships/src/main/java/Relationships.java`.

Change the line, so it fits your local environment:
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
    hadoop fs -copyFromLocal relationship.csv OUTPUT/relationship.csv
    ```
