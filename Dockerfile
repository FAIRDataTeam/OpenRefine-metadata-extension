# Build image
FROM maven:3.6-jdk-11-slim as builder

WORKDIR /usr/src/app/

# Add necessary project parts (exclude in .dockerignore)
ADD . .

# Compile and create package
RUN mvn clean package

# Prepare OpenRefine 3.2 and metadata extension
RUN curl -sSL https://github.com/OpenRefine/OpenRefine/releases/download/3.2/openrefine-linux-3.2.tar.gz | tar xz
RUN tar xzf target/metadata-OpenRefine-3.2.tgz --directory openrefine-3.2/webapp/extensions

# ===================================================================
# Main image
FROM openjdk:11-jre-slim

LABEL maintainer="marek.suchanek@fit.cvut.cz"

# Dependencies for running OpenRefine
RUN apt-get -qq update && apt-get -qq -y install wget

# Copy prepared OpenRefine with extension
COPY --from=builder /usr/src/app/openrefine-3.2 /app

# Prepare workspace volume
VOLUME /data
WORKDIR /data

# Expose OpenRefine's port and run it
EXPOSE 3333
ENTRYPOINT ["/app/refine"]
CMD ["-i", "0.0.0.0", "-d", "/data"]
