Running Locally
===============
1. Download and install [Atlassian Plugin SDK](https://developer.atlassian.com/display/DOCS/Installing+the+Atlassian+Plugin+SDK)
2. Run: `mvn bamboo:run` Note, if running with your own Maven, be sure to also specify the `settings.xml` in the SDK

Tests
=====
When running tests, be sure to provide system properties `heroku.apiKey` and `heroku.appName` for test API key and test app.