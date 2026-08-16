# ============================================================
# BookStore Makefile - No Colors/Emoji
# ============================================================

PROJECT_NAME = bookstore
DOCKER_COMPOSE = docker-compose
MAVEN = mvn

# ============================================================
# HELP
# ============================================================
.PHONY: help
help:
	@echo ""
	@echo "BookStore - Makefile Commands"
	@echo "=============================="
	@echo ""
	@echo "DOCKER COMMANDS"
	@echo "  make up          - Build and start all containers"
	@echo "  make down        - Stop and remove all containers"
	@echo "  make logs        - View all logs"
	@echo "  make logs-app    - View app logs only"
	@echo "  make logs-mysql  - View mysql logs only"
	@echo "  make logs-redis  - View redis logs only"
	@echo "  make ps          - Show container status"
	@echo "  make shell       - Open shell in app container"
	@echo "  make restart     - Restart all containers"
	@echo "  make build       - Rebuild images"
	@echo "  make clean       - Stop and remove everything (including volumes)"
	@echo ""
	@echo "TEST COMMANDS"
	@echo "  make test        - Run all tests"
	@echo "  make test-unit   - Run unit tests only"
	@echo "  make test-integration - Run integration tests only"
	@echo ""
	@echo "INFO COMMANDS"
	@echo "  make status      - Show detailed status"
	@echo "  make stats       - Show container stats"
	@echo "  make health      - Check health status"
	@echo ""

# ============================================================
# DOCKER COMMANDS
# ============================================================

.PHONY: up
up:
	echo "Starting BookStore..."
	$(DOCKER_COMPOSE) up -d --build
	echo "BookStore started!"
	echo "App: http://localhost:8082"
	echo "phpMyAdmin: http://localhost:8081"
	echo "RabbitMQ: http://localhost:15672"

.PHONY: down
down:
	echo "Stopping BookStore..."
	$(DOCKER_COMPOSE) down
	echo "BookStore stopped"

.PHONY: logs
logs:
	$(DOCKER_COMPOSE) logs -f

.PHONY: logs-app
logs-app:
	$(DOCKER_COMPOSE) logs -f app

.PHONY: logs-mysql
logs-mysql:
	$(DOCKER_COMPOSE) logs -f mysql

.PHONY: logs-redis
logs-redis:
	$(DOCKER_COMPOSE) logs -f redis

.PHONY: logs-rabbitmq
logs-rabbitmq:
	$(DOCKER_COMPOSE) logs -f rabbitmq

.PHONY: ps
ps:
	$(DOCKER_COMPOSE) ps

.PHONY: shell
shell:
	$(DOCKER_COMPOSE) exec app sh

.PHONY: shell-mysql
shell-mysql:
	$(DOCKER_COMPOSE) exec mysql bash

.PHONY: shell-redis
shell-redis:
	$(DOCKER_COMPOSE) exec redis sh

.PHONY: restart
restart:
	echo "Restarting BookStore..."
	$(DOCKER_COMPOSE) restart
	echo "BookStore restarted"

.PHONY: build
build:
	echo "Building images..."
	$(DOCKER_COMPOSE) build --no-cache
	echo "Build completed"

.PHONY: clean
clean:
	echo "Cleaning up BookStore..."
	$(DOCKER_COMPOSE) down -v
	echo "Cleaned up"

.PHONY: clean-all
clean-all: clean
	echo "Removing images..."
	docker rmi $(PROJECT_NAME)-app || true
	echo "All cleaned up"

# ============================================================
# TEST COMMANDS
# ============================================================

.PHONY: test
test:
	echo "Running all tests..."
	$(MAVEN) clean test
	echo "Tests completed"

.PHONY: test-unit
test-unit:
	echo "Running unit tests..."
	$(MAVEN) test -Dtest=*Test
	echo "Unit tests completed"

.PHONY: test-integration
test-integration:
	echo "Running integration tests..."
	$(MAVEN) test -Dtest=*IntegrationTest
	echo "Integration tests completed"

.PHONY: test-coverage
test-coverage:
	echo "Running tests with coverage..."
	$(MAVEN) clean test jacoco:report
	echo "Coverage report generated at target/site/jacoco/index.html"

# ============================================================
# INFO COMMANDS
# ============================================================

.PHONY: status
status:
	echo "BookStore Status"
	echo "================"
	echo ""
	echo "Containers:"
	$(DOCKER_COMPOSE) ps
	echo ""
	echo "Images:"
	docker images | grep $(PROJECT_NAME) || echo "No images found"

.PHONY: stats
stats:
	$(DOCKER_COMPOSE) stats --no-stream

.PHONY: health
health:
	echo "Health Check"
	echo "============="
	@for container in $$(docker ps --format '{{.Names}}' | grep bookstore); do \
		status=$$(docker inspect $$container --format='{{.State.Health.Status}}'); \
		echo "$$container: $$status"; \
	done

# ============================================================
# DEVELOPMENT
# ============================================================

.PHONY: dev
dev:
	echo "Starting development mode..."
	$(DOCKER_COMPOSE) up -d --build
	echo "Development mode started"

.PHONY: migrate
migrate:
	echo "Running Flyway migrations..."
	$(DOCKER_COMPOSE) exec app java -jar app.jar --spring.flyway.enabled=true
	echo "Migrations completed"

# ============================================================
# DEFAULT
# ============================================================
.DEFAULT_GOAL := help