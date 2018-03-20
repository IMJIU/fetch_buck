
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoulf
 * @create 2018-03-19 14:34
 **/
public class Fetch {
    static Connection conn;
    static boolean init = false;

    public static void main(String[] args) throws Exception {
        init();
        exe();

    }

    private static void insertPlateInfo(String time) throws SQLException {
        String c = getText("http://quote.eastmoney.com/zs000001.html");
        int i1 = c.indexOf("token=") + 6;
        int i2 = c.indexOf("\"", i1);
        String token = c.substring(i1, i2);
        System.out.println(c.substring(i1, i2));
        String val = getText("http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&cmd=0000011,3990012&sty=DFPIU&st=z&sr=&p=&ps=&cb=&js=var%20C1Cache={quotation:[(x)]}&token=" + token + "&0.2597831847847587");
        System.out.println("content: " + val);
        int i3 = val.indexOf("\"") + 1;
        int i4 = val.indexOf("\"", i3);
        String sh = val.substring(i3, i4);
        System.out.println(sh);
        int i5 = val.indexOf("\"", i4 + 1) + 1;
        int i6 = val.indexOf("\"", i5);
        String sz = val.substring(i5, i6);
        addPlate(sh, time);
        addPlate(sz, time);
    }

    private static void addPlate(String sh, String time) throws SQLException {
        sh = sh.replace("%", "");
        String[] arr = sh.split(",");
        exep("insert into plate values(nextval('seq_b'),?,?,?,?,?,?,?)", time, arr[0], toFloat(arr[2]), toFloat(arr[3]), toFloat(arr[4]), toFloat(arr[5]), arr[6]);
    }

    private static String subQuate(String val) {
        int i3 = val.indexOf("\"") + 1;
        int i4 = val.indexOf("\"", i3);
        return val.substring(i3, i4);
    }

    private static void exe() {
        ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);
        Fetch fetch = new Fetch();
        scheduledExecutor.scheduleAtFixedRate(() -> fetch.parse(), 0, 1, TimeUnit.MINUTES);
    }

    public void test_preparestatement() throws SQLException {
        exep("insert into t(n) values(?)", 5);
        List<Map<String, Object>> list = select("select * from t");
        list.forEach(m -> System.out.println(m));
    }

    public void test_sql() throws SQLException {
        int n = exe("insert into t values(555)");
        System.out.println(n);
        List<Map<String, Object>> list = select("select * from t");
        list.forEach(m -> System.out.println(m));
    }

    public void parse() {
        if (!timeCheck()) {
            return;
        }
        String string = getText("http://data.eastmoney.com/bkzj/hy.html");
        float sh = getVal("sh000001");
        float sz = getVal("sz399001");
        try {
            int i = string.indexOf("data:[\"");
            int end = string.indexOf("]},", i);
            String content = string.substring(i + 5, end + 1);
            String[] arr = content.split("\"");
            String time = getTime();
            System.out.println("" + time + "");
            System.out.println(content);
            for (int j = 1; j < arr.length; j += 2) {
                String str = arr[j];
                String[] row = str.split(",");
                exep("insert into buck_trend values(nextval('seq_b'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", time, j, row[1], row[2], toFloat(row[3]), toFloat(row[4]),
                        toFloat(row[5]), toFloat(row[6]), toFloat(row[7]), toFloat(row[8]), toFloat(row[9]), toFloat(row[10]), toFloat(row[11]),
                        toFloat(row[12]), toFloat(row[13]), row[14], row[15], sh, sz);
            }
            for (String s : arr) {
                System.out.print(s + "\t");
            }
            System.out.println();
            insertPlateInfo(time);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getTime() {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }

    private float getVal(String name) {
        String sh = null;
        try {
            long cur = System.currentTimeMillis();
            sh = getText("http://hq.sinajs.cn/rn=" + cur + "&list=" + name);
            return Float.parseFloat(sh.split(",")[3]);
        } catch (Exception e) {
            System.out.println(sh);
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean timeCheck() {
        String s = new SimpleDateFormat("HHmm").format(new Date());
        int result = Integer.parseInt(s);
        System.out.println(result);
        if ((result >= 930 && result <= 1200) || (result >= 1300 && result <= 1520)) {
            return true;
        }
        return false;
    }

    private static Float toFloat(String s) {
        if (s.equals("-")) {
            return 0f;
        }
        return Float.parseFloat(s);
    }

    private static String getText(String uri) {
        URL url;
        try {
            url = new URL(uri);
            InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is, "gbk");
            BufferedReader br = new BufferedReader(isr);
            String data = br.readLine();
            StringBuilder result = new StringBuilder();
            while (data != null) {
                result.append(data);
                data = br.readLine();
            }
            br.close();
            isr.close();
            is.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void init() {
        String driverName = "org.postgresql.Driver";
        String url = "jdbc:postgresql://127.0.0.1:5432/buck";
        String user = "ott";
        String password = "ott";
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static List<Map<String, Object>> select(String sqlString) throws SQLException {
        List list = new ArrayList();
        PreparedStatement pst = conn.prepareStatement(sqlString);
        ResultSet rSet = pst.executeQuery();
        ResultSetMetaData meta = rSet.getMetaData();
        while (rSet.next()) {
            int cnt = meta.getColumnCount();
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i < cnt + 1; i++) {
                String cname = meta.getColumnName(i);
                map.put(cname, rSet.getObject(i));
            }
            list.add(map);
        }
        return list;
    }

    public static int exe(String sql) throws SQLException {
        Statement s = conn.createStatement();
        return s.executeUpdate(sql);
    }

    public static int exep(String sql, Object... p) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < p.length; i++) {
            ps.setObject(i + 1, p[i]);
//            ps.setInt(i + 1,5);
        }
        return ps.executeUpdate();
    }
}
