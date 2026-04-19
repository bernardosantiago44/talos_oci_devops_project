#!/bin/bash
# Helper script to run the application locally with necessary secrets

# 1. Set Database Secrets
export DB_URL="jdbc:oracle:thin:@chatbotbd_medium?TNS_ADMIN=$(pwd)/MtdrSpring/backend/src/main/resources/Wallet_chatbotBD"
export DB_USERNAME="CHATBOT_USER"
read -sp "Enter DB_PASSWORD: " DB_PASSWORD
echo ""
export DB_PASSWORD=$DB_PASSWORD

# 2. Set API Secrets
read -p "Enter TELEGRAM_BOT_TOKEN (leave empty to skip): " TELEGRAM_BOT_TOKEN
export TELEGRAM_BOT_TOKEN=$TELEGRAM_BOT_TOKEN
read -p "Enter TELEGRAM_BOT_NAME (default: local_test_bot): " TELEGRAM_BOT_NAME
export TELEGRAM_BOT_NAME=${TELEGRAM_BOT_NAME:-local_test_bot}
read -p "Enter DEEPSEEK_API_KEY (leave empty to skip): " DEEPSEEK_API_KEY
export DEEPSEEK_API_KEY=$DEEPSEEK_API_KEY

# 3. Export TNS_ADMIN for the Oracle Driver
export TNS_ADMIN="$(pwd)/MtdrSpring/backend/src/main/resources/Wallet_chatbotBD"

echo "Starting Backend..."
cd MtdrSpring/backend
./mvnw spring-boot:run &

echo "Starting Frontend..."
cd src/main/frontend
npm start
