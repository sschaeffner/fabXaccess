# fabXaccess

Backend for fabX access system. Written using [ktor](https://ktor.io) and [exposed](https://github.com/JetBrains/Exposed).

Compatible AngularJS frontend (with screenshots in readme): [fabXdashboard](https://github.com/sschaeffner/fabXdashboard).


# Setup
## Github
Fork this Repo.

## Heroku

* Create a Heroku account on https://www.heroku.com/
* Login and create a new App, give it a name and select a region
* Connect to Github and authorize Heroku
* Search for the forked repository and click connect
* Manually trigger a deploy on master/main branch

## Database
* Click on the "Resources" Tab in Heroku
* In the Add-ons section search for Postgresql and select Heroku Postgres
* Select a Plan and submit the order
* Under "More" click restart all dynos
* Install Heroku CLI https://devcenter.heroku.com/articles/heroku-cli
* Install PostgresSQL as described here https://devcenter.heroku.com/articles/heroku-postgresql#local-setup

> Currently the database tables are created with the first connection. But connecting is not possible because auth requires users in the database  
Workaround:
> * In the web dashboard, under "Settings", add a config var named DEMO_CONTENT with value true
> * Restart the Dyno and delete the config var  
>
> Tables should be setup correctly now!

* Now enter the psql shell with ```heroku sql -a [appname]```
* Add users to the "admins" table with
```INSERT INTO admins ("name", "passwordHash") VALUES ('[name]', '[hash]') ```
* Replace [name] with the username and [hash] with the hashed password, for hashing use the provided script tooling/createPasswordHash.sh
* Password can be changed with SQL ```UPDATE admins SET passwordHash = '[hash]' where name = '[name]' ```


Setup for the Backend is done!

Continue with setup of https://github.com/sschaeffner/fabXdashboard