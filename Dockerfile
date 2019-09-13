# Build image
FROM maven:3.6-jdk-12 as build-env

WORKDIR /usr/src/app/

# Copy necessary project parts
COPY project-repository project-repository
COPY src src
COPY pom.xml pom.xml

# Compile and create package
RUN mvn clean package

# ===================================================================
# Main image
FROM openjdk:12-alpine

LABEL maintainer="marek.suchanek@fit.cvut.cz"

# OpenRefine 3.2 and metadata extension
ENV OR_URL https://github.com/OpenRefine/OpenRefine/releases/download/3.2/openrefine-linux-3.2.tar.gz
ENV METADATA_ZIP metadata-OpenRefine-3.2.zip
ENV EXTENSIONS_DIR /app/webapp/extensions

WORKDIR /app

# Get OpenRefine
RUN apk add --no-cache bash curl grep tar unzip
RUN curl -sSL ${OR_URL} | tar xz --strip 1

# Copy extension
COPY --from=build-env /usr/src/app/target/${METADATA_ZIP} .
RUN unzip ${METADATA_ZIP} -d ${EXTENSIONS_DIR}

# Prepare workspace volume
VOLUME /data
WORKDIR /data

# Expose OpenRefine's port and run it
EXPOSE 3333

ENTRYPOINT ["/app/refine"]
CMD ["-i", "0.0.0.0", "-d", "/data"]
