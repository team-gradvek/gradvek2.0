#!/usr/bin/make -f

# Variables
FRONTEND_DIR := springdb/frontend
BACKEND_DIR := springdb

FRONTEND_BUILD := npm install
BACKEND_BUILD := mvn clean install

FRONTEND_RUN := npm start
BACKEND_RUN := mvn spring-boot:run

LOCAL_DOCKER_IMAGE_TAG := gradvek/springdb-docker

LOCAL_DOCKER_COMPOSE_FILE := docker-compose-local.yml
DEPLOYED_DOCKER_COMPOSE_FILE := docker-compose.yml

# Default Target Run all for local docker development
.PHONY: local-docker
default: local-docker

# Build Frontend with npm install in springdb/frontend folder
build-frontend:
	$(info Make: Building frontend.)
	@cd $(FRONTEND_DIR) && $(FRONTEND_BUILD)

# Build Backend with maven install in springdb folder
build-backend:
	$(info Make: Building backend.)
	@cd $(BACKEND_DIR) && $(BACKEND_BUILD)
 
# Build local docker image
build-docker:
	$(info Make: Building docker image.)
	@docker build -t $(LOCAL_DOCKER_IMAGE_TAG) .

# Run local gradvek backend on local host
run-backend:
	$(info Make: Running local gradvek backend.)
	@cd $(BACKEND_DIR) && $(BACKEND_RUN)

# Run local gradvek frontend on local host
run-frontend:
	$(info Make: Running local gradvek frontend.)
	@cd $(FRONTEND_DIR) && $(FRONTEND_RUN)

# Run local gradvek back and front end together on local host
run-local:
	$(info Make: Running local gradvek backend and frontend.)
	$(info Make: This requires neo4j running locally.)
	@cd $(BACKEND_DIR) && $(BACKEND_RUN) &
	@cd $(FRONTEND_DIR) && $(FRONTEND_RUN)
# Run local gradvek through docker on local host
run-docker:
	$(info Make: Running docker image.)
	@docker-compose -f $(LOCAL_DOCKER_COMPOSE_FILE) up

# Run deployed gradvek through docker on local host
run-deployed:
	$(info Make: Running deployed docker image.)
	@docker-compose -f $(DEPLOYED_DOCKER_COMPOSE_FILE) up

# Run all for local development in order
local: | build-frontend build-backend run-local

# Run all for local docker development in order
local-docker: | build-frontend build-backend build-docker run-docker
