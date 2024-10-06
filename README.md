# Azure Test Suite for Java

The Azure Test Suite is a testing repository for Azure services in Java.
This repo contains sample implementations of Azure features and services along with a local test implementation of
those services.

# Pre-requisites

Docker must be installed and configured so that the current user can invoke containers. On Linux, this means adding
docker and the user to a docker group.
Java 17+ must be installed.
Maven 3.8+ must be installed.

# Build
To build and test:
```bash
mvn clean verify
```

# Services

## Blob Storage

The blob storage samples demonstrate how to store and retrieve content in an Azure blob storage container. These samples
use an Azurite Test Container to emulate Azure Blob Storage.

Features:
* Creation of a blob container
* Verifying that a container exists
* Writing content to blob storage
* Reading content from blob storage
* Verifying that a blob object exists
