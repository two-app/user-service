<!-- PROJECT LOGO -->
<br />
<p align="center">
    <img src="./image/logo.jpg" alt="Logo" width="auto" height="80">

  <h3 align="center">[Scala] User-Service</h3>

  <p align="center">
    User queries and actions.
    <br />
    <a href="https://github.com/othneildrew/Best-README-Template"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/two-app/user-service/issues">Report Bug</a>
    ·
    <a href="https://github.com/two-app/user-service/issues">Request Feature</a>
  </p>
</p>

<!-- TABLE OF CONTENTS -->
## Table of Contents

* [About the Project](#about-the-project)
  * [Built With](#built-with)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
* [Usage](#usage)
* [Running, Testing, Building](#Running,-Testing,-Building)
* [Configuration](#Configuration)
* [Road Map](#road-map)
* [Contributing](#contributing)
* [License](#license)
* [Acknowledgements](#acknowledgements)


## About The Project
A Scala implementation of the User Service:
* Register and Login
* Partner Connection
* Self and Partner Retrieval

### Built With
* [Scala 2.12](https://www.scala-lang.org/)
* [AKKA HTTP](https://doc.akka.io/docs/akka-http/)
* [Quill - MySQL Async](https://github.com/getquill/quill)


## Getting Started
### Prerequisites
* Java (8-11): https://www.oracle.com/technetwork/java/javase/downloads/index.html
* Scala: https://www.scala-lang.org/download/
* SBT (IntelliJ or Native)
* MySQL: https://dev.mysql.com/downloads/mysql/

## Usage
### Public (Gateway Enabled)
- Creating and Retrieving Self [/self](src/main/scala/user/SelfRoute.md)
- Connecting with Partner and retrieving Partner [/partner](src/main/scala/partner/PartnerRoute.md)

### Private (Internal)
- Retrieving User by Email [/user](src/main/scala/user/UserRoute.md)

## Running, Testing, Building
```bash
# make sure MySQL & S3 are running.
sbt run
sbt test

# generate a new artifact & docker image
sbt docker:publishLocal
```
More information on the test and build steps in the [CI config](./.github/scala.yml).

## Configuration
Default configuration values can be found in the [config file](./src/main/resources/application.conf). Configuration values are overridden using environment variables.

For example, `application.conf` features the following property:
```
server.port=8083
server.port=${?SERVER_PORT}
```

The `${?FOO}` syntax will override the default value `8083` if a system or environment variable is present with the key `SERVER_PORT`.

## Road Map
See the issues and project pages for a list of proposed features (and known issues).
* [open issues](https://github.com/two-app/user-service/issues)
* [project](https://github.com/orgs/two-app/projects/16)

## Contributing
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
Distributed under the MIT License. See `LICENSE` for more information.


<!-- ACKNOWLEDGEMENTS -->
## Acknowledgements
* [ReadMe Template](https://linkedin.com/in/othneildrew)