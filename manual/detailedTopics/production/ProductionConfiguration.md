<!--- Copyright (C) 2009-2015 Typesafe Inc. <http://www.typesafe.com> -->
# Additional configuration

There are a number of different types of configuration that you can configure in production.  The three mains types are:

* [General configuration](#General-configuration)
* [Logging configuration](#Logging-configuration)
* [JVM configuration](#JVM-configuration)

Each of these types have different methods to configure them.

## General configuration

Play has a number of configurable settings. You can configure database connection URLs, the application secret, the HTTP port, SSL configuration, and so on.

Most of Play's configuration is defined in various `.conf` files, which use the [HOCON format](https://github.com/typesafehub/config/blob/master/HOCON.md). The main configuration file that you'll use is the `application.conf` file. You can find this file at `conf/application.conf` within your project. The `application.conf` file is loaded from the classpath at runtime (or you can override where it is loaded from). There can only be one `application.conf` per project.

Other `.conf` files are loaded too. Libraries define default settings in `reference.conf` files. These files are stored in the libraries' JARs—one `reference.conf` per JAR—and aggregated together at runtime. The `reference.conf` files provide defaults; they are overridden by any settings defined in the `application.conf` file.

Play's configuration can also be defined using system properties and environment variables. This can be handy when settings change between environments; you can use the `application.conf` for common settings, but use system properties and environment variables to change settings when you run the application in different environments.

System properties override settings in `application.conf`, and `application.conf` overrides the default settings in the various `reference.conf` files.

You can override runtime configuration in several ways. This can be handy when settings vary between environments; you can changing the configuration dynamically for each environment. Here are your choices for runtime configuration:

* Using an alternate `application.conf` file.
* Overriding individual settings using system properties.
* Injecting configuration values using environment variables.

### Specifying an alternate configuration file

The default is to load the `application.conf` file from the classpath. You can specify an alternative configuration file if needed:

#### Using `-Dconfig.resource`

This will search for an alternative configuration file in the application classpath (you usually provide these alternative configuration files into your application `conf/` directory before packaging). Play will look into `conf/` so you don't have to add `conf/`.

```
$ /path/to/bin/<project-name> -Dconfig.resource=prod.conf
```

#### Using `-Dconfig.file`

You can also specify another local configuration file not packaged into the application artifacts:

```
$ /path/to/bin/<project-name> -Dconfig.file=/opt/conf/prod.conf
```

> Note that you can always reference the original configuration file in a new `prod.conf` file using the `include` directive, such as:
>
> ```
> include "application.conf"
>
> key.to.override=blah
> ```

### Overriding configuration with system properties

Sometimes you don't want to specify another complete configuration file, but just override a bunch of specific keys. You can do that by specifying then as Java System properties:

```
$ /path/to/bin/<project-name> -Dplay.crypto.secret=abcdefghijk -Ddb.default.password=toto
```

#### Specifying the HTTP server address and port using system properties

You can provide both HTTP port and address easily using system properties. The default is to listen on port `9000` at the `0.0.0.0` address (all addresses).

```
$ /path/to/bin/<project-name> -Dhttp.port=1234 -Dhttp.address=127.0.0.1
```

#### Changing the path of RUNNING_PID

It is possible to change the path to the file that contains the process id of the started application. Normally this file is placed in the root directory of your play project, however it is advised that you put it somewhere where it will be automatically cleared on restart, such as `/var/run`:

```
$ /path/to/bin/<project-name> -Dpidfile.path=/var/run/play.pid
```

> Make sure that the directory exists and that the user that runs the Play application has write permission for it.

Using this file, you can stop your application using the `kill` command, for example:

```
$ kill $(cat /var/run/play.pid)
```

### Using environment variables

You can also reference environment variables from your `application.conf` file:

```
my.key = defaultvalue
my.key = ${?MY_KEY_ENV}
```

Here, the override field `my.key = ${?MY_KEY_ENV}` simply vanishes if there's no value for `MY_KEY_ENV`, but if you set an environment variable `MY_KEY_ENV` for example, it would be used.

### Server configuration options

A full list of server configuration options, including defaults, can be seen here:

```
play {
  server {

    # The root directory for the Play server instance. This value can
    # be set by providing a path as the first argument to the Play server
    # launcher script. See `ServerConfig.loadConfiguration`.
    dir = ${?user.dir}

    # HTTP configuration
    http {
      # The HTTP port of the server. Use a value of "disabled" if the server
      # shouldn't bind an HTTP port.
      port = 9000
      port = ${?http.port}

      # The interface address to bind to.
      address = "0.0.0.0"
      address = ${?http.address}
    }

    # HTTPS configuration
    https {

      # The HTTPS port of the server.
      port = ${?https.port}

      # The interface address to bind to
      address = "0.0.0.0"
      address = ${?https.address}

      # The SSL engine provider
      engineProvider = "play.core.server.ssl.DefaultSSLEngineProvider"
      engineProvider = ${?play.http.sslengineprovider}

      # HTTPS keystore configuration, used by the default SSL engine provider
      keyStore {
        # The path to the keystore
        path = ${?https.keyStore}

        # The type of the keystore
        type = "JKS"
        type = ${?https.keyStoreType}

        # The password for the keystore
        password = ""
        password = ${?https.keyStorePassword}

        # The algorithm to use. If not set, uses the platform default algorithm.
        algorithm = ${?https.keyStoreAlgorithm}
      }

      # HTTPS truststore configuration
      trustStore {

        # If true, does not do CA verification on client side certificates
        noCaVerification = false
      }
    }

    # The type of ServerProvider that should be used to create the server.
    # If not provided, the ServerStart class that instantiates the server
    # will provide a default value.
    provider = ${?server.provider}

    # The path to the process id file created by the server when it runs.
    # If set to "/dev/null" then no pid file will be created.
    pidfile.path = ${play.server.dir}/RUNNING_PID
    pidfile.path = ${?pidfile.path}

    # Configuration options specific to Netty
    netty {
      # The maximum length of the initial line. This effectively restricts the maximum length of a URL that the server will
      # accept, the initial line consists of the method (3-7 characters), the URL, and the HTTP version (8 characters),
      # including typical whitespace, the maximum URL length will be this number - 18.
      maxInitialLineLength = 4096
      maxInitialLineLength = ${?http.netty.maxInitialLineLength}

      # The maximum length of the HTTP headers. The most common effect of this is a restriction in cookie length, including
      # number of cookies and size of cookie values.
      maxHeaderSize = 8192
      maxHeaderSize = ${?http.netty.maxHeaderSize}

      # The maximum length of body bytes that Netty will read into memory at a time.
      # This is used in many ways.  Note that this setting has no relation to HTTP chunked transfer encoding - Netty will
      # read "chunks", that is, byte buffers worth of content at a time and pass it to Play, regardless of whether the body
      # is using HTTP chunked transfer encoding.  A single HTTP chunk could span multiple Netty chunks if it exceeds this.
      # A body that is not HTTP chunked will span multiple Netty chunks if it exceeds this or if no content length is
      # specified. This only controls the maximum length of the Netty chunk byte buffers.
      maxChunkSize = 8192
      maxChunkSize = ${?http.netty.maxChunkSize}

      # Whether the Netty wire should be logged
      log.wire = false
      log.wire = ${?http.netty.log.wire}

      # Netty options. Possible keys here are defined by:
      #
      # http://netty.io/3.9/api/org/jboss/netty/channel/socket/SocketChannelConfig.html
      # http://netty.io/3.9/api/org/jboss/netty/channel/socket/ServerSocketChannelConfig.html
      # http://netty.io/3.9/api/org/jboss/netty/channel/socket/nio/NioSocketChannelConfig.html
      #
      # Options that pertain to the listening server socket are defined at the top level, options for the sockets associated
      # with received client connections are prefixed with child.*
      option {

        # Set whether connections should use TCP keep alive
        # child.keepAlive = false

        # Set whether the TCP no delay flag is set
        # child.tcpNoDelay = false

        # Set the size of the backlog of TCP connections.  The default and exact meaning of this parameter is JDK specific.
        # backlog = 100
      }
    }
  }

  # Configuration specific to Play's experimental Akka HTTP backend
  akka {
    # How long to wait when binding to the listening socket
    http-bind-timeout = 5 seconds
  }
}
```

## Logging configuration

Logging can be configured by creating a logback configuration file.  This can be used by your application through the following means:

### Bundling a custom logback configuration file with your application

Create an alternative logback config file called `logback.xml` and copy that to `<app>/conf`

You can also specify another logback configuration file via a System property. Please note that if the configuration file is not specified then play will use the default `logback.xml` that comes with play in the production mode. This means that any log level settings in `application.conf` file will be overridden. As a good practice always specify your `logback.xml`.

### Using `-Dlogger.resource`

Specify another logback configuration file to be loaded from the classpath:

```
$ /path/to/bin/<project-name> -Dlogger.resource=conf/prod-logger.xml
```

### Using `-Dlogger.file`

Specify another logback configuration file to be loaded from the file system:

```
$ /path/to/bin/<project-name> -Dlogger.file=/opt/prod/prod-logger.xml
```

### Using `-Dlogger.url`

Specify another logback configuration file to be loaded from an URL:

```
$ /path/to/bin/<project-name> -Dlogger.url=http://conf.mycompany.com/logger.xml
```

## JVM configuration

You can specify any JVM arguments to the application startup script. Otherwise the default JVM settings will be used:

```
$ /path/to/bin/<project-name> -J-Xms128M -J-Xmx512m -J-server
```

As a convenience you can also set memory min, max, permgen and the reserved code cache size in one go; a formula is used to
determine these values given the supplied parameter (which represents maximum memory):

```
$ /path/to/bin/<project-name> -mem 512 -J-server
```
