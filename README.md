# Золотое яблоко

Gold Apple — учебный backend интернет-магазина косметики. Через HTTP API можно смотреть каталог, получать заказы клиента, создавать и отменять заказ, а также проверять, можно ли его оформить. Суммы указаны в рублях.

## Требования

- JDK 17 или новее
- Docker

Gradle отдельно ставить не нужно: в проекте есть wrapper.

## PostgreSQL

Из корня проекта:

```text
docker compose up -d
```

Остановить контейнер:

```text
docker compose down
```

Удалить контейнер вместе с данными:

```text
docker compose down -v
```

## Запуск приложения

```text
gradlew.bat bootRun
```

На Linux и macOS:

```text
./gradlew bootRun
```

Приложение слушает порт `8080`. Миграции Liquibase применяются при старте.

Витрина магазина: http://localhost:8080/

## Swagger UI

http://localhost:8080/swagger-ui.html

Описание API в JSON: http://localhost:8080/v3/api-docs

## Подключение к PostgreSQL

| Параметр | Значение |
| --- | --- |
| Host | localhost |
| Port | 5432 |
| Database | goldapple |
| User | goldapple |
| Password | goldapple |

Консоль `psql` внутри контейнера:

```text
docker exec -it goldapple-postgres psql -U goldapple -d goldapple
```

## Тесты

```text
gradlew.bat test
```

Тесты поднимают отдельный PostgreSQL через Testcontainers. Для них нужен запущенный Docker. База из `docker compose` для тестов не используется.
