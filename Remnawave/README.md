# Chumpy в Remnawave

Скачайте [subscription-page.json](subscription-page.json). В нём собраны стандартные приложения Remnawave, а Chumpy стоит первым на Android и Android TV.

1. В панели Remnawave откройте **Подписка → Subscription Page**.

   <img src="screenshots/01-subscription-page.png" alt="Раздел Subscription Page" width="360">

2. Один раз нажмите на строку нужной конфигурации, чтобы открыть редактор.

   <img src="screenshots/02-open-configuration.png" alt="Список конфигураций" width="760">

3. Нажмите кнопку со стрелкой вверх **«Импортировать конфигурацию»**, выберите скачанный файл и нажмите **«Полный импорт»**. Этот режим заменяет открытую конфигурацию. Если у вас уже настроены свои приложения или оформление, сначала экспортируйте текущую конфигурацию.

   <img src="screenshots/03-import-configuration.png" alt="Кнопка импорта" width="600">

4. Нажмите **«Сохранить»**, затем выполните на сервере, где запущена страница подписки:

   <img src="screenshots/04-save.png" alt="Кнопка «Сохранить»" width="320">

   ```sh
   docker restart remnawave-subscription-page
   ```

Обновите страницу подписки и нажмите **«Открыть в Chumpy»**. Подтвердите добавление подписки в приложении.
