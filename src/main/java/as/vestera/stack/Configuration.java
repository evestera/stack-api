package as.vestera.stack;

public class Configuration {
    private static int port = 4567;
    static {
        String portString = System.getenv("PORT");
        if (portString != null) {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + portString);
            }
        }
    }

    public static int getPort() {
        return port;
    }


    private static String dbFile = "store.db";
    static {
        String envDbSetting = System.getenv("DB_FILE");
        if (envDbSetting != null) dbFile = envDbSetting;
    }

    public static String getDbFile() {
        return dbFile;
    }

    public static void setDbFile(String dbFile) {
        Configuration.dbFile = dbFile;
    }
}
