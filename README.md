# Spotify :notes:

## Въведение

[`Spotify`](https://www.spotify.com/) е платформа за `stream`-ване на музика, която предоставя на потребителите достъп до милиони песни на изпълнители от цял свят.

> `Stream`-ването е метод за предаване на данни, използван обикновено за мултимедийни файлове. При него възпроизвеждането на съдържанието върху устройството на потребителя започва още с достъпването му, без да се налага то отначало да бъде изтеглено изцяло като файл и после да се стартира в подходящ плеър. Предаването на данните протича едновременно с възпроизвеждането, затова е необходима постоянна мрежова свързаност.

## Условие на проекта

Създайте приложение по подобие на `Spotify`, състоящо се от две части - сървър и клиент.

## Използвани технологии и библиотеки

- `Java` version 19
- `JUnit` version 5.8.1
- `Mockito` version 5.1.1
- `Google GSON` version 2.10.1

## Основни функционалности

### **Spotify Server**

Предоставя следните функционалности на клиента:
- регистриране в платформата чрез **email** и **парола** (потребителите се съхраняват във файл)
- login в платформата чрез **email** и **парола**
- съхраняване на набор от песни, достъпни на потребителите за слушане (песните се съхраняват във файл)
- търсене на песни
- разглеждане на статистика на най-слушаните песни от потребителите
- създаване и изтриване на плейлисти (плейлистите се съхраняват във файлове)
- добавяне и изтриване на песни от плейлисти
- извличане на информация за даден плейлист
- `stream`-ване на песни
- `stream`-ване на плейлисти (последователност от песни)

### **Spotify Client**

`Spotify` клиентът представлява `command line interface` със следните команди (в азбучен ред):

```
add-song-to <name_of_the_playlist> <title_of_the_song> : Add <song> to <playlist>
create-playlist <name_of_the_playlist> : Create <playlist>. The title of the playlist must be one-word and is case-sensitive
delete-playlist <name_of_the_playlist> : Delete <playlist>. The titles of the playlists are case-sensitive
disconnect : Disconnect from Spotify
help : List the current info
login <email> <password> : Log in Spotify
logout : Log out of Spotify
play <title_of_the_song> : Start playing the <song>
play-playlist <name_of_the_playlist> : Start playing all the songs of the <playlist> one after another. The title of the playlists are case-sensitive. If you want to stop or skip a song, you can stop it with the relevant command. In order to stop the playlist, you have to stop each song in it.
register <email> <password> : Registration in Spotify
remove-song-from <name_of_the_playlist> <title_of_the_song> : Remove <song> from <playlist>. The titles of the playlist are case-sensitive.
search <words> : Retrieve all songs whose title or artist contain all the words
show-playlist <name_of_the_playlist> : List the content of the <playlist>. The titles of the  playlists are case-sensitive.
show-playlists : List the titles of all created playlists
stop : Stop playing the current song
top <number> : Retrieve list of the top <number> most listened songs
```

## Организация на проекта

Структурата на кода по директории и пакети е следната:

```
src
└── bg.sofia.uni.fmi.mjt.spotify
    ├── client
    │   ├── Client.java
    ├── server
    │       ├── command
    │   	├── Command.java
    │   	├── CommandExecutor.java
    │   	├── CommandExtractor.java
    │   	└── CommandName.java
    │       ├── exceptions
    │   	├── EmailAlreadyRegisteredException.java
    │   	├── IODatabaseException.java
    │   	├── LoggerNotCreatedSuccessfullyException.java
    │   	├── NoSongPlayingException.java
    │   	├── NoSongsInPlaylistException.java
    │   	├── NoSuchPlaylistException.java
    │   	├── NoSuchSongException.java
    │   	├── NotValidEmailFormatException.java
    │   	├── PlaylistAlreadyExistException.java
    │   	├── PlaylistNotEmptyException.java
    │   	├── SongAlreadyInPlaylistException.java
    │   	├── SongIsAlreadyPlayingException.java
    │   	├── SpotifyException.java
    │   	├── UserAlreadyLoggedException.java
    │   	├── UserNotFoundException.java
    │   	└── UserNotLoggedException.java
    │       ├── logger
    │   	└── SpotifyLogger.java
    │       ├── login
    │   	├── AuthenticationService.java
    │   	├── SHAAlgorithm.java
    │   	└── User.java
    │       ├── player
    │   	├── PlayPlaylistThread.java
    │   	└── PlaySongThread.java
    │       ├── storage
    │   	├── Playlist.java
    │   	├── Song.java
    │   	└── SongEntity.java
    ├── Server.java
    ├── ServerReply.java
    └── StreamingPlatform.java
test
└── bg.sofia.uni.fmi.mjt.spotify.server
    ├── command
    │   ├── CommandExecutorTest.java
    │   └── CommandExtractorTest.java
    ├── logger
    │   └── SpotifyLoggerTest.java
    ├── login
    │   ├── AuthenticationServiceTest.java
    │   └── SHAAlgorithmTest.java
    ├── player
    │   ├── PlayPlaylistThreadTest.java
    │   └── PlaySongThreadTest.java
    └── StreamingPlatformTest.java
data
    ├── authentication
    │   └── RegisteredUsersList.txt
    ├── music
    │   └── (...).wav
    ├── PlaylistList.json
    ├── SongsList.json
    └── SpotifyLogger.log
```

## Предизвикателства

Придържането към принципите за чист код беше водещо по време на разработването на проекта. Голяма част от литералите, както и всякакви "magic numbers" са изнесени като константи. Методите в класовете са с лимитирана дължина, както и изнесени в отделни private методи, където се налага. Всякакви отговори под формата на съобщения от сървъра са изнесени в enum клас. Част от правилата са проверени от инструмента за статичен анализ на кода, CheckStyle, с използвано, конкретно създаденo за MJT курса, множество от правила, които да напомнят за правилно форматиране и други правила за чист код.  

Друг ключов момент от работата по проекта беше неговото тестване с JUnit и Mockito. Постигането на 84% code coverage беше предизвикателство, което несъмнено си заслужаваше, тъй като това до голяма степен верифицира правилното функциониране на кода. От друга страна, работата по създаването на тестове помогна за намирането и изчистването на бъгове в кода. Тестването на многонишков код изискваше използването на редица функции на Mockito, което беше поучително по отношение на усвояването на една нова, сравнително от скоро позната за мен, библиотека.    

## Демонстрация

<p align="center">
<img width="860px" src="https://github.com/sdvelev/Spotify/blob/main/resources/screen01.png" alt="screen01">
</p>

<p align="center">
<img width="860px" src="https://github.com/sdvelev/Spotify/blob/main/resources/screen02.png" alt="screen01">
</p>

<p align="center">
<img width="860px" src="https://github.com/sdvelev/Spotify/blob/main/resources/screen03.png" alt="screen01">
</p>

<p align="center">
<img width="860px" src="https://github.com/sdvelev/Spotify/blob/main/resources/screen04.png" alt="screen01">
</p>


## Забележки и други особености

1. За да можете да се изпълняват песни от `Spotify` клиента е използвано API-то `javax.sound.sampled`.
2. `javax.sound.sampled` работи само с файлове във [wav](https://en.wikipedia.org/wiki/WAV) формат, затова всички песни, които има на сървъра, трябва да са **.wav**
3. `javax.sound.sampled` предоставя два начина за възпроизвеждане на музика - чрез `Clip` и `SourceDataLine`. `Clip` се използва когато имаме `non-real-time` музикални данни (файл), които могат да бъдат предварително заредени в паметта.
`SourceDataLine` се използва за `stream`-ване на данни, като например голям музикален файл, който не може да се зареди в паметта наведнъж, или за данни, които предварително не са известни. (за повече информация [тук](https://docs.oracle.com/javase/tutorial/sound/playing.html))

    За целите на проекта, е използван `SourceDataLine`.
	1. За да създадем [`SourceDataLine`](https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/SourceDataLine.html) първо трябва да знаем конкретния формат на данните, които ще получаваме по мрежата. Това става с класа [`AudioFormat`](https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/AudioFormat.html). За да успеем да възпроизведем дадена песен при клиента, трябва предварително да знаем какъв е този формат.
	
	2. Преди сървърът да започне да ни `stream`-ва  песента, той трябва да ни даде(прати) информация за формата на данните. Класът `AudioFormat` не е `Serializable`, т.е не можем да изпратим на клиента директно обект от тип `AudioFormat`.
	
	3. За да вземем формата на песента на сървъра, можем да използваме следния код:
        ```java
        AudioFormat audioFormat = AudioSystem.getAudioInputStream(new File(song)).getFormat();
        ```
	
	4. Данните, които са необходими на клиента, са всички полета от конструктора на `AudioFormat`. Те могат да бъдат достъпени чрез съответните `getter` методи:
        ```java
        AudioFormat(AudioFormat.Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian)
        ```
	
	5. След като сървърът е изпратил формата на данните, клиентът вече е готов да създаде съответния `SourceDataLine` обект, чрез който ще се възпроизвежда песента.
        ```java
        Encoding encoding = ...;
        int sampleRate = ...;
        ...
        AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
        dataLine.open();
        dataLine.start(); // Имайте предвид, че SourceDataLine.start() пуска нова нишка. За повече информация, може да проверите имплементацията.
        ```
    6. За да запишем данни в `SourceDataLine` обекта (данните, които искаме да възпроизведем) използваме следния метод:
	    ```java
	    dataLine.write(byte[] b, int off, int len);
	    ```
    
    7. За тестови цели, можем да си пуснем песен (non-real-time) със следния код:
    
        ```java
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File("<music>.wav"));
        SourceDataLine dataLine = AudioSystem.getSourceDataLine(stream.getFormat());
        dataLine.open();
        dataLine.start();
        
        while (true);
        ```
  4. Валидиране на командите по подходящ начин.
  5. Хврълените `exception`-и се записват във файл (logger).

