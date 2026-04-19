#!/bin/bash
# Helper script to run the application locally with necessary secrets

# 1. Ensure mvnw is executable
chmod +x MtdrSpring/backend/mvnw

# 2. Set Database Secrets
# We use a relative path for TNS_ADMIN to avoid issues with spaces in the absolute path
export TNS_ADMIN="src/main/resources/Wallet_chatbotBD"
export DB_URL="jdbc:oracle:thin:@chatbotbd_medium?TNS_ADMIN=$TNS_ADMIN"
export DB_USERNAME="CHATBOT_USER"

echo "Database Configuration:"
echo "  URL: $DB_URL"
echo "  Username: $DB_USERNAME"
read -sp "Enter DB_PASSWORD: " DB_PASSWORD
echo ""
export DB_PASSWORD=$DB_PASSWORD

# 3. Set API Secrets
read -p "Enter TELEGRAM_BOT_TOKEN (leave empty to skip): " TELEGRAM_BOT_TOKEN
export TELEGRAM_BOT_TOKEN=$TELEGRAM_BOT_TOKEN
read -p "Enter TELEGRAM_BOT_NAME (default: local_test_bot): " TELEGRAM_BOT_NAME
export TELEGRAM_BOT_NAME=${TELEGRAM_BOT_NAME:-local_test_bot}
read -p "Enter DEEPSEEK_API_KEY (leave empty to skip): " DEEPSEEK_API_KEY
export DEEPSEEK_API_KEY=$DEEPSEEK_API_KEY

echo "Starting Backend in the background..."
cd MtdrSpring/backend
# Redirect output to backend.log for easier debugging
./mvnw spring-boot:run -DskipTests > backend.log 2>&1 &
BACKEND_PID=$!

echo "Waiting for Backend to start on port 8080..."
# Wait up to 60 seconds for the backend to respond
for i in {1..60}; do
    if curl -s http://localhost:8080 > /dev/null; then
        echo "Backend is UP!"
        break
    fi
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        echo "ERROR: Backend process failed to start. Check MtdrSpring/backend/backend.log for details."
        exit 1
    fi
    echo -n "."
    sleep 2
done

echo "Starting Frontend..."
cd src/main/frontend
npm start
