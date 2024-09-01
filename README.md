# springboot-mybatis-oracle-demo
Demo code for starting development sprint boot application using mybatis and oracle database.

It demostrates some basic operations and get you started to further development.

1. You need to have a running oracle database (tested with 12c release 2).
2. Create table using schema.sql
3. Change application.properties to your settings.
4. You need to manually add ojdbc8.jar to library from lib folder because oracle does not host it on maven central.
5. Run 'mvn package' to compile and run the program.
