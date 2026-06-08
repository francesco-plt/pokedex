.PHONY: up down build test it arch check spotless apply-locks

up:
	docker compose --env-file .env.example up -d --build

down:
	docker compose down -v

build:
	./gradlew clean assemble

test:
	./gradlew test

it:
	./gradlew integrationTest

arch:
	./gradlew architectureTest

check:
	./gradlew spotlessCheck test integrationTest architectureTest

spotless:
	./gradlew spotlessApply

apply-locks:
	./gradlew dependencies --write-locks
