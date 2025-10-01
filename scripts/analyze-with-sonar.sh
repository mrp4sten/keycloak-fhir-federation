#!/bin/bash
#
# analyze-with-sonar.sh - Analyze project with SonarQube
#
# Description: This script analyzes the FHIR Federation Provider project with SonarQube.
# Author: Mauricio Pasten (@mrp4sten)
# Version: 1.0.0
# Created: 2025-10-01
#
# Usage: ./analyze-with-sonar.sh
#
set -euo pipefail

echo "🔍 Starting SonarQube analysis..."

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    exit 1
fi

if ! curl -s "$SONAR_HOST_URL/api/system/status" | grep -q "UP"; then
    echo "❌ SonarQube is not running at $SONAR_HOST_URL"
    echo "💡 Start it with: cd ~/dev-lab/tools/sonarqube && ./sonarqube-manager.sh start"
    exit 1
fi

echo "✅ SonarQube is running"
echo "📊 Project: $SONAR_PROJECT_KEY"

mvn clean verify sonar:sonar \
  -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
  -Dsonar.projectName="$SONAR_PROJECT_KEY" \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.token="$SONAR_TOKEN"

echo "🎉 Analysis complete!"
echo "📈 View results: $SONAR_HOST_URL/dashboard?id=$SONAR_PROJECT_KEY"