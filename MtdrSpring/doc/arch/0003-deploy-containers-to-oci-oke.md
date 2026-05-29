# 3. Deploy containers to OCI OKE

Date: 2026-05-29

## Status

Accepted

## Context

The application has separate frontend and backend Dockerfiles, Kubernetes manifests, ingress routing, and Terraform configuration for OCI networking, container registry, and Oracle Kubernetes Engine.

## Decision

We will deploy the frontend and backend as containers on OCI OKE. Kubernetes ingress will route `/api` traffic to the backend service and all other web traffic to the frontend service. Terraform will remain responsible for provisioning the OCI platform resources.

## Consequences

The deployment model matches the existing repository automation and OCI target environment. The team must maintain Kubernetes manifests, container images, registry configuration, and Terraform state consistently.
