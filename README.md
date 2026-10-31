# AntiEsp

## Русский

AntiEsp — плагин для Paper 1.21.11, который ограничивает передачу клиенту данных об игроках за непрозрачными блоками.

### Возможности

- скрывает игроков и их экипировку за стенами;
- мгновенно возвращает игрока при появлении прямой видимости;
- быстрее скрывает невидимых игроков;
- подменяет клиентское название передаваемой брони на `@freakdev`, не изменяя реальные предметы;
- поддерживает право `antiesp.bypass` для исключения отдельных наблюдателей;
- использует PacketEvents для обработки пакетов экипировки.

### Требования

- Java 21;
- Paper 1.21.11;
- PacketEvents 2.14.0 или новее.

### Установка

1. Установите PacketEvents в папку `plugins` сервера.
2. Поместите собранный `AntiEsp` JAR в папку `plugins`.
3. Перезапустите сервер.

### Сборка

```bash
./gradlew build
```

Готовый файл появится в `build/libs`.

## English

AntiEsp is a Paper 1.21.11 plugin that limits the player data sent to clients when a target is behind opaque blocks.

### Features

- hides players and their equipment behind walls;
- restores a player immediately when direct line of sight is available;
- hides invisible players more aggressively;
- replaces the client-side display name of transmitted armor with `@freakdev` without modifying the real items;
- provides the `antiesp.bypass` permission for exempt viewers;
- uses PacketEvents for equipment packet processing.

### Requirements

- Java 21;
- Paper 1.21.11;
- PacketEvents 2.14.0 or newer.

### Installation

1. Install PacketEvents into the server's `plugins` directory.
2. Place the built `AntiEsp` JAR into the `plugins` directory.
3. Restart the server.

### Building

```bash
./gradlew build
```

The resulting JAR will be available in `build/libs`.
