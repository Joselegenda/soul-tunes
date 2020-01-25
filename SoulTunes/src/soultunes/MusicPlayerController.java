/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soultunes;

import com.sun.javafx.collections.ObservableListWrapper;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Callback;
import javafx.util.Duration;


public class MusicPlayerController implements Initializable {
    @FXML
    TableView tracksTableView;
    
    @FXML
    Slider trackSlider;
    
    @FXML
    Label artistLabel;
    
    @FXML
    Label albumLabel;
    
    @FXML
    Label songLengthLabel;
    
    @FXML
    Label songTimeLabel;
    
    @FXML
    TextField searchField;
    
    @FXML
    Button genPlayButton;
    
    @FXML
    Button previousAlbumButton;
    
    @FXML
    Button nextAlbumButton;
    
    @FXML
    ImageView albumCover;
    
    @FXML 
    ProgressIndicator progress;
    
    // Other Fields...
    ScheduledExecutorService sliderExecutor = null;
    ScheduledExecutorService progressExecutor = null;
    ScheduledExecutorService searchAlbumsExecutor = null;
    MediaPlayer mediaPlayer = null;
    boolean isSliderAnimationActive = false;
    Button lastPlayButtonPressed = null;
    ArrayList<Album> albums = null;
    int currentAlbumIndex = 0;
    
    
    public void shutdown(){
        if(sliderExecutor != null){
            if(progressExecutor != null || searchAlbumsExecutor != null){
                progressExecutor.shutdown(); 
                searchAlbumsExecutor.shutdown();
            }
            sliderExecutor.shutdown();
        }
        Platform.exit();
    }
    
    @FXML
    private void onEnter(KeyEvent ke){
        if(ke.getCode().equals(KeyCode.ENTER) && searchField.isFocused() && !searchField.getText().equals("")){
            currentAlbumIndex = 0;
            
            // Search artist
            progress.setVisible(true);
            progress.setProgress(-1.0d);
            
            progressExecutor.submit(new Task<Void>(){
                @Override
                protected Void call() throws Exception{
                    searchFirstAlbumFromArtist(searchField.getText());
                    return null;
                }
                
                @Override
                protected void succeeded(){
                    try{
                        displayAlbum(currentAlbumIndex);
                        progress.setProgress(0.5d);
                    }
                    catch(Exception e){
                        artistLabel.setText("Error");
                        albumLabel.setText("Invalid artist.");
                        progress.setVisible(false);
                        albumCover.setImage(new Image("file:error.png")); // what kind of path is this?
                        tracksTableView.setItems(new ObservableListWrapper(new ArrayList()));
                    }
                    
                    // Search other albums
                    searchAlbumsExecutor.submit(new Task<Void> () {
                        @Override
                        protected Void call() throws Exception{
                            searchAlbumsFromArtist(searchField.getText());
                            return null;
                        }

                        @Override
                        protected void succeeded(){
                            progress.setProgress(1d);
                        }

                        @Override
                        protected void cancelled(){
                            albumLabel.setText("Error");
                            artistLabel.setText("Searching failed.");
                            albumCover.setImage(new Image("file:error.png"));
                            tracksTableView.setItems(new ObservableListWrapper(new ArrayList()));
                            progress.setVisible(false);  
                        }
                    });                    
                }
                
                @Override
                protected void cancelled(){
                    albumLabel.setText("Error");
                    artistLabel.setText("Searching failed.");
                    albumCover.setImage(new Image("file:error.png"));
                    tracksTableView.setItems(new ObservableListWrapper(new ArrayList()));
                    progress.setVisible(false);                     
                }
            });
        }
    }
    
    @FXML
    private void nextAlbum(ActionEvent event){
        displayAlbum(++currentAlbumIndex);
    }
    
    @FXML
    private void previousAlbum(ActionEvent event){
        displayAlbum(--currentAlbumIndex);
    }
      
