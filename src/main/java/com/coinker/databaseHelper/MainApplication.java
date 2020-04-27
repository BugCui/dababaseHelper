package com.coinker.databaseHelper;

import com.coinker.databaseHelper.controller.ExportTableStructController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent login = FXMLLoader.load(getClass().getResource("./ui/main.fxml"));
        primaryStage.setTitle("数据库表结构转Word");
        primaryStage.setScene(new Scene(login));
        primaryStage.show();
    }
}
