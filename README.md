# Dad joke Strava activity title generator
This app is meant to spice up your Strava activity feed. It calls an API to get a random dad joke. Then sets your Strava activity title to display the joke.

[The app is deployed here](https://dad-joke-strava-title.herokuapp.com/). 

## Running Locally
Make sure you have Java 8 installed.  Also, install the [Heroku CLI](https://cli.heroku.com/) if you plan to deploy to Heroku.

```sh
$ git clone https://github.com/yonidoronpeters/dad-joke-strava-title.git
$ cd dad-joke-strava-title
$ ./gradlew build
$ docker-compose -f src/main/docker/docker-compose.yml up -d
# Start it up
$ SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The app should now be running on [localhost:5000](http://localhost:5000/).

## Deploying to Heroku
This app is meant to be easily deployable to Heroku. It was created from the [Getting Started with Kotlin on Heroku](https://github.com/heroku/kotlin-getting-started.git) app.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

```sh
$ heroku create
$ git push heroku main
$ heroku open
```
