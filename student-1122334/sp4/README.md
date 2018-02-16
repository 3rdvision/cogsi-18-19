STUDENT 1 - ITERATION 4
===============================

# 0. Introduction

This branch includes support for database using JPA (Java Persistence API) (<https://docs.oracle.com/javaee/6/tutorial/doc/bnbpz.html>).

The implementation of JPA that is used is Eclipselink (<http://www.eclipse.org/eclipselink/>). If for some reason Eclipse is unable to locate Eclipselink you mau have to configure it by right clicking in the project and selecting "Properties/JPA". The, in the "JPA implementation" use the "disk icon" to download JPA.

You can find a very simple tutorial on JPA in (<https://www.tutorialspoint.com/jpa/index.htm>).

The specific database that is configured is H2 (<http://www.h2database.com/html/main.html>).

How to insert an image:

![alt text](https://bitbucket.org/atb/cogsi-17-18-rep-template/raw/master/student1/it1/imgs/git2.jpg?fileviewer=file-view-default "Logo Title Text 1")

# 1. Overview

# 2. Important Remarks

## 2.1 GWT SDK Settings in Eclipse


## 2.2 Gradle Properties

# 3. Eclipse Requirements

## 3.1 GWT Eclipse Plugin

## 3.2 GWT SDK


# 4. How to Run


To stop the server open another terminal/console and type `gradle gwtStop`

# 5. How to Debug the Server Code

## 5.1 Running the Application from the command line


**Notes:**

- You must run the server application before running the new run configuration of eclipse. This is because the debug port 8000 must be open by the server before starting the debug in Eclipse.

- Also note that there is a "debugSuspend" property in gradle (default value is "false") that you can set so that the server waits for the start of the Eclipse debug session.

## 5.2 Running the Application from Eclipse

You can also set up a "GWT Development Mode (Dev Mode)" configuration in Eclipse. If you do that, eclipse will launch the GWT Application in Debug Mode. In this case it is not necesssary to run the Application from the command line.

Setup the "GWT Development Mode (Dev Mode)" in this way:

- In tab "Main"

	1. Select the project CMS
	2. As main classe enter: "com.google.gwt.dev.DevMode"

- In tab "Server"

	1. Check "Run built-in server"
	2. Port: 8888

- In tab "GWT"

	1. Check "Super Development Mode"
	2. In avalable modules select "pt.isep.cms.Showcase"

- In tab "Arguments"

	1. In program arguments enter:

	-remoteUI "${gwt_remote_ui_server_port}:${unique_id}" -logLevel INFO -codeServerPort 9997 -war /workspaces/odsoft/2017/odsoft-edom-2017/cms/src/main/webapp -port 8888 pt.isep.cms.Showcase

Note: You should replace "/workspaces/odsoft/2017/odsoft-edom-2017/cms/src/main/webapp" by your own path.

- In the tab "classpath" add an entry for the sources of the project, i.e., /cms/src/main/java

# 6. How to Debug the Client Code

The simplest way to debug the client code is to use a "Launch Chrome" configuration in Eclipse.

Create a "Launch Chrome" configuration:

- In the "Main" tab:

	1. In URL enter "http://127.0.0.1:8888/Showcase.html"
	2. In Project select the "cms" project

**Notes:**

- Remember that the server must be running! You should run the server in debug mode using a running configuration in Eclipse or the `gwtDev` task in gradle.

- Confirm that the port number is correct (either 8888 or other you have setup)

# 7. Using the Command Line with Gradle

To get a list of available GWT tasks simple type **gradle tasks** in a terminal or console.

Some common commands:

**gradle build**: builds the application (a **war** file is produced in **build/libs**)

**gradle gwtRun**: the jetty web serever is started (using port 8080) to serve the application. You can open the application in a browser with the following url <http://127.0.0.1:8080/Contacts.html>

**gradle gwtDev**: gwt starts in development mode. You should be able to update the code of the application and the changes should be automatically visible. You can open the application in a browser with the following url <http://127.0.0.1:8080/Contacts.html>

# 8. Credits

This project is based on examples from the GWT project (http://www.gwtproject.org), specially:

- http://samples.gwtproject.org/samples/Showcase/Showcase.html

- http://www.gwtproject.org/articles/mvp-architecture.html
