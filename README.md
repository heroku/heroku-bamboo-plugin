Running Locally
===============
- Install direct-to-heroku-client-java:
    - git clone git@github.com:heroku/direct-to-heroku-client-java.git
    - mvn install -DskipTests
- Download and install [Atlassian Plugin SDK](https://developer.atlassian.com/display/DOCS/Installing+the+Atlassian+Plugin+SDK)
- Run: `mvn bamboo:run` Note, if running with your own Maven, be sure to also specify the `settings.xml` in the SDK

Tests
=====
When running tests, be sure to provide system properties `heroku.apiKey` and `heroku.appName` for test API key and test app.