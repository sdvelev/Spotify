package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchPlaylistException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NoSuchSongException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotLoggedException;
import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
import bg.sofia.uni.fmi.mjt.server.storage.SongEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamingPlatform {

    private static final String INTERVAL_REGEX = " ";

    private static final Gson GSON = new Gson();
    private Set<SongEntity> songs;
    private User user;
    private boolean isLogged;
    private Map<String, Set<Playlist>> playlists;

    public StreamingPlatform() {

        this.readSongs();
        this.readPlaylists();
    }

    private void readPlaylists() {

        try (Reader reader = Files.newBufferedReader(Paths.get("data/playListsList.json"))) {

            Set<Playlist> readPlaylists = new HashSet<>();

            Type type = new TypeToken<Set<Playlist>>(){}.getType();
            Set<Playlist> playlistCollection = GSON.fromJson(reader, type);
            readPlaylists = new HashSet<>(playlistCollection);

            this.allocatePlaylists(readPlaylists);
        } catch (IOException e) {

            System.out.println();
        }

    }

    public void writePlaylists() {

        List<Playlist> divided = new ArrayList<>();

        for (Map.Entry<String, Set<Playlist>> currentEntry : this.playlists.entrySet()) {

            for (Playlist currentPlaylist : currentEntry.getValue()) {

                divided.add(currentPlaylist);
            }
        }

        try (Writer writer = new FileWriter("data/playlistsList.json")) {

            GSON.toJson(divided, writer);
            writer.flush();
        } catch (IOException e) {

            System.out.println();
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

    private void readSongs() {

        try (Reader reader = Files.newBufferedReader(Paths.get("data/songsList.json"))) {

            Type type = new TypeToken<List<SongEntity>>(){}.getType();
            List<SongEntity> songCollection = GSON.fromJson(reader, type);
            this.songs = new HashSet<>(songCollection);
        } catch (IOException e) {

            System.out.println();
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

        List<SongEntity> result = new LinkedList<>();
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

    public void createPlaylist(String title) throws UserNotLoggedException {

        if (!this.isLogged) {
            throw new UserNotLoggedException("You cannot create playlist unless you have logged-in.");
        }

        Playlist toAdd = new Playlist(this.user.getEmail(), title);

        if (this.playlists.containsKey(this.user.getEmail())) {

            this.playlists.get(this.user.getEmail()).add(toAdd);
        } else {

            this.playlists.put(this.user.getEmail(), new HashSet<>());
            this.playlists.get(this.user.getEmail()).add(toAdd);
        }

        this.writePlaylists();
    }

    public void addSongToPlaylist(String playlistTitle, String songTitle) throws UserNotLoggedException,
        NoSuchSongException, NoSuchPlaylistException {

        if (!this.isLogged) {
            throw new UserNotLoggedException("You cannot create playlist unless you have logged-in.");
        }

        boolean isFound = false;
        Song songToAdd = new Song();
        for (SongEntity currentSongEntity : this.songs) {
            if (currentSongEntity.getSong().getTitle().equalsIgnoreCase(songTitle)) {
                isFound = true;
                songToAdd = currentSongEntity.getSong();
                break;
            }
        }

        if (!isFound) {
            throw new NoSuchSongException("There is not a song with such a title in the system.");
        }

        String emailCreator = this.user.getEmail();

        if (!this.playlists.get(emailCreator).contains(new Playlist(emailCreator, playlistTitle))) {

            throw new NoSuchPlaylistException("Threre is not a playlist with such a title associated with that user.");
        }


        Set<Playlist> allPlaylists =  this.playlists.get(emailCreator);

        for (Playlist currentPlaylist : allPlaylists) {

            if (currentPlaylist.getTitle().equals(playlistTitle)) {
                currentPlaylist.addSong(songToAdd);
            }
        }
        this.writePlaylists();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setIsLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

    public static void main(String[] args) throws IOException {

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
