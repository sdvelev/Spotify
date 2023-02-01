package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSongPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistAlreadyExistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.PlaylistNotEmptyException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongAlreadyInPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.SongIsAlreadyPlayingException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.player.PlayPlaylist;
import bg.sofia.uni.fmi.mjt.server.player.PlaySong;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
import bg.sofia.uni.fmi.mjt.server.storage.SongEntity;
import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StreamingPlatform {

    private static final String INTERVAL_REGEX = " ";
    private final static String PLAYLISTS_LIST_PATH = "data" + File.separator + "PlaylistsList.json";
    private final static String SONGS_LIST_PATH = "data" + File.separator + "SongsList.json";

    private static final Gson GSON = new Gson();
    private Set<SongEntity> songs;
    private User user;
    private boolean isLogged;
    private boolean isPlaying;
    private Map<String, Set<Playlist>> playlists;
    private Map<SelectionKey, PlaySong> alreadyRunning;
    private Set<SelectionKey> alreadyLogged;

    public StreamingPlatform() throws IODatabaseException {

        this.playlists = new HashMap<>();
        this.alreadyRunning = new ConcurrentHashMap<>();
        this.alreadyLogged = new HashSet<>();

        this.readSongs();
        this.readPlaylists();
    }

    private void readPlaylists() throws IODatabaseException {

        try (Reader reader = Files.newBufferedReader(Paths.get(PLAYLISTS_LIST_PATH))) {

            Set<Playlist> readPlaylists = new HashSet<>();

            Type type = new TypeToken<Set<Playlist>>() {
            }.getType();
            Set<Playlist> playlistCollection = GSON.fromJson(reader, type);
            readPlaylists = new HashSet<>(playlistCollection);

            this.allocatePlaylists(readPlaylists);
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }

    }

    public void writeSongs() throws IODatabaseException {

        try (Writer writer = new FileWriter(SONGS_LIST_PATH)) {

            GSON.toJson(this.songs, writer);
            writer.flush();
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }

    }

    public void writePlaylists() throws IODatabaseException {

        List<Playlist> allPlaylists = new ArrayList<>();
        for (Map.Entry<String, Set<Playlist>> currentEntry : this.playlists.entrySet()) {

            allPlaylists.addAll(currentEntry.getValue());
        }

        try (Writer writer = new FileWriter(PLAYLISTS_LIST_PATH)) {

            GSON.toJson(allPlaylists, writer);
            writer.flush();
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }

    }

    private void allocatePlaylists(Set<Playlist> toAllocate) {

        Map<String, List<Playlist>> resultWithList = toAllocate.stream()
            .collect(Collectors.groupingBy(Playlist::getEmailCreator));

        Map<String, Set<Playlist>> resultWithSet = new HashMap<>();

        for (Map.Entry<String, List<Playlist>> currentEntry : resultWithList.entrySet()) {

            Set<Playlist> currentSet = new HashSet<>(currentEntry.getValue());
            resultWithSet.put(currentEntry.getKey(), currentSet);
        }
        this.playlists = resultWithSet;
    }

    private void readSongs() throws IODatabaseException {

        try (Reader reader = Files.newBufferedReader(Paths.get(SONGS_LIST_PATH))) {

            Type type = new TypeToken<List<SongEntity>>() {
            }.getType();
            List<SongEntity> songCollection = GSON.fromJson(reader, type);
            this.songs = new HashSet<>(songCollection);
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }

    }

    public List<SongEntity> getTopNMostListenedSongs(int n) {

        if (n == 0) {

            return new ArrayList<>();
        }

        return this.songs.stream()
            .sorted((content1, content2) -> Integer.compare(content2.getListeningTimes(),
                content1.getListeningTimes()))
            .limit(n)
            .toList();
    }

    public List<SongEntity> searchSongs(String word) {

        List<SongEntity> result = new ArrayList<>();
        for (SongEntity currentSongEntity : this.songs) {

            boolean contains = true;
            for (String currentWord : word.split(INTERVAL_REGEX)) {

                if (!currentSongEntity.getSong().getTitle().toLowerCase().contains(currentWord.toLowerCase()) &&
                    !currentSongEntity.getSong().getArtist().toLowerCase().contains(currentWord.toLowerCase())) {

                    contains = false;
                }
            }

            if (contains) {
                result.add(currentSongEntity);
            }
        }

        return result;
    }

    public void createPlaylist(String playlistTitle, SelectionKey selectionKey) throws UserNotLoggedException,
        IODatabaseException, PlaylistAlreadyExistException {

        if (!this.alreadyLogged.contains(selectionKey)) {

            throw new UserNotLoggedException(ServerReply.CREATE_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        Playlist toAdd = new Playlist(this.user.getEmail(), playlistTitle);
        if (this.playlists.containsKey(this.user.getEmail())) {

            if (this.playlists.get(this.user.getEmail()).contains(toAdd)) {

                throw new PlaylistAlreadyExistException(ServerReply.CREATE_PLAYLIST_ALREADY_EXIST_REPLY.getReply());
            }

            this.playlists.get(this.user.getEmail()).add(toAdd);
        } else {

            this.playlists.put(this.user.getEmail(), new HashSet<>());
            this.playlists.get(this.user.getEmail()).add(toAdd);
        }

        this.writePlaylists();
    }

    public void deletePlaylist(String playlistTitle, SelectionKey selectionKey) throws UserNotLoggedException,
        IODatabaseException, PlaylistNotEmptyException, NoSuchPlaylistException {

        if (!this.alreadyLogged.contains(selectionKey)) {

            throw new UserNotLoggedException(ServerReply.DELETE_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = this.user.getEmail();
        if (!this.playlists.get(emailCreator).contains(new Playlist(emailCreator, playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.DELETE_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        this.removePlaylistFromPlaylists(emailCreator, playlistTitle);
        this.writePlaylists();
    }

    private void removePlaylistFromPlaylists(String emailCreator, String playlistTitle)
        throws PlaylistNotEmptyException {

        Set<Playlist> allPlaylists =  this.playlists.get(emailCreator);
        for (Playlist currentPlaylist : allPlaylists) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {

                if (!currentPlaylist.getPlaylistSongs().isEmpty()) {

                    throw new PlaylistNotEmptyException(ServerReply
                        .DELETE_PLAYLIST_NOT_EMPTY_PLAYLIST_REPLY.getReply());
                }

                this.playlists.get(emailCreator).remove(currentPlaylist);
                break;
            }
        }
    }

    Song isFound(String songTitle) {

        for (SongEntity currentSongEntity : this.songs) {
            if (currentSongEntity.getSong().getTitle().equalsIgnoreCase(songTitle)) {
                return currentSongEntity.getSong();
            }
        }
        return null;
    }

    public void addSongToPlaylist(String playlistTitle, String songTitle, SelectionKey selectionKey)
        throws UserNotLoggedException, NoSuchSongException, NoSuchPlaylistException, IODatabaseException,
        SongAlreadyInPlaylistException {

        if (!this.alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.ADD_SONG_TO_NOT_LOGGED_REPLY.getReply());
        }

        Song songToAdd = isFound(songTitle);
        /*for (SongEntity currentSongEntity : this.songs) {
            if (currentSongEntity.getSong().getTitle().equalsIgnoreCase(songTitle)) {
                isFound = true;
                songToAdd = currentSongEntity.getSong();
                break;
            }
        }*/

        if (songToAdd == null) {
            throw new NoSuchSongException(ServerReply.ADD_SONG_TO_NO_SUCH_SONG_REPLY.getReply());
        }

        String emailCreator = this.user.getEmail();
        if (!this.playlists.get(emailCreator).contains(new Playlist(emailCreator, playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.ADD_SONG_TO_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        addSongInPlaylist(emailCreator, playlistTitle, songToAdd);
    }

    public void removeSongFromPlaylist(String playlistTitle, String songTitle, SelectionKey selectionKey)
        throws UserNotLoggedException, NoSuchSongException, NoSuchPlaylistException, IODatabaseException {

        if (!this.alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.REMOVE_SONG_FROM_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = this.user.getEmail();
        if (!this.playlists.get(emailCreator).contains(new Playlist(emailCreator, playlistTitle))) {
            throw new NoSuchPlaylistException(ServerReply.REMOVE_SONG_FROM_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        removeSongFromPlaylists(emailCreator, playlistTitle, songTitle);
    }

    private void removeSongFromPlaylists(String emailCreator, String playlistTitle, String songTitle)
        throws IODatabaseException, NoSuchSongException {

        Song songToRemove = isFound(songTitle);
        if (songToRemove == null) {
            throw new NoSuchSongException(ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply());
        }
        for (Playlist currentPlaylist : this.playlists.get(emailCreator)) {

            if (currentPlaylist.equals(new Playlist(emailCreator, playlistTitle))) {

                if (!currentPlaylist.containsSong(songToRemove)) {

                    throw new NoSuchSongException(ServerReply.REMOVE_SONG_FROM_NO_SUCH_SONG_REPLY.getReply());
                }

                currentPlaylist.removeSong(songToRemove);
                break;
            }
        }

        this.writePlaylists();
    }

    private void addSongInPlaylist(String emailCreator, String playlistTitle, Song songToAdd)
        throws IODatabaseException, SongAlreadyInPlaylistException {

        Set<Playlist> allPlaylists =  this.playlists.get(emailCreator);
        for (Playlist currentPlaylist : allPlaylists) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {

                if (currentPlaylist.containsSong(songToAdd)) {

                    throw new SongAlreadyInPlaylistException(ServerReply
                        .ADD_SONG_TO_SONG_ALREADY_EXIST_REPLY.getReply());
                }

                currentPlaylist.addSong(songToAdd);
            }
        }

        this.writePlaylists();
    }
    public Playlist showPlaylist(String playlistTitle, SelectionKey selectionKey)
        throws UserNotLoggedException, NoSuchPlaylistException {

        if (!this.alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = this.user.getEmail();
        if (!this.playlists.get(emailCreator).contains(new Playlist(emailCreator, playlistTitle))) {

            throw new NoSuchPlaylistException(ServerReply.SHOW_PLAYLIST_NO_SUCH_PLAYLIST_REPLY.getReply());
        }

        Set<Playlist> allPlaylists =  this.playlists.get(emailCreator);
        for (Playlist currentPlaylist : allPlaylists) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {
                return currentPlaylist;
            }
        }
        return new Playlist();
    }

    public List<String> showPlaylists(SelectionKey selectionKey)
        throws UserNotLoggedException {

        if (!this.alreadyLogged.contains(selectionKey)) {
            throw new UserNotLoggedException(ServerReply.SHOW_PLAYLIST_NOT_LOGGED_REPLY.getReply());
        }

        String emailCreator = this.user.getEmail();

        return this.playlists.get(emailCreator).stream()
            .map(Playlist::getTitle)
            .toList();

    }

    private final static String UNDERSCORE = "_";

    public void playPlaylist(String playListTitle, SelectionKey selectionKey) throws UserNotLoggedException,
        SongIsAlreadyPlayingException {

        if (!this.alreadyLogged.contains(selectionKey)) {

            throw new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply());
        }

        if (this.alreadyRunning.containsKey(selectionKey)) {

            throw new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply());
        }

        PlayPlaylist playPlaylistThread = new PlayPlaylist(playListTitle, selectionKey, this);
        playPlaylistThread.start();
    }

    public void playSong(String songTitle, SelectionKey selectionKey) throws UserNotLoggedException,
        NoSuchSongException, SongIsAlreadyPlayingException, IODatabaseException {

        if (!this.alreadyLogged.contains(selectionKey)) {

            throw new UserNotLoggedException(ServerReply.PLAY_SONG_NOT_LOGGED_REPLY.getReply());
        }

        if (this.alreadyRunning.containsKey(selectionKey)) {

            throw new SongIsAlreadyPlayingException(ServerReply.PLAY_SONG_IS_ALREADY_RUNNING_REPLY.getReply());
        }

        Song songToPlay = isFound(songTitle);
        if (songToPlay == null) {
            throw new NoSuchSongException(ServerReply.PLAY_SONG_NO_SUCH_SONG_REPLY.getReply());
        }

        PlaySong playSongThread = new PlaySong(songToPlay.getArtist() + UNDERSCORE + songToPlay.getTitle(),
            selectionKey, this);
        this.alreadyRunning.put(selectionKey, playSongThread);
        playSongThread.start();
        this.increaseSongPlays(songToPlay);
        this.writeSongs();
    }

    private void increaseSongPlays(Song songToPlay) {

        for (SongEntity currentSongEntity : this.songs) {
            if (currentSongEntity.getSong().equals(songToPlay)) {

                currentSongEntity.increaseListeningTimes();
            }
        }
    }

    public void logout(SelectionKey selectionKey) throws UserNotLoggedException, NoSongPlayingException,
        InterruptedException {

        if (!this.alreadyLogged.contains(selectionKey)) {

            throw new UserNotLoggedException(ServerReply.LOGOUT_COMMAND_USER_NOT_LOGGED_REPLY.getReply());
        }

        if (this.alreadyRunning.containsKey(selectionKey)) {

            this.stopSong(selectionKey);
        }

        this.alreadyLogged.remove(selectionKey);
    }

    public void stopSong(SelectionKey selectionKey) throws NoSongPlayingException, UserNotLoggedException,
        InterruptedException {

        if (!this.alreadyLogged.contains(selectionKey)) {

            throw new UserNotLoggedException(ServerReply.STOP_COMMAND_NOT_LOGGED_REPLY.getReply());
        }

        if (!this.alreadyRunning.containsKey(selectionKey)) {

            throw new NoSongPlayingException(ServerReply.STOP_COMMAND_NO_SONG_PLAYING.getReply());
        }

        this.alreadyRunning.get(selectionKey).terminateSong();

        this.alreadyRunning.get(selectionKey).join();

        //this.alreadyRunning.remove(selectionKey);
    }


    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public Set<SelectionKey> getAlreadyLogged() {
        return alreadyLogged;
    }

    public Map<String, Set<Playlist>> getPlaylists() {
        return playlists;
    }

    public void setIsLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

    public Map<SelectionKey, PlaySong> getAlreadyRunning() {
        return alreadyRunning;
    }

    public void setAlreadyRunning(
        Map<SelectionKey, PlaySong> alreadyRunning) {
        this.alreadyRunning = alreadyRunning;
    }

    public static void main(String[] args) throws IOException, IODatabaseException {

       /* Set<SongEntity> hashSet = new HashSet<>();

        Song song1 = new Song("No Time To Die", "Billie Eilish", 239, "pop");
        Song song2 = new Song("The Crown - Main title", "Hans Zimmer", 85, "classical");
        Song song3 = new Song("Vivaldi Variation", "Florian Christl", 114, "classical");
        Song song4 = new Song("The Crown - Bittersweet Symphony", "Richard Ashcroft", 248, "modern");

        hashSet.add(new SongEntity(song1, 0));
        hashSet.add(new SongEntity(song2, 0));
        hashSet.add(new SongEntity(song3, 0));
        hashSet.add(new SongEntity(song4, 0));
        String json = GSON.toJson(hashSet);
        //System.out.println(json);

        StreamingPlatform streamingPlatform = new StreamingPlatform();

        Set<Playlist> playlistst = new HashSet<>();

        Playlist toAdd = new Playlist("sampleEmail@abv.bg", "The Crown Music");
        toAdd.addSongToPlaylist(song2);
        toAdd.addSongToPlaylist(song4);

        playlistst.add(toAdd);

        Playlist toAdd2 = new Playlist("sampleEmail2@abv.bg", "My Favourites");
        toAdd2.addSongToPlaylist(song1);
        toAdd2.addSongToPlaylist(song3);

        playlistst.add(toAdd2);

        String json2 = GSON.toJson(playlistst);
        System.out.println(json2);*/

        StreamingPlatform streamingPlatform = new StreamingPlatform();

        Map<String, List<Playlist>> toWrite = new HashMap<>();

    }


}
