#!/bin/bash

echo "Waiting for Debezium Connect to start..."
until curl -s http://localhost:8083/connectors; do
  sleep 5
done

echo "Registering Order Producer Connector..."
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @debezium-config/order-producer-connector.json

echo "Registering Order Inventory Connector..."
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @debezium-config/order-inventory-connector.json

echo "Registering Order Payment Connector..."
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @debezium-config/order-payment-connector.json

echo "Registering Order Notification Connector..."
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @debezium-config/order-notification-connector.json

echo "Connectors registered successfully!"
