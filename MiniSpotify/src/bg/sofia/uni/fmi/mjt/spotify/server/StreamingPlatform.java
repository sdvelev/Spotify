package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSongPlayingException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSongsInPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PlaylistAlreadyExistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PlaylistNotEmptyException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SongAlreadyInPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.SpotifyException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.spotify.server.logger.SpotifyLogger;
import bg.sofia.uni.fmi.mjt.spotify.server.login.User;
import bg.sofia.uni.fmi.mjt.spotify.server.player.PlayPlaylistThread;
import bg.sofia.uni.fmi.mjt.spotify.server.player.PlaySongThread;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.storage.SongEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StreamingPlatform {

    private static final String INTERVAL_REGEX = " ";
    private static final String UNDERSCORE = "_";
    private static final String EMPTY_STRING = "";
    private static final String PLAYLISTS_LIST_PATH = "data" + File.separator + "PlaylistsList.json";
    private static final String SONGS_LIST_PATH = "data" + File.separator + "SongsList.json";
    private static final String NEGATIVE_N_ARGUMENT = "The provided argument cannot be negative.";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Set<SongEntity> songs;
    private User user;
    private Map<String, Set<Playlist>> playlists;
    private final Map<SelectionKey, PlaySongThread> alreadyRunning;
    private final Set<SelectionKey> alreadyLogged;

    private Reader playlistsReader;
    private Writer playlistsWriter;
    private Reader songsReader;
    private Writer songsWriter;
    private SpotifyLogger spotifyLogger;

    public StreamingPlatform(SpotifyLogger spotifyLogger) throws IODatabaseException {
        this.playlists = new LinkedHashMap<>();
        this.alreadyRunning = new ConcurrentHashMap<>();
        this.alreadyLogged = new HashSet<>();
        this.spotifyLogger = spotifyLogger;

        this.user = new User(EMPTY_STRING, EMPTY_STRING);

        readSongs();
        readPlaylists();
    }

    public StreamingPlatform(Reader playlistsReader, Writer playlistsWriter, Reader songsReader, Writer songsWriter,
                             Set<SelectionKey> alreadyLogged, Map<SelectionKey, PlaySongThread> alreadyRunning)
        throws IODatabaseException {
        this.playlists = new LinkedHashMap<>();
        this.playlistsReader = playlistsReader;
        this.playlistsWriter = playlistsWriter;
        this.songsReader = songsReader;
        this.songsWriter = songsWriter;

        this.user = new User(EMPTY_STRING, EMPTY_STRING);

        this.alreadyLogged = alreadyLogged;
        this.alreadyRunning = alreadyRunning;

        readSongs();
        readPlaylists();
    }

    public List<SongEntity> getTopNMostListenedSongs(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(NEGATIVE_N_ARGUMENT);
        }

        if (n == 0) {
            return new ArrayList<>();
        }

        return songs.stream()
            .sorted((content1, content2) -> Integer.compare(content2.getListeningTimes(),
                content1.getListeningTimes()))
            .limit(n)
            .toList();
    }

    public List<SongEntity> searchSongs(String word) {
        Objects.requireNonNull(word, "The given word cannot be null.");

        List<SongEntity> result = new ArrayList<>();
        for (SongEntity currentSongEntity : songs) {
            boolean contains = true;
            for (String currentWord : word.split(INTERVAL_REGEX)) {
                if (!currentSongEntity.getSong().getTitle().toLowerCase().contains(currentWord.toLowerCase()) &&
                    !currentSongEntity.getSong().getArtist().toLowerCase().contains(currentWord.toLowerCase())) {
                    contains = false;
                    break;
                }
            }

            if (contains) {
                result.add(currentSongEntity);
            }
        }

        return result;
    }

    public synchronized void createPlaylist(String playlistTitle, SelectionKey selectionKey) throws SpotifyException {

        Objects.requireNonNull(playlistTitle, "The given playlist title cannot be null");
        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        Playlist toAdd = new Playlist(user.getEmail(), playlistTitle);
        if (playlists.containsKey(user.getEmail())) {

            if (playlists.get(user.getEmail()).contains(toAdd)) {
                throw new PlaylistAlreadyExistException(ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply());
            }

            playlists.get(user.getEmail()).add(toAdd);
        } else {

            playlists.put(user.getEmail(), new LinkedHashSet<>());
            playlists.get(user.getEmail()).add(toAdd);
        }

        writePlaylists();
    }

    public void deletePlaylist(String playlistTitle, SelectionKey selectionKey) throws SpotifyException {

        Objects.requireNonNull(playlistTitle, "The given playlist title cannot be null.");
        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = user.getEmail();
        if (!playlists.containsKey(emailCreator) || !playlists.get(emailCreator)
            .contains(new Playlist(emailCreator, playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        removePlaylistFromPlaylists(emailCreator, playlistTitle);
        writePlaylists();
    }

    public void addSongToPlaylist(String playlistTitle, String songTitle, SelectionKey selectionKey)
        throws SpotifyException {

        Objects.requireNonNull(playlistTitle, "The given playlist title cannot be null.");
        Objects.requireNonNull(songTitle, "The given song title cannot be null.");
        Objects.requireNonNull(selectionKey, "The given selection key cannot be null");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply());
        }

        Song songToAdd = isFound(songTitle);

        if (songToAdd == null) {
            throw new NoSuchSongException(ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply());
        }

        String emailCreator = user.getEmail();
        if (!playlists.containsKey(emailCreator) || !playlists.get(emailCreator)
            .contains(new Playlist(emailCreator, playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        addSongInPlaylist(emailCreator, playlistTitle, songToAdd);
    }

    public void removeSongFromPlaylist(String playlistTitle, String songTitle, SelectionKey selectionKey)
        throws SpotifyException {

        Objects.requireNonNull(playlistTitle, "The given playlist title cannot be null.");
        Objects.requireNonNull(songTitle, "The given playlist title cannot be null");
        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.REMOVE_SONG_FROM_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = user.getEmail();
        if (!playlists.containsKey(emailCreator) ||
            !playlists.get(emailCreator).contains(new Playlist(emailCreator, playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        removeSongFromPlaylists(emailCreator, playlistTitle, songTitle);
    }

    public Playlist showPlaylist(String playlistTitle, SelectionKey selectionKey)
        throws UserNotLoggedException, NoSuchPlaylistException {

        Objects.requireNonNull(playlistTitle, "The given playlist title cannot be null.");
        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = user.getEmail();
        if (!playlists.containsKey(emailCreator) || !playlists.get(emailCreator)
            .contains(new Playlist(emailCreator, playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        Set<Playlist> allPlaylists =  playlists.get(emailCreator);
        for (Playlist currentPlaylist : allPlaylists) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {
                return currentPlaylist;
            }
        }
        return new Playlist();
    }

    public List<String> showPlaylists(SelectionKey selectionKey)
        throws UserNotLoggedException {

        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = user.getEmail();

        if (!playlists.containsKey(emailCreator)) {

            return new ArrayList<>();
        }

        return playlists.get(emailCreator).stream()
            .map(Playlist::getTitle)
            .toList();
    }

    public void playPlaylist(String playlistTitle, SelectionKey selectionKey) throws SpotifyException {

        Objects.requireNonNull(playlistTitle, "The given playlist title cannot be null.");
        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply());
        }

        if (!playlists.containsKey(user.getEmail()) || !playlists.get(user.getEmail())
            .contains(new Playlist(user.getEmail(), playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.PLAY_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        if (alreadyRunning.containsKey(selectionKey)) {
            throw new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply());
        }

        validateNoSongsInPlaylistException(playlistTitle);
        PlayPlaylistThread playPlaylistThread = new PlayPlaylistThread(playlistTitle, selectionKey, this,
            spotifyLogger);
        playPlaylistThread.start();
    }

    public void playSong(String songTitle, SelectionKey selectionKey) throws SpotifyException {

        Objects.requireNonNull(songTitle, "The given playlist title cannot be null.");
        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply());
        }

        if (alreadyRunning.containsKey(selectionKey)) {
            throw new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply());
        }

        Song songToPlay = isFound(songTitle);
        if (songToPlay == null) {
            throw new NoSuchSongException(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply());
        }

        PlaySongThread playSongThread = new PlaySongThread(songToPlay.getArtist() + UNDERSCORE + songToPlay.getTitle(),
            selectionKey, this, spotifyLogger);
        alreadyRunning.put(selectionKey, playSongThread);
        playSongThread.start();
        increaseSongPlays(songToPlay);
        writeSongs();
    }

    public void logout(SelectionKey selectionKey) throws SpotifyException, InterruptedException {

        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply());
        }

        if (alreadyRunning.containsKey(selectionKey)) {

            stopSong(selectionKey);
        }

        alreadyLogged.remove(selectionKey);
    }

    public void stopSong(SelectionKey selectionKey) throws SpotifyException, InterruptedException {

        Objects.requireNonNull(selectionKey, "The given selection key cannot be null.");

        if (!alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply());
        }

        if (!alreadyRunning.containsKey(selectionKey)) {
            throw new NoSongPlayingException(ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply());
        }

        alreadyRunning.get(selectionKey).terminateSong();

        alreadyRunning.get(selectionKey).join();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Set<SelectionKey> getAlreadyLogged() {
        return alreadyLogged;
    }

    public Map<String, Set<Playlist>> getPlaylists() {
        return playlists;
    }

    public Map<SelectionKey, PlaySongThread> getAlreadyRunning() {
        return alreadyRunning;
    }

    public Reader getPlaylistsReader() {
        return playlistsReader;
    }

    public Writer getPlaylistsWriter() {
        return playlistsWriter;
    }

    public Reader getSongsReader() {
        return songsReader;
    }

    public Writer getSongsWriter() {
        return songsWriter;
    }

    public void setPlaylistsWriter(Writer playlistsWriter) {
        this.playlistsWriter = playlistsWriter;
    }

    public Set<SongEntity> getSongs() {
        return songs;
    }

    private Writer getAppropriateSongsWriter() throws IOException {
        if (songsWriter == null) {

            return new FileWriter(SONGS_LIST_PATH);
        }

        return songsWriter;
    }

    private synchronized void writeSongs() throws IODatabaseException {
        try {
            Writer toUseSongWriter = getAppropriateSongsWriter();
            GSON.toJson(songs, toUseSongWriter);
            toUseSongWriter.flush();
        } catch (IOException e) {
            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }
    }

    private Reader getAppropriateSongsReader() throws IOException {
        if (songsReader == null) {
            return Files.newBufferedReader(Paths.get(SONGS_LIST_PATH));
        }

        return songsReader;
    }

    private synchronized void readSongs() throws IODatabaseException {
        try {
            Type type = new TypeToken<List<SongEntity>>() {
            }.getType();
            List<SongEntity> songCollection;
            songCollection = GSON.fromJson(getAppropriateSongsReader(), type);
            songs = new LinkedHashSet<>(songCollection);
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }
    }

    private Writer getAppropriatePlaylistsWriter() throws IOException {
        if (playlistsWriter == null) {
            return new FileWriter(PLAYLISTS_LIST_PATH);
        }

        return playlistsWriter;
    }

    private synchronized void writePlaylists() throws IODatabaseException {
        List<Playlist> allPlaylists = new ArrayList<>();
        for (Map.Entry<String, Set<Playlist>> currentEntry : playlists.entrySet()) {

            allPlaylists.addAll(currentEntry.getValue());
        }

        try {
            Writer toUsePlaylistWriter = getAppropriatePlaylistsWriter();
            GSON.toJson(allPlaylists, toUsePlaylistWriter);
            toUsePlaylistWriter.flush();
        } catch (IOException e) {
            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }
    }

    private Reader getAppropriatePlaylistsReader() throws IOException {
        if (playlistsReader == null) {
            return Files.newBufferedReader(Paths.get(PLAYLISTS_LIST_PATH));
        }

        return playlistsReader;
    }

    private synchronized void readPlaylists() throws IODatabaseException {
        try {
            Set<Playlist> readPlaylists;
            Type type = new TypeToken<Set<Playlist>>() {
            }.getType();
            Set<Playlist> playlistCollection = GSON.fromJson(getAppropriatePlaylistsReader(), type);
            readPlaylists = new LinkedHashSet<>(playlistCollection);

            allocatePlaylists(readPlaylists);
        } catch (IOException e) {
            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }
    }

    private synchronized void allocatePlaylists(Set<Playlist> toAllocate) {
        Map<String, List<Playlist>> resultWithList = toAllocate.stream()
            .collect(Collectors.groupingBy(Playlist::getEmailCreator));

        Map<String, Set<Playlist>> resultWithSet = new LinkedHashMap<>();

        for (Map.Entry<String, List<Playlist>> currentEntry : resultWithList.entrySet()) {
            Set<Playlist> currentSet = new LinkedHashSet<>(currentEntry.getValue());
            resultWithSet.put(currentEntry.getKey(), currentSet);
        }

        playlists = resultWithSet;
    }

    private synchronized void removePlaylistFromPlaylists(String emailCreator, String playlistTitle)
        throws SpotifyException {
        Set<Playlist> allPlaylists =  playlists.get(emailCreator);
        for (Playlist currentPlaylist : allPlaylists) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {

                if (!currentPlaylist.getPlaylistSongs().isEmpty()) {
                    throw new PlaylistNotEmptyException(ServerReply
                        .DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply());
                }

                playlists.get(emailCreator).remove(currentPlaylist);
                break;
            }
        }
    }

    private Song isFound(String songTitle) {
        for (SongEntity currentSongEntity : songs) {
            if (currentSongEntity.getSong().getTitle().equalsIgnoreCase(songTitle)) {
                return currentSongEntity.getSong();
            }
        }
        return null;
    }

    private synchronized void removeSongFromPlaylists(String emailCreator, String playlistTitle, String songTitle)
        throws SpotifyException {
        Song songToRemove = isFound(songTitle);
        if (songToRemove == null) {
            throw new NoSuchSongException(ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply());
        }
        for (Playlist currentPlaylist : playlists.get(emailCreator)) {

            if (currentPlaylist.equals(new Playlist(emailCreator, playlistTitle))) {

                if (!currentPlaylist.containsSong(songToRemove)) {
                    throw new NoSuchSongException(ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply());
                }

                currentPlaylist.removeSong(songToRemove);
                break;
            }
        }

        writePlaylists();
    }

    private synchronized void addSongInPlaylist(String emailCreator, String playlistTitle, Song songToAdd)
        throws SpotifyException {
        Set<Playlist> allPlaylists =  playlists.get(emailCreator);
        for (Playlist currentPlaylist : allPlaylists) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {

                if (currentPlaylist.containsSong(songToAdd)) {
                    throw new SongAlreadyInPlaylistException(ServerReply
                        .ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply());
                }

                currentPlaylist.addSong(songToAdd);
            }
        }

        writePlaylists();
    }

    private void validateNoSongsInPlaylistException(String playlistTitle) throws NoSongsInPlaylistException {
        for (Playlist currentPlaylist : playlists.get(user.getEmail())) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {

                if (currentPlaylist.getPlaylistSongs().isEmpty()) {

                    throw new
                        NoSongsInPlaylistException(ServerReply.PLAY_PLAYLIST_NO_SONGS_IN_PLAYLIST_REPLY.getReply());
                }
                break;
            }
        }
    }

    private void increaseSongPlays(Song songToPlay) {
        for (SongEntity currentSongEntity : songs) {
            if (currentSongEntity.getSong().equals(songToPlay)) {
                currentSongEntity.increaseListeningTimes();
            }
        }
    }
}