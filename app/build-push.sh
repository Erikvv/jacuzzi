set -ex

podman build \
    --file Containerfile \
    --tag ghcr.io/erikvv/jacuzzi \
    ..

podman push ghcr.io/erikvv/jacuzzi
