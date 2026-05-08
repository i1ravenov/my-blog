# My Blog

Учебное приложение блога на Spring MVC с React-фронтендом.

## Архитектура

| Компонент | Технологии | Порт |
|-----------|-----------|------|
| Бэкенд | Spring MVC 6, JdbcTemplate, H2, Tomcat 10.1, Java 21 | 8080 |
| Фронтенд | React (pre-built), nginx | 80 |

Данные хранятся в H2 in-memory базе данных и сбрасываются при перезапуске контейнера. Загруженные изображения сохраняются в Docker volume `uploads_data`.

## Требования

- Docker
- Docker Compose

## Запуск

```bash
docker compose up --build
```

После запуска приложение доступно по адресу: http://localhost

Бэкенд API доступен напрямую на http://localhost:8080/api

## Структура проекта

```
my-blog/
├── docker-compose.yaml
├── my-blog-back-app/       # Spring MVC бэкенд
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/       # Исходный код
│           └── resources/
│               ├── application.properties
│               ├── schema.sql      # DDL + тестовые данные
│               └── static/
│                   └── default.jpeg  # Изображение по умолчанию
└── my-blog-front-app/      # React фронтенд (pre-built)
    ├── Dockerfile
    ├── nginx.conf
    └── dist/
```

## API

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/api/posts` | Список постов |
| POST | `/api/posts` | Создать пост |
| GET | `/api/posts/{id}` | Получить пост |
| PUT | `/api/posts/{id}` | Обновить пост |
| DELETE | `/api/posts/{id}` | Удалить пост |
| GET | `/api/posts/{id}/image` | Получить изображение поста |
| PUT | `/api/posts/{id}/image` | Загрузить изображение поста |
| GET | `/api/posts/{id}/comments` | Список комментариев |
| POST | `/api/posts/{id}/comments` | Добавить комментарий |
| PUT | `/api/posts/{postId}/comments/{id}` | Обновить комментарий |
| DELETE | `/api/posts/{postId}/comments/{id}` | Удалить комментарий |

## Начальные данные

При старте `schema.sql` автоматически создаёт таблицы и наполняет их тестовыми данными: 3 поста и 2 комментария к первому посту.

## Пересборка отдельного сервиса

```bash
# Только бэкенд
docker compose build my-blog-back-app && docker compose up -d my-blog-back-app

# Только фронтенд
docker compose build my-blog-front-app && docker compose up -d my-blog-front-app
```
