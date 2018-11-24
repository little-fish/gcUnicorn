gcUnicorn
=========
gcUnicorn is an opensource tool for [geocaching.com](https://www.geocaching.com/) platform. You can use it either as a web application requiring nothing more but [Java JRE](https://www.oracle.com/technetwork/java/javase/downloads/index.html) installed on your machine or as an Android application.

The tool allows you to search for caches of selected types within given coordinates and radius. Its output contains not only exhaustive cache details, but links to spoiler images, detailed log entries including links to uploaded pictures as well. The output is served as GPX file with following schemas applied to it:
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

## Modules
The application contains two modules:

* __core__ - Core functionality.
* __webapp__ - Web application. UI has been created with simplicity and perfect readability in mind so it works on your desktop and mobile devices with no issues.
* __android__ - Android application. It allows you to the the very same operations like _webapp_ module.

## Screenshots
### Web application
![Login page](https://goo.gl/ZKYxwF) ![Search page](https://goo.gl/6nx6C5) ![Queue page](https://goo.gl/Kf31cL)

See [gcUnicorn-webapp album](https://photos.app.goo.gl/vA4nyUmZSjE3HxUQ8) for full resolution screenshots.

### Android application
![Main activity](https://goo.gl/FPmUJ1) ![Settings activity](https://goo.gl/NLgWMW) ![About activity](https://goo.gl/1mWPo6)

See [gcUnicorn-android album](https://photos.app.goo.gl/8TUfrMJ5ZF7pc8nLA) for full resolution screenshots.

## How to run
### Web application
For Windows OS there is an executable file created. This approach always enables tray icon.

If you want to run the application from within command line, run the following command:
```bash
java -jar gcUnicorn-webapp-<version>.jar
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

#### How to configure
The application uses [Spring Boot](https://spring.io/projects/spring-boot) so basic configuration can be done by creating `application.properties` file next to the `gcUnicorn-webapp-<version>.jar` with properties specified inside it.

The list of all available properties (not all of them are applicable to the application) can be found on [Spring Boot reference page](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html).

If the application will be exposed on the internet it is recommended to enable SSL within the application. You can do that by specifying required `server.ssl.*` properties and providing valid certificate. See the reference page for more details.

You can find examples of configuration files inside [webapp](https://github.com/little-fish/gcUnicorn/tree/master/webapp/resources/configuration-examples) module.

##### Application port
You have two options how to configure application port:
* Add `-Dserver.port=<port-number>` argument to the executing command:
```bash
java -jar -Dserver.port=<port-number> gcUnicorn-webapp-<version>.jar
```
* Create/edit `application.properties` file next to the `gcUnicorn-webapp-<version>.jar` with following content:
```
server.port=<port-number>
```

##### Logging configuration
The application uses Logback implementation. You can read about its configuration on its [homepage](https://logback.qos.ch/manual/configuration.html).

You can specify logging configuration with following options:
 
* Add `-Dlogging.config=file:<path-to-logging-config>` argument to the executing command:
```bash
java -jar -Dlogging.config=file:<path-to-logging-config> gcUnicorn-webapp-<version>.jar
```
* Create/edit `application.properties` file next to the `gcUnicorn-webapp-<version>.jar` with following content:
```
logging.config=file:<path-to-logging-config>
```

##### Displaying tray icon
If you want to shut down the application quickly, tray icon can be handy. To display the tray icon, you have two options here as well:
* Add `-Dtray` argument to the executing command:
```bash
java -jar -Dtray gcUnicorn-webapp-<version>.jar
```
* Create/edit `application.properties` file next to the `gcUnicorn-webapp-<version>.jar` with following content:
```
tray
```

### Android application
To install the application, you have to enable _Unknown Sources_ first.

Android version of the tool supports all Android version since Jelly Bean (API 16) onwards.

As a bonus, the application interacts with popular Android map application called [Locus Map](http://www.locusmap.eu/). You can find shortcuts to gcUnicorn within main _Geocaching functions_ menu and within _Point view_'s share option. If you open the application from later option, latitude and longitude will be transferred from Locus directly into the gcUnicorn.

All the created GPX files are stored within `<externalStorage>/gcUnicorn`.

## How to build
Clone the repository:
```bash
git clone https://github.com/little-fish/gcUnicorn.git
```
Navigate to the cloned folder:
```bash
cd gcUnicorn
```
Build the applications:
```bash
./gradlew build
```
Created artifacts for _core_ and _webapp_ modules are located within `<module>/build/libs` folder. For _android_ module the artifact is located within `<module>/build/outputs/apk`.

### core
__Core__ module is now shared with an __android__ module and because its minimal API is 16, core module doesn't have tu use any Java 8 fancy features which are not available in Android SDK 18. To catch the exception earlier, the module should be compiled with Java 7 compiler.

To compile the module with proper JDK, you should create `local.properties` file directly within _core_ module and specify `java.home` property pointing to proper JDK. Once the property is set, all Kotlin compile tasks will use this JDK.

Possible content of _local.properties_:
```bash
java.home=/path/to/proper/jdk
```

### android
To enable location picking from Google Maps, Google Places API key has to be provided during build time.

To obtain your key, follow the instruction: [Get API Key](https://developers.google.com/places/web-service/get-api-key).

Once you obtain your key, you have to specify it within `/android/src/main/res/values/google_api_key.xml` file. Otherwise location picking won't work properly.

## Signing
### core & webapp
_Core_ and _webapp_ modules could use two different signing mechanisms. It is up to you which one you choose.
* __Gradle Signing plugin__ - You can read about it on its [homepage](https://docs.gradle.org/current/userguide/signing_plugin.html).
* __jarsigner__ - Read more [here](https://docs.oracle.com/javase/tutorial/deployment/jar/signing.html).

If you want to use any of the methods mentioned above, you have to create `keystore.properties` file within _module_ directory and specify additional (self-explanatory) properties:
* Gradle Signing plugin:
  * `signing.keyId`
  * `signing.password`
  * `signing.secretKeyRingFile`
  
  Once all the properties are provided, all output artifacts will be signed automatically.

* jarsigner:
  * `jarsigning.keystore`
  * `jarsigning.keystoreType`
  * `jarsigning.keystorePassword`
  * `jarsigning.keyPassword`
  * `jarsigning.alias`
  
  Once all the properties are provided, `signedJar` (`signedBoorJar` respectively) Gradle task will be created and hooked into the build process.

### android
_Android_ module uses standard signing described on Android [publishing](https://developer.android.com/studio/publish/app-signing) page.
To enable signing, simply create `keystore.properties` within the module directory and and specify additional (self-explanatory) properties:
* `keyAlias` 
* `keyPassword` 
* `storeFile` 
* `storePassword` 

Once the properties are provided, signing will be enabled automatically.

## Additional notes
* __Skip premium caches__ - If enabled the tool will simply skip premium caches __earlier__. If you are a _basic member_ and disable this option, the tool will try to load cache's details anyway and once it discovers that it can not load the cache (because you are a _basic member_), it skips it. (The tool has no idea whether you are a _basic member_ or a _premium_ one.)

## Release notes
##### 2018-11-24: android v1.0.1
* __android__
  * Changed navigation between activities.

##### 2018-11-23: core v2.0.0 & webapp v1.0.3 & android v1.0.0
* __core__
  * Removed Java 8 features not available at Android SDK 16. (See [How to build](#core).)
  * Updated exception handling.
* __webapp__
  * Changed default cache count from `50` to `100`.
* __android__
  * First release.
* Added signing options to all modules.
* Updated dependencies to newer versions.

## How to contribute
Any kind of contribution is welcome. If you have any ideas, bug-fxies or improvements, just create a pull request or contact me via email. Thank you.

## How to donate
[![PayPal](https://i1.wp.com/hasutsuki.com/wp-content/uploads/2018/06/buy-me-a-coffee-with-paypal.png?ssl=1)](https://www.paypal.me/gcUnicorn)

## Acknowledgment
Special thanks to [c:geo](https://github.com/cgeo/cgeo) team for inspiring me to create the application.

