package com.umbrela.comics.downloader.cartoonmad;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/*
 * 下載 http://web2.cartoonmad.com 網站的漫畫
 * 使用方法:
 * 1. 先到網站找漫畫
 * 2. 查看網頁源代碼, 找到圖片的 <img> tag, 分析圖片URL後將prefix貼在COMICS_URL中
 * 漫畫圖片URL的這段是固定的 (http://web2.cartoonmad.com/c86es736z62/1221/001(第幾集)/001.jpg)
 */

public class SingleFolderDownloader
{
//	private static final String COMICS_NAME = "進擊的巨人";
//	private static final String COMIC_URL = "https://www.cartoonmad.com/comic/1221.html";
//	private static final String PAGE_URL = "https://www.cartoonmad.com/75632/1221/";
	
//	private static final String COMICS_NAME = "一拳超人";
//	private static final String COMIC_URL = "https://www.cartoonmad.com/comic/3583.html";
//	private static final String PAGE_URL = "https://www.cartoonmad.com/75632/3583/";
	
	private static final String COMICS_NAME = "刮掉鬍子的我與撿到的女高中生";
	private static final String COMIC_URL = "https://www.cartoonmad.com/comic/8071.html";
	private static final String PAGE_URL = "https://www.cartoonmad.com/75632/8071/";

	// 從第幾集開始下載
	private static final int START_CHAPTER = 139;
	// 下載到這集
	private static final int END_CHAPTER = 139;
	
	private static final String SAVE_PATH = "C:/ZZZ/comics/" + COMICS_NAME + "/";
	
	public static void main(String[] args) throws IOException
	{
		TreeMap<Integer, Integer> chapterPageMap = CartoonMadChapterPagesFinder.execute(COMIC_URL);
		
		File destFolder = new File(SAVE_PATH);
		if (!destFolder.exists()) {
			Files.createDirectories(Paths.get(SAVE_PATH));
		}
		
		downloadAll(chapterPageMap, destFolder);
//		downloadChapters(chapterPageMap, START_CHAPTER, END_CHAPTER, destFolder);
		
		System.out.println("All Done!");
	}
	
	public static void downloadAll(TreeMap<Integer, Integer> chapterPageMap, File destFolder)
	{
		ArrayList<Integer> chapterList = new ArrayList<Integer>(chapterPageMap.keySet());
		int firstChapter = chapterList.get(0);
		int lastChapter = chapterList.get(chapterList.size() - 1);
		
		for (int i = firstChapter; i <= lastChapter; i++)
		{
			downloadSingleFolder(chapterPageMap, destFolder, i);
		}
	}
	
	public static void downloadChapters(TreeMap<Integer, Integer> chapterPageMap, final int start, int end, File destFolder)
	{
		for (int i = start; i <= end; i++)
		{
			downloadSingleFolder(chapterPageMap, destFolder, i);
		}
	}
	
	
	public static void downloadSingleFolder(Map<Integer, Integer> chapterPageMap, File folder, int chapter)
	{
		final String chapterLocal = String.format("%03d", chapter);
		final String chapterRemote =  "" + chapterLocal;
		
		final String LOCAL_FILENAME_FORMAT = "%s-%03d.jpg";
		final String REMOTE_FILENAME_FORMAT = "%03d.jpg";
		
		int failUrlCount = 0;
		
		Integer totalPages = chapterPageMap.get(chapter);
		if (totalPages == null) {
			System.out.println("Error! There is no Chapter " + chapter + " for this comics!");
		}
		else {
			System.out.println("Chapter " + chapter + ": " + totalPages + " pages");
			for (int i = 1; i <= totalPages; i++)
			{
				String remoteFileName = String.format(REMOTE_FILENAME_FORMAT, i);
				String urlStr = PAGE_URL + chapterRemote + "/" + remoteFileName;
				System.out.println("Download Image: " + urlStr);
				
				URL url = null;
				try
				{
					url = new URL(urlStr);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.connect();
					int statusCode = connection.getResponseCode();
					if (statusCode != 200)
					{
						System.out.println("statusCode: " + statusCode + "\n");
						failUrlCount++;
						if (failUrlCount >= 3)
							break;
						
						continue;
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
					failUrlCount++;
					System.out.println("URL Error: " + failUrlCount + "\n");
					if (failUrlCount >= 3)
						break;
					
					continue;
				}
				
				InputStream is;
				try
				{
					is = new BufferedInputStream(url.openStream());
				}
				catch (IOException e)
				{
					failUrlCount++;
					System.out.println("InputStream Error: " + failUrlCount + "\n");
					if (failUrlCount >= 3)
						break;
					
					continue;
				};
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				try
				{
					int numOfByteRead = is.read(buf);
					while (numOfByteRead != -1)
					{
						baos.write(buf, 0, numOfByteRead);
						numOfByteRead = is.read(buf);
					}
					
					is.close();
					baos.close();
					
					String localFileName = String.format(LOCAL_FILENAME_FORMAT, chapterLocal, i);
					File file = new File(folder, localFileName);
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(baos.toByteArray());
					
					System.out.println("Save to: " + file.getAbsolutePath() + "\n");
					
					fos.close();
				}
				catch (IOException e)
				{
					failUrlCount++;
					System.out.println("Read/Write Error: " + failUrlCount);
					if (failUrlCount >= 3)
						break;
				}
			}
		}
	}
}
