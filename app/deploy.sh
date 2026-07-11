#!/bin/bash

ssh odroid /bin/bash << EOF
    podman pull ghcr.io/erikvv/jacuzzi:latest \
        && systemctl restart jacuzzi
EOF
