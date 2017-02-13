package tk.leoforney.phoneimager;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kohsuke.github.*;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Leo on 12/26/2016.
 */
public class Main {
    private static String API_KEY = "8534df699907e4e673e9ff932af2acdfb4c33056";
    private static File homeDirectory = new File(System.getProperty("user.home") + "/PhoneImager/");
    private static JadbConnection jadb;

    private static JadbDevice ds;
    private static JadbDevice rc;

    private static int stage = 0;

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        jadb = new JadbConnection();

        stage = 0;

        GitHubBuilder builder = new GitHubBuilder();
        builder.withOAuthToken(API_KEY);

        GitHub github = builder.build();

        GHOrganization organization = github.getOrganization("7351");

        GHRepository selectedRepo = null;
        List<GHRepository> repositoryList = null;
        List<GHAsset> assets = null;

        FileUtils.cleanDirectory(homeDirectory);

        while (Thread.currentThread().isAlive() && stage != -1) {
            if (stage == 0) { // Display repositories
                repositoryList = new ArrayList<GHRepository>(organization.getRepositories().values());

                log("Choose a repository to download by typing the number:");

                for (GHRepository repository : repositoryList) {
                    log("[" + repositoryList.indexOf(repository) + "] " + repository.getFullName());
                }
                stage++;
            }
            if (stage == 1) {
                log("Selection: ");
                String selectionString = scanner.next();
                if (NumberUtils.isNumber(selectionString)) {
                    Integer selectionNumber = Integer.parseInt(selectionString);

                    if (selectionNumber + 1 <= repositoryList.size()) {
                        selectedRepo = repositoryList.get(selectionNumber);
                        assets = selectedRepo.listReleases().asList().get(0).getAssets();
                        stage++;
                    } else {
                        log("Incorrect input. Please retry.");
                        stage = 0;
                    }

                } else {
                    log("Incorrect input. Please retry.");
                    stage = 0;
                }
            }
            if (stage == 2) {
                for (GHAsset asset : assets) {
                    System.out.println("Downloading " + asset.getName() + " from " + selectedRepo.listReleases().asList().get(0).getName());
                    saveFile(asset);
                }
                stage++;
            }
            if (stage == 3) {
                System.out.println("Downloaded all files!");
                if (args.length >= 1 && args[0].equals("generate")) {
                    stage = -100;
                } else {
                    File schema = new File(homeDirectory + File.separator + "schema.json");
                    if (schema.exists() && !FileUtils.readFileToString(schema).equals("")) {
                        stage++;
                    } else {
                        System.out.println("This repository doesn't implement the schema.json mapping format... Please ask the developer to fix this");
                        System.exit(101);
                    }
                }

            }
            if (stage == -100) {
                List<AssetProp> propList = new ArrayList<AssetProp>();
                scanner.nextLine();
                for (GHAsset asset : assets) {

                    AssetProp prop = new AssetProp();
                    prop.fileName = asset.getName();

                    String contentType = asset.getContentType();

                    if (contentType.equals("application/vnd.android.package-archive")) {
                        prop.type = "apk";
                    } else if (contentType.equals("text/xml") || contentType.equals("application/xml")) {
                        prop.type = "hardwaremap";
                    } else {
                        prop.type = "unknown";
                    }

                    if (!prop.type.equals("hardwaremap")) {
                        System.out.println("Should " + asset.getName() + " be on DS, RC, or BOTH?");
                        String input = scanner.nextLine().toLowerCase();
                        if (input.equals("rc") || input.equals("ds") || input.equals("both")) {
                            prop.destination = input;
                        } else {
                            System.exit(103);
                        }
                    } else {
                        prop.destination = "rc";
                    }

                    propList.add(prop);
                }
                String propListJson = new Gson().toJson(propList);
                File schemajson = new File(homeDirectory + File.separator + "schema.json");
                FileUtils.writeStringToFile(schemajson, propListJson);
                System.out.println("Wrote " + schemajson.getPath());
                System.exit(0);
            }
            if (stage == 4) {
                System.out.println("Please plug in Driver Station");
                try {
                    List<JadbDevice> devices = jadb.getDevices();
                    if (devices.size() == 1) {
                        ds = devices.get(0);
                        System.out.println(ds.getState().toString());
                        stage++;
                    }
                } catch (ConnectException e) {

                }

            }
            if (stage == 5) {
            }
            Thread.sleep(250);
        }

    }

    private static void log(String logString) {
        System.out.println(logString);
    }

    private static void saveFile(GHAsset asset) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URI uri = new URI("https://api.github.com/repos/" + asset.getOwner().getFullName() + "/releases/assets/" +
                asset.getId() + "?access_token=" + API_KEY);

        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Accept", "application/octet-stream");

        org.apache.http.HttpResponse response = httpClient.execute(httpGet);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        FileUtils.writeStringToFile(new File(homeDirectory + File.separator + asset.getName()), result.toString());
    }

    private static void terminate() {
        stage = -1;
    }
}