    private void startMusic(String url){ 
        lastPlayButtonPressed.setText("Pause");
        
        // If a song is already playing, stop it and play the new song
        if(mediaPlayer != null){
            stopMusic();
        }
        
        trackSlider.setDisable(false);
        genPlayButton.setDisable(false);
        genPlayButton.setText("Pause");
        
        mediaPlayer = new MediaPlayer(new Media(url));
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            isSliderAnimationActive = true;
            trackSlider.setValue(0);
            trackSlider.setMax(30.0);
            
            mediaPlayer.setOnEndOfMedia(() ->{
                mediaPlayer.pause();
                mediaPlayer.seek(Duration.ZERO);
                
                isSliderAnimationActive = false;
                trackSlider.setValue(0);
                stopMusic();
                if(lastPlayButtonPressed != null){
                    lastPlayButtonPressed.setText("Play");
                }
            });
        });
    }

    public void stopMusic(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.dispose();
            songTimeLabel.setText("0:00");
            genPlayButton.setText("Play");
            trackSlider.setDisable(true);
            genPlayButton.setDisable(true);
        }
        mediaPlayer = null;
    }
    
    @FXML
    public void playPauseMusic(){
        try{
            if(genPlayButton.getText().equals("Play")){
                if(lastPlayButtonPressed != null){
                    lastPlayButtonPressed.setText("Pause");
                }
                genPlayButton.setText("Pause");
                
                if(mediaPlayer != null){
                    mediaPlayer.play();
                }
                trackSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
                isSliderAnimationActive = true;                
            }
            else{
                if(lastPlayButtonPressed != null){
                    lastPlayButtonPressed.setText("Play");
                }
                genPlayButton.setText("Play");
                if(mediaPlayer != null){
                    mediaPlayer.pause();
                }
                isSliderAnimationActive = false;
            }
        }
        catch(Exception e){
            artistLabel.setText("Error");
            albumLabel.setText("Song playback failed.");
            genPlayButton.setDisable(true);
            trackSlider.setDisable(true);
            genPlayButton.setText("Play");
            trackSlider.setValue(0.0f);
            songTimeLabel.setText("0:00");
            songLengthLabel.setText("0:00");
        }
    }
    
    private void displayAlbum(int albumNumber)
    {   
        // Display Tracks for the album passed as parameter
        if (albumNumber >=0 && albumNumber < albums.size())
        {
            Album album = albums.get(albumNumber);
            
            artistLabel.setText(album.getArtistName());
            albumLabel.setText(album.getAlbumName());
            albumCover.setImage(new Image(album.getImageURL()));
            
            if(albums.size() > 1){
                if(albumNumber == albums.size() - 1){
                    nextAlbumButton.setDisable(true);
                    previousAlbumButton.setDisable(false);
                }
                else if(albumNumber == 0){
                    nextAlbumButton.setDisable(false);
                    previousAlbumButton.setDisable(true);
                }
                else{
                    nextAlbumButton.setDisable(false);
                    previousAlbumButton.setDisable(false);
                }
            }
            
            // Set tracks
            ArrayList<TrackForTableView> tracks = new ArrayList<>();
            for (int i=0; i<album.getTracks().size(); ++i)
            {
                TrackForTableView trackForTable = new TrackForTableView();
                Track track = album.getTracks().get(i);
                trackForTable.setTrackNumber(track.getNumber());
                trackForTable.setTrackTitle(track.getTitle());
                trackForTable.setTrackPreviewUrl(track.getUrl());
                tracks.add(trackForTable);
            }
            tracksTableView.setItems(new ObservableListWrapper(tracks));

            if(lastPlayButtonPressed != null){
                lastPlayButtonPressed.setText("Play");
                lastPlayButtonPressed = null;
            }
        }
    }
    
    private void searchAlbumsFromArtist(String artistName)
    {
        // The thread call takes care of the exception
        String artistId = SpotifyController.getArtistId(artistName);
        albums = SpotifyController.getAlbumDataFromArtist(artistId);  
        if(albums.size() > 1){
            nextAlbumButton.setDisable(false);               
        }       
       
    }
    
     private void searchFirstAlbumFromArtist(String artistName)
    {
        try{
            String artistId = SpotifyController.getArtistId(artistName);
            albums = SpotifyController.getFirstAlbumDataFromArtist(artistId); 
            previousAlbumButton.setDisable(true);
            nextAlbumButton.setDisable(true);
        }
        catch(Exception e){
            albumLabel.setText("Error");
            artistLabel.setText("Invalid artist.");
            albumCover.setImage(new Image("file:error.png"));
            tracksTableView.setItems(new ObservableListWrapper(new ArrayList()));            
        }
       
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Setup Table View
        TableColumn<TrackForTableView, Number> trackNumberColumn = new TableColumn("#");
        trackNumberColumn.setCellValueFactory(new PropertyValueFactory("trackNumber"));
        trackNumberColumn.setPrefWidth(28);
        
        TableColumn trackTitleColumn = new TableColumn("Title");
        trackTitleColumn.setCellValueFactory(new PropertyValueFactory("trackTitle"));
        trackTitleColumn.setPrefWidth(220);
        
        TableColumn playColumn = new TableColumn("Preview");
        playColumn.setCellValueFactory(new PropertyValueFactory("trackPreviewUrl"));
        Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>> cellFactory = new Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>>(){
            @Override
            public TableCell<TrackForTableView, String> call(TableColumn<TrackForTableView, String> param) {
                final TableCell<TrackForTableView, String> cell = new TableCell<TrackForTableView, String>(){
                    final Button playButton = new Button("Play");

                    @Override
                    public void updateItem(String item, boolean empty)
                    {
                        if (item != null && item.equals("") == false){
                            playButton.setOnAction(event -> {
                                if(playButton.getText().equals("Pause") || (mediaPlayer != null && mediaPlayer.getMedia().getSource().equals(item))){
                                    playPauseMusic();
                                }
                                else{
                                    if(lastPlayButtonPressed != null){
                                        lastPlayButtonPressed.setText("Play");
                                    }
                                    lastPlayButtonPressed = playButton;
                                    startMusic(item);
                                }
                            });
                            
                            setGraphic(playButton);
                        }
                        else{                        
                            setGraphic(null);
                        }

                        setText(null);                        
                    }
                };
                
                return cell;
            }
        };
        playColumn.setCellFactory(cellFactory);
        tracksTableView.getColumns().setAll(trackNumberColumn, trackTitleColumn, playColumn);

        trackSlider.setOnMouseReleased(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (mediaPlayer != null)
                {
                    mediaPlayer.seek(Duration.seconds(trackSlider.getValue()));
                    
                    int time = 0;
                    time += (int)(trackSlider.getValue());

                    int min = time/60;
                    int sec = time%60;

                    songTimeLabel.setText(String.format(min + ":%02d", sec));                     
                }
            }
        });
        
        // Schedule the slider to move right every second
        sliderExecutor = Executors.newSingleThreadScheduledExecutor();
        sliderExecutor.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                // We can't update the GUI elements on a separate thread... 
                // Let's call Platform.runLater to do it in main thread!!
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        // Move slider
                        if(isSliderAnimationActive){
                            trackSlider.setValue(trackSlider.getValue() + 1.0);

                            int length = (int)trackSlider.getMax();
                            int lengthMin = length/60;
                            int lengthSec = length%60;
                            songLengthLabel.setText(String.format("/ " + lengthMin + ":%02d", lengthSec));
                            
                            int time = 0;
                            time += (int)(trackSlider.getValue());
                            
                            int min = time/60;
                            int sec = time%60;
                            
                            songTimeLabel.setText(String.format(min + ":%02d", sec)); 
                        }
                    }
                });
            }
        }, 1, 1, TimeUnit.SECONDS);
        
        searchAlbumsExecutor = Executors.newSingleThreadScheduledExecutor(); 
        progressExecutor = Executors.newSingleThreadScheduledExecutor(); 
        genPlayButton.setDisable(true);
        trackSlider.setDisable(true);
        Platform.runLater(new Runnable(){
            @Override
            public void run(){
                searchField.requestFocus();
            }
        });
        
    }        
}

