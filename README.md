# Multi-Modulde Ecommerce app

### Agents365-ai_drawio-skill — Hermes Agent Skills & Skill Registries _ Hermes Atlas
![Agents365-ai_drawio-skill — Hermes Agent Skills & Skill Registries _ Hermes Atlas](./src/resources/referenz/Agents365-ai_drawio-skill—Hermes_Agent_Skills_&_Skill_Registries_Hermes_Atlas.jpg)

| Component | Responsibility |
| --- | --- |
| React UI | Frontend |
| API Gateway | Routing, authentication, rate limiting |
| Identity Service | Users, authentication, authorization |
| Product Service | Product catalog |
| Order Service | Orders and checkout |
| Kafka | Asynchronous communication |
| Payment Service | Payments |
| Inventory Service | Stock management |
| Notification Service | Email and notifications |

### Technology Stack
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Apache Kafka
- Redis
- Docker / Docker Compose
- JUnit 5, Mockito, Testcontainers
- Maven
- GitHub Actions
- React

### Project configuration

| Decision | Choice |
| --- | --- | 
| Language | Java 17 | 
| Framework | Spring Boot 4.x | 
| Architecture | Hexagonal | 
| Development | Guided | 
| Database | PostgreSQL | 
| Database strategy | Schema per service | 
| First service | Identity Service | 
| First feature | User Registration | 