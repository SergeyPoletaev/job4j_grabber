# job4j_grabber

## О проекте

Этот проект - агрегатор вакансий.

## Описание

* Система запускается по расписанию. Период запуска указывается в настройках - app.properties.
* Первый сайт будет career.habr.com. В нем есть раздел https://career.habr.com/vacancies/java_developer. С ним будет
  идти работа. Программа cчитывает все вакансии относящиеся к Java и записывать их в базу данных.
* Доступ к интерфейсу осуществляется через REST API.

## Расширение

* В проект можно добавить новые сайты без изменения кода.
* В проекте можно сделать параллельный парсинг сайтов.

[![build](https://github.com/SergeyPoletaev/job4j_grabber/workflows/build/badge.svg)](https://github.com/SergeyPoletaev/job4j_grabber/actions)
[![codecov](https://codecov.io/gh/SergeyPoletaev/job4j_grabber/branch/master/graph/badge.svg?token=7YI1XO5JIO)](https://codecov.io/gh/SergeyPoletaev/job4j_grabber)

