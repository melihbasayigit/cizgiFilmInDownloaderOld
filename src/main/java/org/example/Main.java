package org.example;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Main {
    public static final DecimalFormat format = new DecimalFormat("0.00");
    public static void main(String[] args) {
        boolean commandInput = false;
        boolean openBrowser = false;
        boolean downloader = true;
        String link = "";
        System.out.println("Cizgifilm.in Video İndiriciye Hoşgeldiniz.");
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Lütfen cizgifilm.in sitesinden indireceğiniz videonun sayfa linkini giriniz.");
        System.out.println("Link .html ile bitmelidir. örnek: http://www.cizgifilm.in/sunger-bob-izle/rodeo-heyecani.html ");
        System.out.println("Çıkış için exit komutunu kullanabilirsiniz ya da 0 yazabilirsiniz.");
        System.out.println();
        while(true) {
            link = keyboard.nextLine();
            link = link.toLowerCase();
            if(link.equals("exit") || link.equals("0"))
                break;
            switch (link) {
                case "open browser":
                    openBrowser = true;
                    System.out.println("İzleme ayarı açıldı.");
                    commandInput = true;
                    break;
                case "open browser 0":
                    openBrowser = false;
                    System.out.println("İzleme ayarı kapatıldı.");
                    commandInput = true;
                    break;
                case "downloader 0":
                case "downloader turn off":
                    downloader = false;
                    commandInput = true;
                    System.out.println("Downloader kapatıldı");
                    break;
                case "downloader":
                    if (downloader) {
                        System.out.println("Downloader kapatıldı");
                        downloader = false;
                        commandInput = true;
                    } else {
                        System.out.println("Downloader açıldı");
                        downloader = true;
                        commandInput = true;
                    }
                    break;
            }
            if(link.startsWith("http://www.cizgifilm.in") && link.endsWith(".html")) {
                System.out.println("Link doğru gözüküyor...");
                System.out.println("Dosya Aranıyor...");
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(link))
                        .GET() // GET is default
                        .build();

                HttpResponse<String> response;

                {
                    try {
                        response = client.send(request,
                                HttpResponse.BodyHandlers.ofString());
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                // get video name
                String[] parts = link.split("/");
                String fileName = parts[parts.length-1].replace(".html", "");
                fileName += ".mp4";
                System.out.println("Dosya Ismi: " + fileName);
                // get video link
                int startIndex = response.body().indexOf("file=");
                int lastIndex = response.body().indexOf("&amp;",startIndex);
                String fileLink = "";
                for (int i=startIndex + 5; i<lastIndex; i++) {
                    fileLink += response.body().charAt(i);
                }
                if(fileLink.startsWith("http")) {
                    System.out.println("Dosya bulundu...");
                }
                else {
                    if(fileLink.startsWith("/")) {
                        String httpPrefix = "http://www.cizgifilm.in";
                        httpPrefix += fileLink;
                        fileLink = httpPrefix;
                    }
                    else {
                        String httpPrefix = "http://www.cizgifilm.in/";
                        httpPrefix += fileLink;
                        fileLink = httpPrefix;
                    }
                }
                System.out.println("Video Linki: " + fileLink);
                // Open in browser.
                if(openBrowser) {
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI(fileLink));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Get video from link
                if(downloader) {
                    String home = System.getProperty("user.home");
                    File file = new File(home+"/Downloads/cizgifilm/" + fileName);
                    file.getParentFile().mkdirs();
                    System.out.println(file.getPath());
                    try (BufferedInputStream inputStream = new BufferedInputStream(new URL(fileLink).openStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        int kbytesCounter = 0;
                        System.out.println("İndirme başladı...");
                        while((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1){
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                            kbytesCounter++;
                        }

                        System.out.println("\n" + format.format((double)kbytesCounter/1024) + "MB indirildi.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Dosya indirildi. Dosya yolu: " + file.getPath());
                    System.out.println();
                }
            } else {
                if(!commandInput) {
                    System.out.println("Lütfen geçerli bir dosya yolu giriniz.");
                    System.out.println();
                } else {
                    commandInput = false;
                }
            }
        }
    }
}