
# Nokia XML data file simulator

to run standalone 

```
mvn clean install
mvn spring-boot:run
```

the application should show up at localhost use

```
http://localhost:8090/nokia/data
```

within docker-compose jetty use

```
http://restsimulator:8080/nokia/data
```

change count of messages sent

```
http://restsimulator:8080/nokia/data?count=20
```
  
adding interval in ms between samples (60000 ms = 1 minute)

```
http://restsimulator:8080/nokia/data?count=20&interval=60000
```