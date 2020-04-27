package com.coinker.databaseHelper.controller;

import com.coinker.databaseHelper.service.ExportTableStructService;
import com.coinker.databaseHelper.utils.SQLUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ExportTableStructController implements Initializable {

    private ExportTableStructService exportService = new ExportTableStructService();

    @FXML
    private ImageView img;

    @FXML
    private Button testCon;

    @FXML
    private Button exportWord;

    @FXML
    private ChoiceBox<String> dbType;

    @FXML
    private TextField username;

    @FXML
    private TextField port;

    @FXML
    TextField host;

    @FXML
    private PasswordField password;

    @FXML
    private Label dirPath;

    @FXML
    private Button choisePath;

    @FXML
    private ChoiceBox<String> dbName;

    @FXML
    private Label dbLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbType.setItems(FXCollections.observableArrayList("mysql"));
        dbType.getSelectionModel().select(0);
        Image image = new Image("/cxy.jpg");
        img.setImage(image);
        dbType.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if ("mysql".equals(newValue)) {
                    dbName.setVisible(true);
                    dbLabel.setVisible(true);
                } else if ("oracle".equals(newValue)) {
                    dbName.setVisible(false);
                    dbLabel.setVisible(false);
                }
            }
        });
    }

    public void exportDoc(ActionEvent event) {
        String dbType = this.dbType.getValue();
        String username = this.username.getText();
        String password = this.password.getText();
        String exportDir = this.dirPath.getText();
        String dbName = this.dbName.getValue();
        String port = this.port.getText();
        String host = this.host.getText();
        if (exportDir.equals("未选择")) {
            Alerts(false, "请选择文件路径");
            return;
        }
        Map<String, String> map = new HashMap();
        map.put("host", host);
        map.put("port", port);
        map.put("dbType", dbType);
        map.put("dbName", dbName);
        map.put("username", username);
        map.put("password", password);
        map.put("exportDir", exportDir);
        if ("mysql".equals(dbType)) {
            boolean b = checkValue(map);
            if (!b) {
                return;
            }
            try {
                exportService.MySQL(map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 校验输入参数
     *
     * @param map
     * @return
     */
    private boolean checkValue(Map<String, String> map) {
        if (!map.containsKey("dbName") || map.get("dbName") == null || map.get("dbName").equals("")) {
            Alerts(false, "请输入数据库名称！");
            return false;
        }
        if (!map.containsKey("username") || map.get("username") == null || map.get("username").equals("")) {
            Alerts(false, "请输入数据库用户名！");
            return false;
        }
        if (!map.containsKey("password") || map.get("password") == null || map.get("password").equals("")) {
            Alerts(false, "请输入数据库密码！");
            return false;
        }
        if (!map.containsKey("exportDir") || map.get("exportDir") == null || map.get("exportDir").equals("")) {
            Alerts(false, "请输入保存文件的目录！");
            return false;
        }
        return true;
    }

    public static void Alerts(boolean is, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (is) {
            alert.setTitle("Dialog");
            alert.setHeaderText(null);
            alert.setContentText(content);
        } else {
            alert.setTitle("Dialog");
            alert.setHeaderText(null);
            alert.setContentText(content);
        }
        alert.showAndWait();
    }

    public void dbTouch(MouseEvent event) {
        String type = dbType.getValue();
        String user = username.getText();
        String pwd = password.getText();
        String value = dbName.getValue();
        String p = port.getText();
        String h = host.getText();
        if (value != null || "".equals(value)) {
            return;
        }
        if ("mysql".equals(type)) {
            Connection con = SQLUtils.getConnnection(String.format("jdbc:mysql://%s:%s", h, p), user, pwd);
            if (con == null) {
                Alerts(false, "connecting to database failed");
                return;
            }
            ResultSet set = SQLUtils.getResultSet(con, "show databases");
            try {
                List<String> list = new ArrayList<String>();
                while (set.next()) {
                    list.add(set.getString(1));
                }
                System.out.println(list.toString());
                dbName.setItems(FXCollections.observableArrayList(list));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void selectDirPath(ActionEvent event) {
        Stage mainStage = null;
        DirectoryChooser directory = new DirectoryChooser();
        directory.setTitle("选择路径");
        File file = directory.showDialog(mainStage);
        if (file != null) {
            String path = file.getPath();
            dirPath.setText(path);
        }
    }

    public void typeTouch(MouseEvent event) {
        String value = dbType.getValue();
        System.out.println(value);
        if (value.equals("mysql")) {
            dbName.setVisible(true);
        }
        if (value.equals("oracle")) {
            dbName.setVisible(false);
        }
    }

    public void testCon(ActionEvent event) {
        isCon();
    }

    public boolean isCon() {
        String type = dbType.getValue();
        String user = username.getText();
        String pwd = password.getText();
        String p = port.getText();
        String h = host.getText();
        if ("mysql".equals(type)) {
            Connection con = SQLUtils.getConnnection(String.format("jdbc:mysql://%s:%s", h, p), user, pwd);
            if (con != null) {
                Alerts(true, "connected to database success");
                return true;
            } else {
                Alerts(false, "connecting to database failed");
                return false;
            }
        }
        return false;
    }

}


