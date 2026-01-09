#!/bin/bash

# Stop and remove existing container if it exists
docker stop ylca-mysql 2>/dev/null || true
docker rm ylca-mysql 2>/dev/null || true

# Start MySQL container with configuration from application.properties
docker run -d \
  --name ylca-mysql \
  -e MYSQL_ROOT_PASSWORD=root_password \
  -e MYSQL_DATABASE=ylca_blog \
  -e MYSQL_USER=ylca_user \
  -e MYSQL_PASSWORD=ylca_pass \
  -p 3306:3306 \
  -v ylca-mysql-data:/var/lib/mysql \
  mysql:8.0

echo "MySQL container started!"
echo "Container name: ylca-mysql"
echo "Port: 3306"
echo "Database: ylca_blog"
echo "User: ylca_user"
echo "Password: ylca_pass"
echo ""
echo "To view logs: docker logs -f ylca-mysql"
echo "To stop: docker stop ylca-mysql"
echo "To remove: docker rm -f ylca-mysql"
