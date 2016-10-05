# CrawlerPasteBin
Java crawler specific for pastebin

Catch every new pastebin post content and email adresses. They are save in mongodb database.
## Setup
- Install java

``apt-get install openjdk-7-jre``
- Install mongodb

``apt-get install mongodb``
- Configure it

[Official documentation](https://docs.mongodb.com/manual/reference/configuration-options/)
## Use
``java -jar crawlerPastebin.jar [userMongo passwordMongo] \[host]``
