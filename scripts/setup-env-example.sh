#!/bin/bash
#
# setup-env.sh - Setup environment variables
#
# Description: This script sets up environment variables for the FHIR Federation Provider project.
# Author: Mauricio Pasten (@mrp4sten)
# Version: 1.0.0
# Created: 2025-10-01
#
# Usage: ./setup-env.sh
# Notes: Run this script before starting the application to ensure all environment variables are set.
#

echo "🔧 Setting up environment variables..."

echo "🔧 Setting up environment variables..."

export SONAR_TOKEN=""
export SONAR_HOST_URL=""
export SONAR_PROJECT_KEY=""

export KEYCLOAK_VERSION="20.0.2"

echo "✅ Environment configured!"
echo "🔑 Project: $SONAR_TOKEN"
echo "🌐 SonarQube URL: $SONAR_HOST_URL"
echo "📊 Project Key: $SONAR_PROJECT_KEY"
echo "🛡️ Keycloak Version: $KEYCLOAK_VERSION"