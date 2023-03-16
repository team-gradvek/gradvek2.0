# Troubleshooting Errors

## Error: Image not compatible with M1 Macs.
```
➜  gradvek2.0 git:(master) docker pull sheelyn/ns_gradvek_test
Using default tag: latest
latest: Pulling from sheelyn/ns_gradvek_test
no matching manifest for linux/arm64/v8 in the manifest list entries
```

Inspecting the image shows the platforms:

```
docker buildx imagetools inspect sheelyn/ns_gradvek_test:latest
Name:      docker.io/sheelyn/ns_gradvek_test:latest
MediaType: application/vnd.oci.image.index.v1+json
Digest:    sha256:f7e55d87c73cde2733fcad434d68ee756ad9b0c869c91dc61ff2ddec8d203c04
           
Manifests: 
  Name:        docker.io/sheelyn/ns_gradvek_test:latest@sha256:0b45e419a4e16f43e9a8efec22180b6cda9ef617a87e1258f19f02c9480af8fd
  MediaType:   application/vnd.oci.image.manifest.v1+json
  Platform:    linux/amd64
```

So we have two options now:

1. We run with the platform we need specified (manual)
```
docker pull --platform linux/x86_64 sheelyn/ns_gradvek_test
```
OR

2. We rebuild the image to be cross-platform (one-time/when we update image)

```
docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 -t davida26/gradvek2.0:latest --push .
```

This command was not used however, as I had an existing buildx configuration created. See below for all platforms supported.

```
Name:      cross-platform-builder0
Endpoint:  unix:///var/run/docker.sock
Status:    running
Buildkit:  v0.11.4
Platforms: linux/arm64, linux/amd64, linux/amd64/v2, linux/riscv64, linux/ppc64le, linux/s390x, linux/386, linux/mips64le, linux/mips64, linux/arm/v7, linux/arm/v6
```

@NOTE: to get this to publish to DockerHub I had to add the secrets to the repo on Github. Settings > Secrets and Variables > Actions


## Error: /node/npm: line 20: /node/node: cannot execute binary file: Exec format error

Running this in debug mode with 

```
docker-compose logs --follow
```

shows that `npm` is not installed

So we can modify the Dockerfile to install it

```
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y nodejs \
    npm 
```

and we finally tell docker-compose.yml to not forget to run the context of Dockerfile aka the build step

```
    build:
      context: .
```




