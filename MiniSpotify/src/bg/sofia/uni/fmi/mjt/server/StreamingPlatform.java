package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.login.User;
import bg.sofia.uni.fmi.mjt.server.storage.Playlist;
import bg.sofia.uni.fmi.mjt.server.storage.Song;
import bg.sofia.uni.fmi.mjt.server.storage.SongEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StreamingPlatform {

    private static final Gson GSON = new Gson();
    private Set<SongEntity> songs;
    private User user;
    private boolean isLogged;
    private Map<String, List<Playlist>> playlists;

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

    private void allocatePlaylists(Set<Playlist> toAllocate) {

        this.playlists = toAllocate.stream().collect(Collectors.groupingBy(Playlist::getEmailCreator));
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

    public void setUser(User user) {
        this.user = user;
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




    }


}
