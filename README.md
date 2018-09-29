gcUnicorn
=========
gcUnicorn is an opensource tool for [geocaching.com](https://www.geocaching.com/) platform. It is a web application requiring nothing more but [Java JRE](https://www.oracle.com/technetwork/java/javase/downloads/index.html) installed on your machine.

The web application allows you to search for caches of selected types, within given coordinates and radius. Its output contains not only exhaustive cache details, but links to spoiler images, detailed log entries including links to uploaded pictures as well. The output is served as GPX file with following schemas applied to it:
* GPX v1.1
* Groundspeak v1.0.1
* Gsak v1.6

GPX file created by the application is readable by following applications:
* [Cachly](http://www.cach.ly/)
* [Locus](http://www.locusmap.eu/)

_If you test the output GPX format with other applications, please, let me know so I can update the list. Thank you._

## Disclaimer
Be sure to read [Groundspeak's terms of use](https://www.geocaching.com/account/documents/termsofuse). By using the application you may violate some of them and your Geocaching account may be suspended or deleted.

Author of the application is not responsible for any damage caused by using it.

## Screenshots
![Login page](https://goo.gl/ZKYxwF) ![Search page](https://goo.gl/6nx6C5) ![Queue page](https://goo.gl/Kf31cL)

See [gcUnicorn album](https://photos.app.goo.gl/vA4nyUmZSjE3HxUQ8) for full resolution screenshots.

## How to run
For Windows OS there is an executable file created. This approach always enable tray icon.

If you want to run the application from within command line, run the following command:
```bash
java -jar gcUnicorn-webapp-1.0.0.jar
```
The application is then available at `http://<hostname>:8080/`. Most likely it will be `http://localhost:8080/`

#### Login page
On the login page you have to provide your Geocaching credentials. The credentials are not being stored anywhere nor send to any 3rd party servers. The application uses them to log you into Geocaching page.

#### Search page
Coordinates field supports following standards:
* __Decimal degrees__ format with following examples:

   * N 18,556°, E 45.555°
   
   * N18,W 45.555
   
   * N 18,556 , E 45°
   
   * s 18.556° , W45°
* __Degrees decimal minutes__ format with following examples:

   * N 18° 55', E 45° 55'
   
   * N9° 55', E 45° 55
   
   * N 18,556 , E 45°
   
   * N 18°, E 45° 55'
   
   * S°55.55',W45°

#### Queue page
Here you can see all your search jobs. If a job is still running, spinning circle is shown. Once the job is done, download icon is shown.


## How to configure
The application uses [Spring Boot](https://spring.io/projects/spring-boot) so basic configuration can be done by creating `application.properties` file next to the `gcUnicorn-webapp-1.0.0.jar` with properties specified inside it.

The list of all available properties (not all of them are applicable to the application) can be found on [Spring Boot reference page](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html).

If the application will be exposed on the internet it is recommended to enable SSL within the application. You can do that by specifying required `server.ssl.*` properties and providing valid certificate. See the reference page for mor details.

You can find examples of configuration files inside [webapp](https://github.com/little-fish/gcUnicorn/tree/master/webapp/resources/configuration-examples) module.

#### Application port
You have two options how to configure application port:
* Add `-Dserver.port=<port-number>` argument to the executing command:
```bash
java -jar -Dserver.port=<port-number> gcUnicorn-webapp-1.0.0.jar
```
* Create/edit `application.properties` file next to the `gcUnicorn-webapp-1.0.0.jar` with following content:
```
server.port=<port-number>
```

#### Logging configuration
The application uses Logback implementation. You can read about its configuration on its [homepage](https://logback.qos.ch/manual/configuration.html).

You can specify logging configuration with following options:
 
* Add `-Dlogging.config=file:<path-to-logging-config>` argument to the executing command:
```bash
java -jar -Dlogging.config=file:<path-to-logging-config> gcUnicorn-webapp-1.0.0.jar
```
* Create/edit `application.properties` file next to the `gcUnicorn-webapp-1.0.0.jar` with following content:
```
logging.config=file:<path-to-logging-config>
```

#### Displaying tray icon
Tray icon can be handy if you want to quickly shut down the application. To display tray icon, you have two options here as well:
* Add `-Dtray` argument to the executing command:
```bash
java -jar -Dtray gcUnicorn-webapp-1.0.0.jar
```
* Create/edit `application.properties` file next to the `gcUnicorn-webapp-1.0.0.jar` with following content:
```
tray
```

## Modules
The application contains two modules:

* __core__ - Core functionality.
* __webapp__ - Web application. UI has been created with simplicity and perfect readability in mind so it works on your desktop and mobile devices with no issues.

## How to build
Clone the repository:
```bash
git clone https://github.com/little-fish/gcUnicorn.git
```
Navigate to the cloned folder:
```bash
cd gcUnicorn
```
Build the application:
```bash
./gradlew build
```
Created artifacts are located within `<module>/build/libs` folder.

## How to contribute
Any kind of contribution is welcome. If you have any ideas, bug-fxies or improvements, just create a pull request or contact me via email. Thank you.

## Acknowledgment
Special thanks to [c:geo](https://github.com/cgeo/cgeo) team for inspiring me to create the application.
