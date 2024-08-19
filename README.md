# bot-api-demo
Демонстрация интеграции YuChat через Bot API

## Технические требования для запуска приложения

- java 21

## Быстрый старт
```bash
git clone git@github.com:unison-messenger/bot-api-demo.git
cd bot-api-demo.git
./gradlew clean build
./gradlew bootRun
```

## Описание репозитория

Open API спецификация для работы с YuChat Bot API находится в файле [`openapi.yaml`](spec/openapi.yaml). 
HTTP клиет для работы с API генерируется автоматически при сборке проекта. Пример работы с клиентом находится в 
[YuchatHttpClient](app/src/main/java/ai/yuchat/bot/client/YuchatHttpClient.java).

Конфигурация приложения находится в файле [application.yml](app/src/main/resources/application.yml).



