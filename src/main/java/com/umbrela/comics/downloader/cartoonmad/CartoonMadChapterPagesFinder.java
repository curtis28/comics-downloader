package com.umbrela.comics.downloader.cartoonmad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CartoonMadChapterPagesFinder {
	
	public static final String COMIC_URL = "https://www.cartoonmad.com/comic/8193.html";

	public static void main(String[] args) throws IOException {
		System.out.println("Analysing " + COMIC_URL);
		TreeMap<Integer, Integer> resultMap = execute(COMIC_URL);
		ArrayList<Integer> chapterList = new ArrayList<Integer>(resultMap.keySet());
		for (int i = 0; i < chapterList.size(); i++)
		{
			Integer chapter = chapterList.get(i);
			int numOfPages = resultMap.get(chapter);
			System.out.println("chapter " + chapter + ": " + numOfPages + " pages");
		}
		System.out.println("Complete!");
	}

	public static TreeMap<Integer, Integer> execute(String comicUrl) throws IOException {

		TreeMap<Integer, Integer> resultMap = new TreeMap<Integer, Integer>();
		Integer chapter = null;
		Integer pages = null;
		
		Response response = Jsoup.connect(comicUrl).execute();
		Document doc = response.parse();
		
		Elements fieldsetList = doc.select("fieldset");
		for (Element fieldset : fieldsetList) {
			Elements cellList = fieldset.select("table td");
			for (Element cell : cellList) {

				Element anchorTag = cell.selectFirst("a");
				if (anchorTag != null) {
					String chapterName = anchorTag.text();
					String chapterNumber = chapterName.replaceAll("[^0-9]", "");
					if (StringUtils.isNotBlank(chapterNumber)) {
						chapter = Integer.parseInt(chapterNumber);

						Element fontTag = cell.selectFirst("font");
						if (fontTag != null) {
							String pageNumDesc = fontTag.text();
							String totalPages = pageNumDesc.replaceAll("[^0-9]", "");
							pages = Integer.parseInt(totalPages);
							resultMap.put(chapter, pages);
						}
					}
				}
			}
		}
		
		return resultMap;
	}
}
