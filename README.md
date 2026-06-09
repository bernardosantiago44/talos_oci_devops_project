## How to run the app

### Run the backend 

1. Navigate to backend: `cd MtdrSpring/backend/`
2. Add the environment variables if not already:
   1. DB_URL: `export DB_URL=jdbc:oracle:thin:@...`
   2. DB_PASSWORD: `export DB_PASSWORD=...`
3. Run the backend with `mvn spring-boot:run`

### Run the frontend

1. Open a new terminal and navigate to frontend: `cd MtdrSpring/frontend`
   1. Install package dependencies with `npm install`
2. Run the app with `npm start`

## Sprint 5, module 5 delivery

1. In updated MtdrSpring/backend/pom.xml the generation of documents is below `<!-- Start of delivery for module 5 -->`
2. Github actions for automatic generation of architecture documents is in .github/workflows/docker-image.yaml
3. The generated documents are present in MtdrSpring/doc/arch/generated/level-4
4. Inclusion of level4 diagrams into C4 diagram is done in MtdrSpring/model.dsl
