# gpx2fit-java

### build

```
mvn install:install-file -Dfile=libs/fit.jar -DgroupId=com.garmin.fit -DartifactId=fit-sdk -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true
mvn package
```



### Usage

```
java -jar gpx2fit.jar xxx.gpx
# with out fit name
java -jar gpx2fit.jar xxx.gpx xxx.fit
```