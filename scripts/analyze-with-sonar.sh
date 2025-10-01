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

echo "🔍 Starting comprehensive SonarQube analysis..."

check_environment() {
    if ! command -v mvn &> /dev/null; then
        echo "❌ Maven is not installed"
        exit 1
    fi
    
    if ! curl -s "$SONAR_HOST_URL/api/system/status" | grep -q "UP"; then
        echo "❌ SonarQube is not running"
        exit 1
    fi
}

run_analysis() {
    echo "✅ SonarQube is running"
    echo "📊 Project: $SONAR_PROJECT_KEY"
    echo "📈 Running comprehensive analysis..."
    
    # Análisis con cobertura y seguridad
    mvn clean verify jacoco:report sonar:sonar \
      -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
      -Dsonar.projectName="FHIR Federation Provider" \
      -Dsonar.host.url="$SONAR_HOST_URL" \
      -Dsonar.token="$SONAR_TOKEN" \
      -Dsonar.coverage.exclusions="**/test/**,**/model/**" \
      -Dsonar.java.coveragePlugin=jacoco \
      -Dsonar.dynamicAnalysis=reuseReports
}

show_results() {
    echo ""
    echo "🎉 Analysis complete!"
    echo "📊 Dashboard: $SONAR_HOST_URL/dashboard?id=$SONAR_PROJECT_KEY"
    echo "🐛 Issues: $SONAR_HOST_URL/project/issues?id=$SONAR_PROJECT_KEY"
    echo "📈 Metrics: $SONAR_HOST_URL/measures?id=$SONAR_PROJECT_KEY"
    echo "🔒 Security: $SONAR_HOST_URL/security_hotspots?id=$SONAR_PROJECT_KEY"
}

check_environment
run_analysis
show_results