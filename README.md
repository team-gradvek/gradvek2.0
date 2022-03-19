# gradvek
GRaph of ADVerse Event Knowledge

## Running gradvek locally

To run the `gravek` application locally, run `docker-compose up` from the project's root directory. This will spin up two docker containers:

1. a Neo4j container, which will run the database used by this application
2. [gradvek/app](https://hub.docker.com/r/gradvek/app), which is the latest version of this application that has been deployed to [DockerHub](https://hub.docker.com/r/gradvek/app).

The gradvek application will be available on port 3000; the Neo4j database will be available at port 7474.

If you want to make local changes to the application, you will need to create your own local development environment instead of using the image hosted on DockerHub. See  the instructions in `springdb/readme.md`.

## Remote build process

GitHub Actions builds the artifacts needed for deployment.
There is one workflow divided into three jobs:
* test
  * The first job uses the Maven `test` goal to run regression tests.
* build
  * The second job uses the Maven `install` goal to create a production build.  The build artifacts are saved for use in the next job.
* deploy
  * The build artifacts from the previous job are restored.  Then the main project Dockerfile is used to generate an image, which is pushed to Docker Hub.

Prior to building and deploying in production, the [GitHub context](https://docs.github.com/en/actions/learn-github-actions/contexts#github-context) is checked to ensure that the `master` branch is being used.
Otherwise, these jobs are skipped.

Separating the build and deploy jobs has two main advantages.
First, it's clear at a glance if a failure occurred during Maven or during Docker Hub operations.
Second, the build artifacts are available for later inspection if desired for troubleshooting.

## Running against Neo4j Desktop

* Start Neo4j Desktop
* Create a new project if you haven't set one up before
  * Add a local DBMS to the project
  * Set the password to match that in src/main/resources/application.properties
  * In the DBMS settings (click the ... while hovering over it):
    * Search for "non-local connections"
    * Uncomment the next line that reads `dbms.default_listen_address=0.0.0.0`
    * Apply the change and close
    * Start the DBMS
* Get the latest docker image using `docker pull gradvek/app`
* Run the container using `docker run -p 3000:3000 -p 8080:8080 gradvek/app`
* Open your browser to localhost:3000
* Inspect the state of the DB with Browser or Bloom
* When you're done:
  * Find the running container id with `docker ps --filter status=running -q`
  * Stop the container with `docker stop <container_id>`
  * Or if you're using bash, simply run `docker stop $(docker ps --filter status=running -q)`
