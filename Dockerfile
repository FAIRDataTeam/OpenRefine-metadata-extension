# Build image
FROM maven:3.6-jdk-12 as builder

WORKDIR /usr/src/app/

RUN yum -y install unzip

# Add necessary project parts
ADD project-repository project-repository
ADD src src
ADD pom.xml pom.xml

# Compile and create package
RUN mvn clean package

# Prepare OpenRefine 3.2 and metadata extension
RUN curl -sSL https://github.com/OpenRefine/OpenRefine/releases/download/3.2/openrefine-linux-3.2.tar.gz | tar xz
RUN unzip target/metadata-OpenRefine-3.2.zip -d openrefine-3.2/webapp/extensions

# ===================================================================
# Main image
FROM openjdk:12-alpine

LABEL maintainer="marek.suchanek@fit.cvut.cz"

WORKDIR /app

# Dependencies for running OpenRefine
RUN apk add --no-cache bash grep wget

# Copy prepared OpenRefine with extension
COPY --from=builder /usr/src/app/openrefine-3.2 /app

# Prepare workspace volume
VOLUME /data
WORKDIR /data

# Expose OpenRefine's port and run it
EXPOSE 3333
ENTRYPOINT ["/app/refine"]
CMD ["-i", "0.0.0.0", "-d", "/data"]
