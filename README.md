# fabXaccess

Backend for fabX access system. Written using [ktor](https://ktor.io) and [exposed](https://github.com/JetBrains/Exposed).

Compatible AngularJS frontend (with screenshots in readme): [fabXdashboard](https://github.com/sschaeffner/fabXdashboard).

# Setup

For a simple setup we would recommend forking this repository on GitHub and then auto-deploying it to Heroku. This
allows for independent updates by pulling changes from this repository when desired.
[Heroku's free tier](https://www.heroku.com/pricing) should be enough for most users.
The following describes this setup.

## Github
* Fork this repository

## Heroku
* Create an account on or login to [Heroku](https://www.heroku.com/)
* Create a new App, give it a name and select a region
* Connect to GitHub and authorize Heroku (such that Heroku can automatically deploy the forked repository)
* Search for the forked repository and click connect
* Manually trigger a deployment of master/main branch

## Database
* Click on the "Resources" Tab in Heroku
* In the Add-ons section search for PostgreSQL and select Heroku Postgres
* Select a Plan and submit the order
* Under "More" click restart all dynos
* Open a postgres client connection to the Heroku Postgres database
    * Install Heroku CLI https://devcenter.heroku.com/articles/heroku-cli
    * Install PostgresSQL as described here https://devcenter.heroku.com/articles/heroku-postgresql#local-setup
    * Now enter the psql client with ```heroku psql -a [appname]```
* Add users to the "admins" table with
```INSERT INTO admins ("name", "passwordHash") VALUES ('[name]', '[hash]') ```
    * Replace [name] with the username and [hash] with the hashed password, for hashing use the provided script [tooling/createPasswordHash.sh](https://github.com/sschaeffner/fabXaccess/blob/master/tooling/createPasswordHash.sh)
* Password can be changed with ```UPDATE admins SET passwordHash = '[hash]' where name = '[name]' ``` (if necessary)

Setup for the Backend is done, continue with the setup of [Dashboard](https://github.com/sschaeffner/fabXdashboard).