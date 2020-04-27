package com.coinker.databaseHelper.service;

import com.coinker.databaseHelper.controller.ExportTableStructController;
import com.coinker.databaseHelper.utils.FileUtils;
import com.coinker.databaseHelper.utils.POITLStyle;
import com.coinker.databaseHelper.utils.SQLUtils;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.DocxRenderData;
import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.TextRenderData;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出表结构核心代码
 */
public class ExportTableStructService {

    private static Logger log = Logger.getLogger(ExportTableStructService.class);

    public void MySQL(Map<String, String> map) throws IOException {
        //设置生成文件名
        String outFile = map.get("exportDir")+"/"+map.get("dbName") + "-表结构.docx";
        //查询表的名称以及一些表需要的信息
        String sql_table_info = "SELECT table_name, table_type , ENGINE,table_collation,table_comment, create_options FROM information_schema.TABLES WHERE table_schema='" + map.get("dbName") + "'";
        //查询表的结构信息
        String sql_table_struct = "SELECT ordinal_position,column_name,column_type, column_key, extra ,is_nullable, column_default, column_comment,data_type,character_maximum_length "
                + "FROM information_schema.columns WHERE table_schema='" + map.get("dbName") + "' and table_name='";
        String URL = "jdbc:mysql://" + map.get("host") + ":" + map.get("port");
        Connection con = SQLUtils.getConnnection(URL, map.get("username"), map.get("password"));
        ResultSet rs = SQLUtils.getResultSet(con, sql_table_info);
        createDoc(rs, sql_table_struct, outFile, "MYSQL", map.get("dbName") + "数据库表结构", con);
    }

    private static void createDoc(ResultSet rs, String sqls, String outFile, String dbType, String title, Connection con) throws IOException {
        log.info("开始生成文件");
        // 获得所有表名
        List<Map<String, String>> list = getTableName(rs);
        // 表头
        RowRenderData header = getHeader();
        // 生成内容的数据结构
        Map<String, Object> datas = new HashMap();
        datas.put("title", title);
        List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
        int i = 0;
        // 遍历表名
        for (Map<String, String> str : list) {
            log.info(str);
            i++;
            String sql = sqls + str.get("table_name") + "'";
            ResultSet set = SQLUtils.getResultSet(con, sql);
            List<RowRenderData> rowList = getRowRenderData(set);
            Map<String, Object> data = new HashMap();
            data.put("no", "" + i);
            data.put("table_comment", str.get("table_comment") + "");
            data.put("engine", str.get("engine") + "");
            data.put("table_collation", str.get("table_collation") + "");
            data.put("table_type", str.get("table_type") + "");
            data.put("name", new TextRenderData(str.get("table_name"), POITLStyle.getHeaderStyle()));
            data.put("table", new MiniTableRenderData(header, rowList));
            tableList.add(data);
        }
        // 生成word文档
        datas.put("tablelist", new DocxRenderData(FileUtils.Base64ToFile(outFile, dbType), tableList));
        XWPFTemplate template = XWPFTemplate.compile(FileUtils.Base64ToInputStream()).render(datas);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            log.info("生成文件结束");
            ExportTableStructController.Alerts(true,"生成表结构成功!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.info("生成文件失败");
            ExportTableStructController.Alerts(true,"生成表结构失败!");;
        } finally {
            try {
                // 关闭
                template.write(out);
                out.flush();
                out.close();
                template.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取一张表的结构数据
     *
     * @return List<RowRenderData>
     */
    private static List<RowRenderData> getRowRenderData(ResultSet set) {
        List<RowRenderData> result = new ArrayList();

        try {
            int i = 0;
            while (set.next()) {
                i++;
                RowRenderData row = RowRenderData.build(
                        new TextRenderData(set.getString("ordinal_position") + ""),
                        new TextRenderData(set.getString("column_name") + ""),
                        new TextRenderData(set.getString("column_comment") + ""),
                        new TextRenderData(set.getString("data_type") + ""),
                        new TextRenderData(set.getString("character_maximum_length") + ""),
                        new TextRenderData(set.getString("is_nullable") + ""),
                        new TextRenderData(set.getString("column_default") + "")
                );
                if (i % 2 == 0) {
                    row.setRowStyle(POITLStyle.getBodyTableStyle());
                    result.add(row);
                } else {
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取数据库的所有表名及表的信息
     *
     * @return list
     */
    private static List<Map<String, String>> getTableName(ResultSet rs) {
        List<Map<String, String>> list = new ArrayList();
        try {
            while (rs.next()) {
                Map<String, String> tableInfo = new HashMap();
                tableInfo.put("table_name", rs.getString("table_name") + "");
                tableInfo.put("table_type", rs.getString("table_type") + "");
                tableInfo.put("engine", rs.getString("engine") + "");
                tableInfo.put("table_collation", rs.getString("table_collation") + "");
                tableInfo.put("table_comment", rs.getString("table_comment") + "");
                tableInfo.put("create_options", rs.getString("create_options") + "");
                list.add(tableInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 设置表头样式
     *
     * @return RowRenderData
     */
    private static RowRenderData getHeader() {
        RowRenderData header = RowRenderData.build(
                new TextRenderData("序号", POITLStyle.getHeaderStyle()),
                new TextRenderData("字段名称", POITLStyle.getHeaderStyle()),
                new TextRenderData("字段类型", POITLStyle.getHeaderStyle()),
                new TextRenderData("长度", POITLStyle.getHeaderStyle()),
                new TextRenderData("允许空", POITLStyle.getHeaderStyle()),
                new TextRenderData("字段描述", POITLStyle.getHeaderStyle()),
                new TextRenderData("缺省值", POITLStyle.getHeaderStyle()));
        header.setRowStyle(POITLStyle.getHeaderTableStyle());
        return header;
    }
}
