# Build image
FROM maven:3.6-jdk-8-slim as builder

# You can specify OpenRefine version by: --build-arg OPENREFINE_VERSION=X.Y
# Possible versions: 3.4.1  (supported, default), 3.3, 3.2
ARG OPENREFINE_VERSION=3.4.1

WORKDIR /usr/src/app/

# Add necessary project parts (exclude in .dockerignore)
COPY . .

# Compile and create package
RUN mvn clean package

# Prepare OpenRefine and metadata extension
RUN curl -sSL https://github.com/OpenRefine/OpenRefine/releases/download/$OPENREFINE_VERSION/openrefine-linux-$OPENREFINE_VERSION.tar.gz | tar xz
RUN mv openrefine-$OPENREFINE_VERSION openrefine
RUN tar xzf target/metadata-OpenRefine-3.4.1.tgz --directory openrefine/webapp/extensions

# ===================================================================
# Main image
FROM openjdk:8-jre-slim

# Dependencies for running OpenRefine
RUN apt-get -qq update \
    && apt-get install -qq -y --no-install-recommends wget=1.20.* \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy prepared OpenRefine with extension
COPY --from=builder /usr/src/app/openrefine /app

# Prepare workspace volume
VOLUME /data
WORKDIR /data

# Expose OpenRefine's port and run it
EXPOSE 3333
ENTRYPOINT ["/app/refine"]
CMD ["-i", "0.0.0.0", "-d", "/data"]
