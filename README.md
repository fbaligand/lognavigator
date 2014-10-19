# LogNavigator

LogNavigator is a web application, made in java, which lets you browse your logs, wherever they are.

**Key points :**
- Navigate into your logs with the comfort of a web interface
- All your logs from different servers centralized in one entry point
- Configure log access from different sources : 
    - local directory
    - directory on a remote server (using SSH)
    - remote logs exposed by Apache Httpd DirectoryIndex (using HTTP)
- List logs, watch log content, filter log content, download log content
- Take advantage of powerful linux commands to get filtered log content
- Easily watch log content even if log file is archived in a `GZ` or `TAR.GZ` archive
- Optionally securise log access with user authentication and role-based authorization


# Getting Started

## Installation

- [Go to the last release](https://github.com/fbaligand/lognavigator/releases/latest) and download `WAR` file
- Deploy the `WAR` file on your favorite java app server (tomcat, jettty, glassfish, ...)
- Create a new file named `lognavigator.xml` wherever you want
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
- Link `lognavigator.xml` to your java app server, using one of these 3 means : 
    - Add system property `-Dlognavigator.config=file:/path/to/lognavigator.xml` to your server startup script
    - Add JNDI key/value `lognavigator.config=file:/path/to/lognavigator.xml` to your server JNDI configuration
    - Add `lognavigator.xml` directory to your server classpath
- Start your java app server
- That's all !


## First Use

- Once you have installed LogNavigator, open your favorite browser and go to :
[http://host:port/lognavigator/](http://host:port/lognavigator/)
- You see screenshot #1, the first log access configuration is selected in combobox at top right, and a table lists all log files and folders
- You can then change current log access configuration using combobox
- You can filter logs list using "Search" filter
- And finally you can of course click on a log file to watch its content
- You see screenshot #2
- By default, you see the 1000 last lines of the file (to avoid browser freeze due to too much content), using this command : `tail -1000 yourlog.log`
- You can then filter log content using powerful linux commands like `grep`.
- For example, type in `Command` field : `grep "GET" yourlog.log` and click the `Execute` button
- You can finally download filtered content by clicking `Download` button


# Screenshots

**1. logs list**
![](src/site/logs-list.png?raw=true)

**2. log content**
![](src/site/log-content.png?raw=true)


# Make it work on windows

Because LogNavigator is based on linux commands, LogNavigator doesn't work out-of-the-box on windows. But few steps make it work on windows :
- Download and install [Cygwin](http://cygwin.com/install.html)
- During install process, check option **_Net > curl_**
- Add following environment variables :
```dos
CYGWIN=mintty nodosfilewarning
CYGWIN_HOME=<cygwin install directory>
PATH=<previous value>;%CYGWIN_HOME%\bin
```

# Enable Security

Enabling security allows 2 points :
- Secure LogNavigator access with login/password authentication, based on HTTP Basic Authentication
- Provide user and role based authorization for each log access configuration

To enable security, few steps :
- create a new file called `lognavigator-authentication-context.xml`, containing users, passwords and roles (authorities).
Here's an example :
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans:beans xmlns:beans="http://www.springframework.org/schema/beans"
	   xmlns="http://www.springframework.org/schema/security"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security.xsd"
>
	<authentication-manager>
		<authentication-provider>
			<password-encoder hash="md5"/>
			<user-service>
				<user name="user1" password="5f4dcc3b5aa765d61d8327deb882cf99" authorities="role1, role2" />
				<user name="user2" password="5f4dcc3b5aa765d61d8327deb882cf99" authorities="role3" />
				<user name="user3" password="5f4dcc3b5aa765d61d8327deb882cf99" authorities="" />
			</user-service>
		</authentication-provider>
	</authentication-manager>
</beans:beans>
```
In this example, passwords are hashed using `md5` algorithm. But you can also use `sha`, `sha-256`, `bcrypt` or even `plaintext`.

- In your `lognavigator.xml` file, for each `log-access-config`, define authorized users and roles.
For example : 
```xml
<log-access-config id="one-config" ... authorized-roles="role1 role2" authorized-users="user1" />
```

- Activate security and reference `lognavigator-authentication-context.xml` in lognavigator configuration. To do that, 3 ways :
    - Add system properties `-Dspring.profiles.active=security-enabled -Dlognavigator.authentication.config=file:/path/to/lognavigator-authentication-context.xml` to your server startup script
    - Add JNDI keys/values `spring.profiles.active=security-enabled` and `lognavigator.authentication.config=file:/path/to/lognavigator-authentication-context.xml` to your server JNDI configuration
    - Put `lognavigator-authentication-context.xml` into your server classpath and define `spring.profiles.active=security-enabled` as system property or JNDI key/value 


# Requirements

- Java SE 6+
- Java Server compatible with Servlet API 2.5 (Tomcat 6+ , Jetty 6+, Glassfish 1+, JBoss AS 5+, ...)


# Change Log

Versions and Release Notes are listed in [Releases](https://github.com/fbaligand/lognavigator/releases) page


# Browser Compatibility

- Firefox
- Chrome
- IE8+
- Probably some others, but I have not tested


# Behind the scene

Technologies behind LogNavigator :
- Twitter Bootstrap 3.1
- Datatables
- jQuery
- Spring MVC
- Spring Security
- sshj (for remote SSH connections)
- linux commands (`tail`, `tar`, `gzip`, `curl`)


# License

LogNavigator is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
