# Прилёты-улёты

Основная идея - подсчитать (примерное) количество людей улетающих из РФ и прилетающих в РФ (пока данные только по Пулково :D)

Приложение состоит из:

1. Скрапперов которые отправляют сырые данные в топики кафки - папка scrappers
2. Приложения, которое фильтрует и агрегирует данные, с помощью Kafka Streams - папка aggregation
3. (в перспективе) Фронтенда, который будет брать готовые данные из кафки и, возможно, управлять какой-то логикой

<details>
  <summary>Картинка с топологией</summary>
  
  ![canvas](https://user-images.githubusercontent.com/1942903/230631377-77eb0fd0-cdf9-468d-8c1b-d5c6c77e9f79.png)
</details>

Топики:

1. raw-departures и raw-arrivals - сырые данные о прилётах-вылетах с ключом по номеру рейса и таймстампу
2. aircraft-capacities - "таблица" со средним числом пассажиров по типу самолёта (для загрузки в GlobalKTable)
3. missing-aircraft-types - список кодов ICAO, для которых вместимость пока не определена
4. total-passenger-count - топик с проагрегированными значениями общего числа пассажиров с отдельными ключами для вылетов и прилётов
5. departures-by-country - общее число пасажиров по странам назначения для вылетов
6. arrivals-by-country - общее число пассажиров по странам отправления для прилётов
7. aircraft-type-counts - подсчет типов самолётов для вылетов и прилётов

Все топики c `cleanup.policy=compact`

## TODO

1. Фронтенд
3. Разместить всё в докере/кубере
4. Больше скрапперов
