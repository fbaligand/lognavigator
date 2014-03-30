# LogNavigator

LogNavigator is a web application, made in java, which lets you browse your logs, wherever they are.

Key points :
* Navigate into your logs with the comfort of a web interface
* All your logs from different servers centralized in one entry point
- Configure log access from different sources : 
    - local directory
    - directory on a remote server (using SSH)
    - remote logs exposed by Apache Httpd DirectoryIndex (using HTTP)
* List logs, watch log content, filter log content, download log content
* Take advantage of powerful linux commands to get filtered log content
* Easily watch log content even if log file is archived in a `GZ` or `TAR.GZ` archive
* Optionally securise log access with user authentication and role-based authorization


# How to install it

- Firstable, [go to the last release](https://github.com/fbaligand/lognavigator/releases/latest) and download `WAR` file
- Then, deploy the `WAR` file on your favorite java app server (tomcat, jettty, glassfish, ...)
- Create a new file named `lognavigator.xml` at the root of your app server classpath
- Define your log access configurations inside `lognavigator.xml`. For example :
```xml
<?xml version="1.0" encoding="UTF-8"?>
<lognavigator-config>
    <log-access-config id="a-local-dir" type="LOCAL" directory="/path/to/logs" display-group="local-configs" />
    <log-access-config id="b-remote-dir-using-ssh" type="SSH" user="your-user" host="remote-host" directory="/path/to/logs" display-group="remote-configs" />
    <log-access-config id="c-remote-httpd-logs" type="HTTPD" url="http://archive.apache.org/dist/tomcat/" display-group="httpd-configs" />
</lognavigator-config>
```
- _Important note :_ if you define a `SSH` configuration, your local user hosting lognavigator server must have its ssh private key authorized to access remote host using remote user.
- Start your java app server
- That's all !


## How to use it 

- Once you have installed LogNavigator, open your favorite browser and go to : 
http://localhost:your-appserver-port/lognavigator/
- you will see screenshot #1

## Behind the scene: 

- Twitter Bootstrap 3.1
- Datatables
- jQuery
- Spring MVC
- Spring Security
- sshj


Compatibility :
---------------

Firefox, Chrome, IE8+
