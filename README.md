# 🔐 FHIR Federation Provider for Keycloak

> User Storage SPI for FHIR-based authentication in healthcare systems

## 🎯 Project Status: In Development

### 📊 Current Phase: Foundation

- [x] Project structure and Maven setup
- [x] SonarQube quality pipeline configured
- [ ] Core SPI implementation
- [ ] FHIR API integration
- [ ] OTP authentication flow
- [ ] Testing and validation
- [ ] Production deployment

### 🚧 Current Sprint Focus

#### Implementing UserStorageProvider core functionality

- Basic user lookup from FHIR Patient resources
- Credential validation skeleton
- Configuration model

## 📋 Planned Features

### 🔐 Authentication & Federation

- [ ] User lookup from FHIR Patient resources
- [ ] Patient contact info extraction (email/phone)

### 🏥 FHIR Integration

- [ ] FHIR R4 Patient resource support
- [ ] Google Cloud Healthcare API integration
- [ ] Error handling and fallback strategies

### ⚙️ Configuration & Management

- [ ] Keycloak Admin Console configuration
- [ ] Customizable FHIR endpoint URLs

## 🏗️ Technical Architecture

Keycloak 20.0.2 → FHIR Federation Provider →  GCP Healthcare API
> SPI Layer Business Logic REST Client FHIR Datastore

## 🚀 Quick Start

### Prerequisites

- Keycloak 20.0.2+
- Java 11
- Maven 3.2.5
- Access to FHIR server/datastore

### Development Setup

```bash
# 1. Clone and setup
git clone <repository>
cd keycloak-fhir-federation

# 2. Configure environment
source scripts/setup-env.sh

# 3. Build and test
mvn clean compile
mvn test

# 4. Run quality analysis
export SONAR_TOKEN=your_token_here
./scripts/analyze-with-sonar.sh
```

## Installation

```bash
# Build the plugin
mvn clean package

# Deploy to Keycloak
cp target/fhir-federation-provider-*.jar $KEYCLOAK_HOME/providers/

# Restart Keycloak and configure via Admin Console
```

## 🔧 Development Guide

### Project structure

```bash
src/main/java/com/nicheaim/keycloak/fhir/
├── provider/     # Keycloak SPI implementations
│   ├── FhirUserStorageProvider.java
│   └── FhirUserStorageProviderFactory.java
├── service/      # FHIR client and business logic
├── model/        # Data models and DTOs
└── config/       # Configuration classes
```

### Code Quality

- SonarQube: Integrated for static analysis
- Testing: JUnit 5 + Mockito
- Coverage: Target >80% test coverage
- Standards: Follow Keycloak SPI conventions

## 🐛 Issue Tracking & Progress

### Recent Updates

- ✅ 2024-01-15: Project foundation completed
- ✅ 2024-01-15: SonarQube integration configured
- 🔄 In Progress: UserStorageProvider implementation

### Known Issues

> None currently - project in early development

### Next Milestones

1. Week 1: Basic user lookup functionality
2. Week 2: FHIR API integration
3. Week 3: OTP authentication flow
4. Week 4: Testing and documentation

## License

MIT License - see LICENSE file for details
