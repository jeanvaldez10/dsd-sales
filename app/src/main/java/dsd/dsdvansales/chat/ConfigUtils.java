package dsd.dsdvansales.chat;

import com.google.gson.Gson;

import java.io.IOException;

public class ConfigUtils extends CoreConfigUtils {

    public static ChatConfigs getSampleConfigs(String fileName) throws IOException {
        ConfigParser configParser = new ConfigParser();
        Gson gson = new Gson();
        return gson.fromJson(configParser.getConfigsAsJsonString(fileName), ChatConfigs.class);
    }
}
