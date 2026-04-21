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
