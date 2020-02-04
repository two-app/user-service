<!-- PROJECT LOGO -->
<br />
<p align="center">
    <img src="./image/logo.jpg" alt="Logo" width="auto" height="80">

  <h3 align="center">\[Scala] User-Service</h3>

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
### Get User Profile Data
```
GET /self
Authorization: Bearer {jwt}

200 OK: user: {uid, cid, pid, connectCode}
401 Unauthorized: missing/invalid JWT
500 Internal Server Error: user not found
```
_For more examples, please refer to the [Documentation](https://example.com)_

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
* [GitHub Emoji Cheat Sheet](https://www.webpagefx.com/tools/emoji-cheat-sheet)
* [Img Shields](https://shields.io)
* [Choose an Open Source License](https://choosealicense.com)
* [GitHub Pages](https://pages.github.com)
* [Animate.css](https://daneden.github.io/animate.css)
* [Loaders.css](https://connoratherton.com/loaders)
* [Slick Carousel](https://kenwheeler.github.io/slick)
* [Smooth Scroll](https://github.com/cferdinandi/smooth-scroll)
* [Sticky Kit](http://leafo.net/sticky-kit)
* [JVectorMap](http://jvectormap.com)
* [Font Awesome](https://fontawesome.com)





<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/othneildrew/Best-README-Template.svg?style=flat-square
[contributors-url]: https://github.com/othneildrew/Best-README-Template/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/othneildrew/Best-README-Template.svg?style=flat-square
[forks-url]: https://github.com/othneildrew/Best-README-Template/network/members
[stars-shield]: https://img.shields.io/github/stars/othneildrew/Best-README-Template.svg?style=flat-square
[stars-url]: https://github.com/othneildrew/Best-README-Template/stargazers
[issues-shield]: https://img.shields.io/github/issues/othneildrew/Best-README-Template.svg?style=flat-square
[issues-url]: https://github.com/othneildrew/Best-README-Template/issues
[license-shield]: https://img.shields.io/github/license/othneildrew/Best-README-Template.svg?style=flat-square
[license-url]: https://github.com/othneildrew/Best-README-Template/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=flat-square&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/othneildrew
[product-screenshot]: images/screenshot.png